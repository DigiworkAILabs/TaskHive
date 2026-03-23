# TaskHive — Gemini Auto Priority Integration
# Next.js Frontend + Spring Boot Backend
# Prompt for Antigravity IDE

---

## PROMPT (Copy and paste in Antigravity IDE)

---

You are an expert Next.js (TypeScript) and Java Spring Boot developer working on TaskHive.
Read everything carefully before writing any code.

## Project Context

- **Project:** TaskHive — Enterprise Task Management System
- **Company:** Digiwork
- **Backend:** Java 21 + Spring Boot 3.5.11
- **Base Package:** `com.digiwork.taskhive`
- **Database:** PostgreSQL 18
- **Frontend:** Next.js 16 + React 19 + TypeScript + Shadcn/ui + Tailwind CSS
- **State Management:** React Query + Zustand
- **HTTP Client:** Axios (or fetch with hooks)
- **Build Tool:** Maven (backend)

## What Already Exists

- ✅ Local ML model (pickle) — already working
- ✅ `GeminiService.java` — already exists
- ✅ `GeminiMlController.java` — already exists with `/api/v1/ml/gemini/` endpoints
- ✅ `GeminiPriorityResponse.java` DTO — already exists
- ✅ Task create endpoint — `POST /api/v1/tasks` — already exists
- ✅ Next.js task create page/form — already exists

---

## Feature to Implement

**Current behavior:**
- Admin fills task form → manually selects priority → clicks Create Task

**New behavior:**
- Admin fills task title + description + selects employee
- Priority field is OPTIONAL — user can leave it empty
- When "Create Task" is clicked → Spring Boot calls Gemini automatically
- Gemini suggests priority BEFORE saving task to DB
- If Gemini fails for any reason → default priority = MEDIUM
- Task is NEVER blocked by Gemini failure

---

## RULES

```
✅ Priority field must be OPTIONAL in task create form
✅ Gemini auto-suggest happens on BACKEND before DB save
✅ If Gemini fails → use MEDIUM as default — NEVER block task creation
✅ Next.js shows what priority AI set after task is created
✅ User can still manually select priority — if selected, Gemini is skipped
✅ Do NOT remove existing "✨ Gemini AI" suggest button
✅ Do NOT touch local ML model code
✅ Do NOT touch Flutter code
✅ Only PostgreSQL 18 — do NOT write any DB migration for this feature
```

---

## BACKEND CHANGES

### Change 1 — Create GeminiPriorityService.java (NEW FILE)

Create: `com.digiwork.taskhive.module.ml.service.GeminiPriorityService`

