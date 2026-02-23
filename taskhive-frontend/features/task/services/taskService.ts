import apiClient from '@/shared/services/api/apiClient';
import { ENDPOINTS } from '@/shared/services/api/endpoints';
import {
    Task,
    TaskListItem,
    CreateTaskData,
    UpdateTaskData,
    UpdateTaskStatusData,
    AddCommentData,
    TaskComment,
    TaskAttachment,
    TaskStatusHistory,
    TaskFilters,
    PageResponse,
} from '../types/task.types';

export const taskService = {
    // POST /tasks — Create task (ADMIN only)
    create: async (data: CreateTaskData): Promise<Task> => {
        const response = await apiClient.post(ENDPOINTS.TASKS.BASE, data);
        return response.data.data;
    },

    // GET /tasks — List with pagination + filters (ADMIN only)
    list: async (filters: TaskFilters): Promise<PageResponse<TaskListItem>> => {
        const params: Record<string, string | number> = {
            page: filters.page,
            size: filters.size,
        };
        if (filters.status) params.status = filters.status;
        if (filters.priority) params.priority = filters.priority;
        if (filters.assignedTo) params.assignedTo = filters.assignedTo;
        if (filters.search) params.search = filters.search;
        if (filters.sortBy) params.sortBy = filters.sortBy;
        if (filters.sortDir) params.sortDir = filters.sortDir;

        const response = await apiClient.get(ENDPOINTS.TASKS.BASE, { params });
        return response.data.data;
    },

    // GET /tasks/{id} — Task detail
    getById: async (id: string): Promise<Task> => {
        const response = await apiClient.get(ENDPOINTS.TASKS.DETAIL(id));
        return response.data.data;
    },

    // PUT /tasks/{id} — Update task (ADMIN only)
    update: async (id: string, data: UpdateTaskData): Promise<Task> => {
        const response = await apiClient.put(ENDPOINTS.TASKS.DETAIL(id), data);
        return response.data.data;
    },

    // DELETE /tasks/{id} — Soft delete (ADMIN only)
    delete: async (id: string): Promise<void> => {
        await apiClient.delete(ENDPOINTS.TASKS.DETAIL(id));
    },

    // PATCH /tasks/{id}/status — Update task status
    updateStatus: async (id: string, data: UpdateTaskStatusData): Promise<Task> => {
        const response = await apiClient.patch(ENDPOINTS.TASKS.STATUS(id), data);
        return response.data.data;
    },

    // POST /tasks/{id}/comments — Add a comment
    addComment: async (id: string, data: AddCommentData): Promise<TaskComment> => {
        const response = await apiClient.post(ENDPOINTS.TASKS.COMMENTS(id), data);
        return response.data.data;
    },

    // GET /tasks/{id}/comments — Get comments
    getComments: async (id: string): Promise<TaskComment[]> => {
        const response = await apiClient.get(ENDPOINTS.TASKS.COMMENTS(id));
        return response.data.data;
    },

    // POST /tasks/{id}/attachments — Upload attachment
    uploadAttachment: async (id: string, file: File): Promise<TaskAttachment> => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await apiClient.post(ENDPOINTS.TASKS.ATTACHMENTS(id), formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return response.data.data;
    },

    // GET /tasks/{id}/attachments — List attachments
    getAttachments: async (id: string): Promise<TaskAttachment[]> => {
        const response = await apiClient.get(ENDPOINTS.TASKS.ATTACHMENTS(id));
        return response.data.data;
    },

    // GET /tasks/{id}/history — Task status history
    getHistory: async (id: string): Promise<TaskStatusHistory[]> => {
        const response = await apiClient.get(ENDPOINTS.TASKS.HISTORY(id));
        return response.data.data;
    },

    // GET /tasks/my-tasks — Logged-in employee's tasks
    getMyTasks: async (filters?: Partial<TaskFilters>): Promise<PageResponse<TaskListItem>> => {
        const params: Record<string, string | number> = {
            page: filters?.page ?? 0,
            size: filters?.size ?? 20,
        };
        if (filters?.status) params.status = filters.status;
        if (filters?.priority) params.priority = filters.priority;
        const response = await apiClient.get(ENDPOINTS.TASKS.MY_TASKS, { params });
        return response.data.data;
    },

    // GET /tasks/overdue — Overdue tasks (ADMIN only)
    getOverdue: async (): Promise<TaskListItem[]> => {
        const response = await apiClient.get(ENDPOINTS.TASKS.OVERDUE);
        return response.data.data;
    },

    // GET /tasks/search?query= — Full-text search
    search: async (query: string, page = 0, size = 20): Promise<PageResponse<TaskListItem>> => {
        const response = await apiClient.get(ENDPOINTS.TASKS.SEARCH, {
            params: { query, page, size },
        });
        return response.data.data;
    },
};
