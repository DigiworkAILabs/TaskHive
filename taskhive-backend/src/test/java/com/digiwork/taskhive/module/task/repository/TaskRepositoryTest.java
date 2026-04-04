package com.digiwork.taskhive.module.task.repository;

import com.digiwork.taskhive.module.auth.enums.UserStatus;
import com.digiwork.taskhive.module.auth.model.User;
import com.digiwork.taskhive.module.auth.repository.UserRepository;
import com.digiwork.taskhive.module.employee.model.Employee;
import com.digiwork.taskhive.module.employee.repository.EmployeeRepository;
import com.digiwork.taskhive.module.task.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import com.digiwork.taskhive.integration.BaseIntegrationTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for TaskRepository.
 * Uses Testcontainers and real PostgreSQL instance.
 */
@Transactional
class TaskRepositoryTest extends BaseIntegrationTest {

        @Autowired
        private TaskRepository taskRepository;

        @Autowired
        private EmployeeRepository employeeRepository;

        @Autowired
        private UserRepository userRepository;

        private UUID employeeId;

        @BeforeEach
        void setUp() {
                // No deleteAll() needed — @Transactional rolls back each test's data
                // automatically.
                // deleteAll() was previously causing FK constraint violations because the admin
                // seeder
                // user has child rows in refresh_tokens, notification_preferences, etc. that
                // weren't
                // being deleted first, leaving employeeId null and failing every test.

                User user = User.builder()
                                .email("user." + UUID.randomUUID() + "@example.com")
                                .passwordHash("shhhh")
                                .firstName("Test")
                                .lastName("User")
                                .status(UserStatus.ACTIVE)
                                .build();
                user = userRepository.save(user);

                // Create an employee
                Employee employee = Employee.builder()
                                .userId(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .status("ACTIVE")
                                .build();
                employee = employeeRepository.save(employee);
                employeeId = employee.getId();
        }

        @Test
        @DisplayName("should find active task by ID and isDeleted=false")
        void findByIdAndIsDeletedFalse() {
                // given
                Task task = Task.builder()
                                .title("Test Task")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(1))
                                .build();
                task = taskRepository.save(task);

                // when
                Optional<Task> result = taskRepository.findByIdAndIsDeletedFalse(task.getId());

                // then
                assertThat(result).isPresent();
                assertThat(result.get().getTitle()).isEqualTo("Test Task");
        }

        @Test
        @DisplayName("should find all tasks with various filters")
        void findAllWithFilters() {
                // given
                Task task1 = Task.builder()
                                .title("Development")
                                .status("IN_PROGRESS")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(1))
                                .build();
                Task task2 = Task.builder()
                                .title("Testing")
                                .status("TODO")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(1))
                                .build();
                taskRepository.saveAll(List.of(task1, task2));

                // when / then
                assertThat(taskRepository.findAllWithFilters("IN_PROGRESS", null, employeeId, null, null,
                                PageRequest.of(0, 10))).hasSize(1);
                assertThat(taskRepository.findAllWithFilters(null, null, employeeId, null, null, PageRequest.of(0, 10)))
                                .hasSize(2);
        }

        @Test
        @DisplayName("should search tasks with keyword and filter")
        void searchTasksExtended() {
                // given
                Task task = Task.builder()
                                .title("Fix bug in auth")
                                .status("DONE")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(1))
                                .build();
                taskRepository.save(task);

                // when
                List<Task> result = taskRepository.searchTasksExtended("auth", employeeId, PageRequest.of(0, 10))
                                .getContent();

                // then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getTitle()).contains("auth");
        }

        @Test
        @DisplayName("should find overdue tasks")
        void findOverdueTasks() {
                // given
                Task task = Task.builder()
                                .title("Late Task")
                                .status("TODO")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().minusDays(1))
                                .build();
                taskRepository.save(task);

                // when
                List<Task> result = taskRepository.findOverdueTasks(LocalDateTime.now());

                // then
                assertThat(result).hasSize(1);
                assertThat(result.get(0).getTitle()).isEqualTo("Late Task");
        }

        @Test
        @DisplayName("should find my tasks with filters")
        void findMyTasksWithFilters() {
                // given — two tasks assigned to the same employee with different statuses
                Task todoTask = Task.builder()
                                .title("My TODO Task")
                                .status("TODO")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(1))
                                .build();
                Task inProgressTask = Task.builder()
                                .title("My IN_PROGRESS Task")
                                .status("IN_PROGRESS")
                                .assignedTo(employeeId)
                                .dueDate(LocalDateTime.now().plusDays(2))
                                .build();
                taskRepository.saveAll(List.of(todoTask, inProgressTask));

                // when — no status filter → both tasks
                var allResults = taskRepository.findMyTasksWithFilters(
                                employeeId, null, null, PageRequest.of(0, 10));

                // then
                assertThat(allResults.getContent()).hasSize(2);

                // when — status filter "To-Do" → only one task
                var todoResults = taskRepository.findMyTasksWithFilters(
                                employeeId, "TODO", null, PageRequest.of(0, 10));

                // then
                assertThat(todoResults.getContent()).hasSize(1);
                assertThat(todoResults.getContent().get(0).getTitle()).isEqualTo("My TODO Task");
        }
}