```java
package com.digiwork.taskhive.module.ml.service;

import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiPriorityService {

    private final GeminiService geminiService;
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ml.gemini.enabled:true}")
    private boolean geminiEnabled;

    private static final Set<String> VALID_PRIORITIES =
        Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    /**
     * Auto-suggest task priority using Gemini AI.
     * Called BEFORE task is saved to DB.
     * Always returns a valid priority — never throws exception.
     */
    public String suggestPriority(String taskTitle,
                                   String taskDescription,
                                   UUID assignedEmployeeId) {
        // Gemini disabled → MEDIUM
        if (!geminiEnabled) {
            log.info("[GeminiPriorityService] Gemini disabled — using MEDIUM");
            return "MEDIUM";
        }

        // Empty title → MEDIUM
        if (taskTitle == null || taskTitle.isBlank()) {
            return "MEDIUM";
        }

        try {
            // Fetch employee data for better accuracy
            double completionRate = 0.75;
            double avgHours = 4.0;
            int activeTasks = 0;

            if (assignedEmployeeId != null) {
                try {
                    Employee emp = employeeService.getById(assignedEmployeeId);
                    completionRate = emp.getCompletionRate();
                    avgHours      = emp.getAvgHoursPerTask();
                    activeTasks   = emp.getActiveTasks();
                } catch (Exception e) {
                    log.warn("[GeminiPriorityService] Employee fetch failed, using defaults");
                }
            }

            // Build prompt
            String prompt = buildPrompt(taskTitle, taskDescription,
                completionRate, avgHours, activeTasks);

            // Call Gemini
            String jsonResponse = geminiService.askGemini(prompt);

            // Parse and validate priority
            String priority = parsePriority(jsonResponse);

            log.info("[GeminiPriorityService] Auto priority for '{}': {}",
                taskTitle, priority);

            return priority;

        } catch (Exception e) {
            log.warn("[GeminiPriorityService] Gemini failed: {}. Using MEDIUM.", e.getMessage());
            return "MEDIUM"; // Safe fallback — NEVER fail task creation
        }
    }

    private String buildPrompt(String title, String description,
                                double completionRate, double avgHours,
                                int activeTasks) {
        String safeDesc = (description != null && !description.isBlank())
            ? description : "No description provided";

        return """
            You are a task management AI for an enterprise system.
            Analyze this task and suggest the appropriate priority level.
            Return ONLY valid JSON. No explanation. No markdown.

            === TASK ===
            Title: %s
            Description: %s

            === ASSIGNEE HISTORY ===
            Completion Rate: %.0f%%
            Average Hours Per Task: %.1f
            Currently Active Tasks: %d

            Return ONLY:
            {"priority":"MEDIUM","confidence":0.85,"reason":"one sentence"}

            Rules:
            - priority must be EXACTLY: LOW | MEDIUM | HIGH | CRITICAL
            - Login, payment, security, crash, data loss → HIGH or CRITICAL
            - UI change, minor improvement, documentation → LOW or MEDIUM
            - confidence: 0.0 to 1.0
            - reason: one sentence only
            """.formatted(title, safeDesc,
                completionRate * 100, avgHours, activeTasks);
    }

    private String parsePriority(String jsonResponse) {
        try {
            JsonNode node = objectMapper.readTree(jsonResponse);
            String priority = node.get("priority").asText().toUpperCase().trim();
            if (VALID_PRIORITIES.contains(priority)) {
                return priority;
            }
            log.warn("[GeminiPriorityService] Invalid priority '{}' from Gemini, using MEDIUM",
                priority);
            return "MEDIUM";
        } catch (Exception e) {
            log.warn("[GeminiPriorityService] Parse failed: {}. Using MEDIUM.", jsonResponse);
            return "MEDIUM";
        }
    }
}
```

### Change 2 — TaskService.java

Find existing `createTask()` method and add Gemini auto-priority BEFORE saving:

```java
// Add injection at top of TaskService class:
private final GeminiPriorityService geminiPriorityService;

// Find createTask() method and add this block BEFORE saving to DB:
public TaskResponse createTask(CreateTaskRequest request, UUID createdBy) {

    // ... existing validation (keep as is) ...

    // ── AUTO PRIORITY via Gemini ──────────────────────────────
    // Only suggest if user did NOT manually set priority
    if (request.getPriority() == null || request.getPriority().isBlank()) {
        log.info("[TaskService] Priority not set — asking Gemini...");
        String suggestedPriority = geminiPriorityService.suggestPriority(
            request.getTitle(),
            request.getDescription(),
            request.getAssignedTo()
        );
        request.setPriority(suggestedPriority);
        log.info("[TaskService] Gemini set priority to: {}", suggestedPriority);
    } else {
        log.info("[TaskService] User set priority manually: {}", request.getPriority());
    }
    // ─────────────────────────────────────────────────────────

    // ... existing save to DB (keep as is) ...
}
```

### Change 3 — CreateTaskRequest.java

Make priority field OPTIONAL (remove @NotBlank or @NotNull if present):

```java
public class CreateTaskRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    // Priority is now OPTIONAL — Gemini will auto-suggest if null
    // REMOVE @NotBlank or @NotNull from this field
    private String priority; // nullable

    private UUID assignedTo;

    @Future
    private LocalDate dueDate;

    private List<String> tags;

    private Double estimatedHours;
}
```

### Change 4 — TaskResponse.java

Make sure `priority` field is included in response (it should already be there):

```java
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private String priority;        // ← must be in response
    private String status;
    private String aiSuggestedBy;   // ADD THIS: "gemini" or "user" or "default"
    // ... rest of fields ...
}
```

Also update Task entity mapping — add `aiSuggestedBy` field to response:

```java
// In TaskMapper or wherever TaskResponse is built:
response.setAiSuggestedBy(
    wasAutoSuggested ? "gemini" : "user"
);
```

---

## NEXT.JS FRONTEND CHANGES

### Change 5 — Task Create Form Component

Find existing task create form (likely `CreateTaskForm.tsx` or similar) and update:

```tsx
'use client';

import { useState } from 'react';
import { useCreateTask } from '@/hooks/tasks/useCreateTask';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Sparkles, Brain } from 'lucide-react';

type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

interface CreateTaskFormProps {
  onSuccess?: () => void;
}

export function CreateTaskForm({ onSuccess }: CreateTaskFormProps) {
  const { mutate: createTask, isPending } = useCreateTask();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState<string>('');
  const [priority, setPriority] = useState<Priority | ''>(''); // empty = AI will suggest
  const [dueDate, setDueDate] = useState('');
  const [tags, setTags] = useState<string[]>([]);
  const [estimatedHours, setEstimatedHours] = useState('');
  const [aiSuggestedPriority, setAiSuggestedPriority] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    createTask(
      {
        title,
        description,
        assignedTo: selectedEmployee || undefined,
        priority: priority || undefined, // send undefined = Gemini will auto-suggest
        dueDate: dueDate || undefined,
        tags,
        estimatedHours: estimatedHours ? parseFloat(estimatedHours) : undefined,
      },
      {
        onSuccess: (data) => {
          // Show what priority AI set
          if (!priority && data.priority) {
            setAiSuggestedPriority(data.priority);
          }
          onSuccess?.();
        },
      }
    );
  };

  const priorityConfig = {
    LOW:      { color: 'bg-green-100 text-green-700 border-green-200',  label: 'Low' },
    MEDIUM:   { color: 'bg-blue-100 text-blue-700 border-blue-200',     label: 'Medium' },
    HIGH:     { color: 'bg-orange-100 text-orange-700 border-orange-200', label: 'High' },
    CRITICAL: { color: 'bg-red-100 text-red-700 border-red-200',        label: 'Critical' },
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-5">

      {/* Title */}
      <div className="space-y-1.5">
        <label className="text-sm font-medium text-gray-700">
          Title <span className="text-red-500">*</span>
        </label>
        <Input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="Enter task title..."
          required
        />
      </div>

      {/* Description */}
      <div className="space-y-1.5">
        <label className="text-sm font-medium text-gray-700">Description</label>
        <Textarea
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          placeholder="Describe the task..."
          rows={3}
        />
      </div>

      {/* Priority — OPTIONAL with AI badge */}
      <div className="space-y-1.5">
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium text-gray-700">Priority</label>
          {!priority && (
            <Badge
              variant="outline"
              className="text-xs bg-purple-50 text-purple-600 border-purple-200 gap-1"
            >
              <Sparkles className="w-3 h-3" />
              AI will suggest
            </Badge>
          )}
        </div>

        <Select
          value={priority}
          onValueChange={(value) => setPriority(value as Priority | '')}
        >
          <SelectTrigger>
            <SelectValue placeholder="Leave empty — AI will suggest priority" />
          </SelectTrigger>
          <SelectContent>
            {/* Clear option — let AI decide */}
            <SelectItem value="">
              <div className="flex items-center gap-2 text-purple-600">
                <Sparkles className="w-3.5 h-3.5" />
                <span>Let AI decide</span>
              </div>
            </SelectItem>

            {/* Manual options */}
            {(Object.keys(priorityConfig) as Priority[]).map((p) => (
              <SelectItem key={p} value={p}>
                <div className="flex items-center gap-2">
                  <span className={`px-2 py-0.5 rounded text-xs font-medium border ${priorityConfig[p].color}`}>
                    {priorityConfig[p].label}
                  </span>
                </div>
              </SelectItem>
            ))}
          </SelectContent>
        </Select>

        {/* Helper text */}
        {!priority ? (
          <p className="text-xs text-purple-600 flex items-center gap-1">
            <Sparkles className="w-3 h-3" />
            Gemini AI will analyze your task and suggest the best priority
          </p>
        ) : (
          <p className="text-xs text-gray-500">
            You selected priority manually — AI suggestion will be skipped
          </p>
        )}
      </div>

      {/* Employee Select — keep existing */}
      {/* Due Date — keep existing */}
      {/* Tags — keep existing */}
      {/* Estimated Hours — keep existing */}

      {/* Submit Button */}
      <Button
        type="submit"
        disabled={isPending || !title}
        className="w-full"
      >
        {isPending ? (
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            {!priority
              ? 'Creating with AI Priority...'
              : 'Creating Task...'}
          </div>
        ) : (
          <div className="flex items-center gap-2">
            {!priority && <Sparkles className="w-4 h-4" />}
            Create Task
          </div>
        )}
      </Button>

    </form>
  );
}
```

### Change 6 — useCreateTask.ts hook

Find existing `useCreateTask` hook and update:

```typescript
// hooks/tasks/useCreateTask.ts

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner'; // or whatever toast library is used
import { taskApi } from '@/lib/api/tasks';

interface CreateTaskInput {
  title: string;
  description?: string;
  assignedTo?: string;
  priority?: string;       // optional — undefined = AI will suggest
  dueDate?: string;
  tags?: string[];
  estimatedHours?: number;
}

interface TaskResponse {
  id: string;
  title: string;
  priority: string;        // what priority was set (by AI or user)
  status: string;
  aiSuggestedBy?: string;  // "gemini" | "user" | "default"
  // ... other fields
}

export function useCreateTask() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateTaskInput) => taskApi.createTask(data),

    onSuccess: (response: TaskResponse, variables) => {
      // Invalidate task list
      queryClient.invalidateQueries({ queryKey: ['tasks'] });

      // Show success message with AI info
      const wasAiSuggested = !variables.priority && response.aiSuggestedBy === 'gemini';

      if (wasAiSuggested) {
        toast.success(
          `Task created! ✨ Gemini AI set priority to ${response.priority}`,
          {
            description: 'You can edit the priority anytime.',
            duration: 4000,
          }
        );
      } else {
        toast.success('Task created successfully!');
      }
    },

    onError: (error: any) => {
      toast.error('Failed to create task. Please try again.');
    },
  });
}
```

### Change 7 — taskApi.ts (API call)

Find existing task API file and update `createTask`:

```typescript
// lib/api/tasks.ts

export const taskApi = {
  createTask: async (data: {
    title: string;
    description?: string;
    assignedTo?: string;
    priority?: string;        // optional — backend handles AI suggestion
    dueDate?: string;
    tags?: string[];
    estimatedHours?: number;
  }) => {
    const response = await apiClient.post('/api/v1/tasks', {
      title: data.title,
      ...(data.description && { description: data.description }),
      ...(data.assignedTo && { assignedTo: data.assignedTo }),
      ...(data.priority && { priority: data.priority }), // only send if user set it
      ...(data.dueDate && { dueDate: data.dueDate }),
      ...(data.tags?.length && { tags: data.tags }),
      ...(data.estimatedHours && { estimatedHours: data.estimatedHours }),
    });
    return response.data.data;
  },

  // ... other existing methods unchanged ...
};
```

### Change 8 — Task List / Detail — Show AI Badge

In task list or task detail component, show a badge if priority was AI-suggested:

```tsx
// In TaskCard.tsx or TaskDetail.tsx

interface TaskCardProps {
  task: {
    id: string;
    title: string;
    priority: string;
    aiSuggestedBy?: string;
    // ...
  };
}

export function TaskCard({ task }: TaskCardProps) {
  const priorityColors = {
    LOW:      'bg-green-100 text-green-700',
    MEDIUM:   'bg-blue-100 text-blue-700',
    HIGH:     'bg-orange-100 text-orange-700',
    CRITICAL: 'bg-red-100 text-red-700',
  };

  return (
    <div className="...">
      {/* ... other task info ... */}

      {/* Priority badge with AI indicator */}
      <div className="flex items-center gap-1.5">
        <span className={`px-2 py-0.5 rounded text-xs font-medium ${
          priorityColors[task.priority as keyof typeof priorityColors]
        }`}>
          {task.priority}
        </span>

        {/* Show ✨ if Gemini suggested this priority */}
        {task.aiSuggestedBy === 'gemini' && (
          <span title="Priority suggested by Gemini AI">
            <Sparkles className="w-3.5 h-3.5 text-purple-500" />
          </span>
        )}
      </div>
    </div>
  );
}
```

---

## Complete Flow After Implementation

```
Admin opens "Create Task" form in Next.js
        ↓
Fills: Title, Description, Employee
Priority: left empty (AI will suggest badge shows)
        ↓
Clicks "Create Task" button
        ↓
Next.js sends:
POST /api/v1/tasks
{ title, description, assignedTo, priority: undefined }
        ↓
Spring Boot TaskService.createTask() runs
        ↓
priority == null → GeminiPriorityService.suggestPriority()
        ↓
Gemini API → { "priority": "CRITICAL", "confidence": 0.94 }
        ↓
Task saved to DB with priority = CRITICAL, aiSuggestedBy = "gemini"
        ↓
Response → { priority: "CRITICAL", aiSuggestedBy: "gemini" }
        ↓
Next.js toast: "Task created! ✨ Gemini AI set priority to CRITICAL"
        ↓
Task card shows CRITICAL badge with ✨ icon
```

## Fallback Flow (Gemini fails)

```
Gemini API fails (404, 429, network issue)
        ↓
GeminiPriorityService catches exception
        ↓
Returns "MEDIUM" + aiSuggestedBy = "default"
        ↓
Task saved with MEDIUM priority
        ↓
Toast: "Task created successfully!" (no AI mention)
        ↓
Task creation NEVER fails ✅
```

---

## What NOT to Change

```
❌ Do NOT touch MlController.java (local model)
❌ Do NOT touch Flutter code
❌ Do NOT touch existing Gemini ML endpoints
❌ Do NOT write any DB migration (PostgreSQL 18 already set up)
❌ Do NOT make task creation fail if Gemini is down
❌ Do NOT remove existing "✨ Gemini AI" suggest button
```

## What to Create / Change

```
Backend:
✅ GeminiPriorityService.java     → NEW — auto priority logic
✅ TaskService.java               → inject + call GeminiPriorityService before save
✅ CreateTaskRequest.java         → priority field = optional (nullable)
✅ TaskResponse.java              → add aiSuggestedBy field

Next.js Frontend:
✅ CreateTaskForm.tsx             → priority optional + AI badge + loading state
✅ useCreateTask.ts               → toast shows AI suggestion info
✅ taskApi.ts                     → priority optional in API call
✅ TaskCard.tsx / TaskDetail.tsx  → show ✨ icon if AI suggested priority
```

---

## Postman Test

```
Test 1 — WITHOUT priority (AI auto-suggest):
POST http://localhost:8080/api/v1/tasks
Body:
{
  "title": "Fix login crash on Android 14",
  "description": "Users unable to login after OS update",
  "assignedTo": "employee-uuid",
  "dueDate": "2026-05-01",
  "tags": ["bug", "mobile"]
}

Expected Response:
{
  "success": true,
  "data": {
    "title": "Fix login crash on Android 14",
    "priority": "CRITICAL",       ← Gemini set this!
    "aiSuggestedBy": "gemini",    ← AI suggested
    "status": "TODO"
  }
}

Test 2 — WITH priority (user override):
POST http://localhost:8080/api/v1/tasks
Body:
{
  "title": "Update README",
  "priority": "LOW",              ← user set this
  "assignedTo": "employee-uuid",
  "dueDate": "2026-05-01"
}

Expected Response:
{
  "success": true,
  "data": {
    "title": "Update README",
    "priority": "LOW",            ← user's choice kept
    "aiSuggestedBy": "user",      ← user set it
    "status": "TODO"
  }
}
```

---

## application-dev.properties (confirm these are set)

```properties
# Gemini AI
gemini.api.key=YOUR_NEW_API_KEY_HERE
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/
gemini.model=gemini-2.0-flash-lite
gemini.model.fallbacks=gemini-2.0-flash,gemini-1.5-flash-latest
ml.gemini.enabled=true
```

---

*TaskHive — Gemini Auto Priority for Next.js Frontend*
*PostgreSQL 18 | Next.js 16 | Spring Boot 3.5.11*
