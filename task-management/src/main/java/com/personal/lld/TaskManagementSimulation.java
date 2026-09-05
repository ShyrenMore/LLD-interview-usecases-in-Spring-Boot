package com.personal.lld;

import com.personal.lld.controller.TaskAssignmentController;
import com.personal.lld.controller.TaskController;
import com.personal.lld.controller.TaskNotificationController;
import com.personal.lld.controller.TaskStateController;
import com.personal.lld.domain.*;
import com.personal.lld.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class TaskManagementSimulation implements CommandLineRunner {

    private final TaskController taskController;
    private final TaskAssignmentController assignmentController;
    private final TaskStateController stateController;
    private final TaskNotificationController notificationController;
    private final CommentRepository commentRepository;


    public static void main(String[] args) {
        SpringApplication.run(TaskManagementSimulation.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("🚀 Starting Task Management System Simulation...");

        // Spring Boot manages repositories, services and controllers through dependency injection.
        // No manual object construction is required here.

        log.info("=== 📱 FRONTEND: User Dashboard ===");
        simulateUserDashboard();

        log.info("=== ✨ FRONTEND: Task Creation ===");
        simulateTaskCreation();

        log.info("=== 🔄 FRONTEND: Task Management ===");
        simulateTaskManagement();

        log.info("=== 👥 FRONTEND: Task Collaboration ===");
        simulateTaskCollaboration();

        log.info("=== 🔍 FRONTEND: Task Search ===");
        simulateTaskSearch();

        log.info("=== 🔔 FRONTEND: Notifications ===");
        simulateNotificationManagement();

        log.info("✅ Simulation completed successfully!");
    }

    private void simulateUserDashboard() {
        log.info("User opens dashboard...");

        TaskSearchCriteria userTasksCriteria = new TaskSearchCriteria()
            .assigneeId(1)
            .status(TaskStatus.TODO);

        List<Task> userTasks =
            taskController.searchTasks(userTasksCriteria);

        log.info("📋 User has {} TODO tasks", userTasks.size());

        if (!userTasks.isEmpty()) {
            List<TaskChangeLog> recentChanges =
                notificationController.getTaskHistory(
                    userTasks.get(0).getId()
                );

            log.info(
                "📢 Recent changes for first task: {} updates",
                recentChanges.size()
            );
        }
    }

    private void simulateTaskCreation() {
        log.info("User clicks 'Create New Task' button...");

        LocalDateTime dueDate =
            LocalDateTime.now().plusDays(7);

        Task newTask = taskController.createTask(
            "Implement User Authentication",
            "Create login, registration, and password reset functionality",
            dueDate,
            "HIGH",
            1
        );

        log.info(
            "✅ Task created: {} (ID: {})",
            newTask.getTitle(),
            newTask.getId()
        );

        log.info("User assigns task to team member...");

        assignmentController.assignTask(
            newTask.getId(),
            2
        );

        log.info("👤 Task assigned to user ID: 2");

        log.info("User adds subtask...");

        Task subtask = taskController.addSubtask(
            newTask.getId(),
            "Design Database Schema",
            "Create user table and authentication tables",
            dueDate.minusDays(2),
            "MEDIUM",
            1
        );

        log.info(
            "📝 Subtask added: {}",
            subtask.getTitle()
        );
    }

    private void simulateTaskManagement() {
        log.info("User opens task management panel...");

        TaskSearchCriteria criteria = new TaskSearchCriteria()
            .status(TaskStatus.TODO);

        List<Task> todoTasks =
            taskController.searchTasks(criteria);

        if (!todoTasks.isEmpty()) {
            Task taskToManage = todoTasks.get(0);

            log.info(
                "📋 Managing task: {}",
                taskToManage.getTitle()
            );

            log.info("User edits task details...");

            Task updatedTask = taskController.updateTask(
                taskToManage.getId(),
                taskToManage.getTitle() + " (Updated)",
                taskToManage.getDescription()
                    + " - Additional requirements added",
                taskToManage.getDueDate().plusDays(1),
                "HIGH"
            );

            log.info(
                "✏️ Task updated: {}",
                updatedTask.getTitle()
            );

            log.info(
                "User changes task status to In Progress..."
            );

            stateController.updateTaskStatus(
                taskToManage.getId(),
                TaskStatus.IN_PROGRESS
            );

            log.info(
                "🔄 Task status changed to IN_PROGRESS"
            );

            log.info("User reassigns task...");

            assignmentController.assignTask(
                taskToManage.getId(),
                2
            );

            log.info("👤 Task reassigned to user ID: 2");
        }
    }

    private void simulateTaskCollaboration() {
        log.info(
            "User opens task collaboration panel..."
        );

        log.info(
            "User subscribes to task notifications..."
        );

        List<Task> availableTasks =
            taskController.searchTasks(
                new TaskSearchCriteria()
            );

        if (!availableTasks.isEmpty()) {
            int taskId = availableTasks.get(0).getId();

            notificationController.subscribeToTask(
                taskId,
                2
            );

            log.info(
                "🔔 User 2 subscribed to task {}",
                taskId
            );

            List<TaskChangeLog> taskHistory =
                notificationController.getTaskHistory(taskId);

            log.info(
                "📚 Task history retrieved: {} changes",
                taskHistory.size()
            );
        } else {
            log.warn("⚠️ No tasks available for subscription");
        }

        log.info("User adds comment to task...");

        Comment comment = new Comment(
            1,
            1,
            2,
            "Great progress! Let's review the implementation."
        );

        commentRepository.save(comment);

        log.info(
            "💬 Comment added: {}",
            comment.getContent()
        );
    }

    private void simulateTaskSearch() {
        log.info("User searches for tasks...");

        TaskSearchCriteria highPriorityCriteria =
            new TaskSearchCriteria()
                .priority(Priority.HIGH);

        List<Task> highPriorityTasks =
            taskController.searchTasks(
                highPriorityCriteria
            );

        log.info(
            "🔍 High priority tasks found: {}",
            highPriorityTasks.size()
        );

        TaskSearchCriteria inProgressCriteria =
            new TaskSearchCriteria()
                .status(TaskStatus.IN_PROGRESS);

        List<Task> inProgressTasks =
            taskController.searchTasks(
                inProgressCriteria
            );

        log.info(
            "🔍 In-progress tasks found: {}",
            inProgressTasks.size()
        );

        TaskSearchCriteria complexCriteria =
            new TaskSearchCriteria()
                .assigneeId(1)
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM);

        List<Task> complexSearchResults =
            taskController.searchTasks(
                complexCriteria
            );

        log.info(
            "🔍 Complex search results: {} tasks",
            complexSearchResults.size()
        );

        log.info(
            "=== 🎯 STRATEGY PATTERN: Sorting Demonstrations ==="
        );

        TaskSearchCriteria prioritySortCriteria =
            new TaskSearchCriteria()
                .sortBy("priority")
                .sortOrder("desc");

        List<Task> prioritySortedTasks =
            taskController.searchTasks(
                prioritySortCriteria
            );

        log.info(
            "📊 Sorting by Priority (HIGH → MEDIUM → LOW):"
        );

        prioritySortedTasks.forEach(task ->
            log.info(
                "  - {} (Priority: {})",
                task.getTitle(),
                task.getPriority()
            )
        );

        TaskSearchCriteria dueDateSortCriteria =
            new TaskSearchCriteria()
                .sortBy("dueDate")
                .sortOrder("asc");

        List<Task> dueDateSortedTasks =
            taskController.searchTasks(
                dueDateSortCriteria
            );

        log.info(
            "📅 Sorting by Due Date (earliest first):"
        );

        dueDateSortedTasks.forEach(task ->
            log.info(
                "  - {} (Due: {})",
                task.getTitle(),
                task.getDueDate()
            )
        );

        TaskSearchCriteria createdDateSortCriteria =
            new TaskSearchCriteria()
                .sortBy("createdDate")
                .sortOrder("desc");

        List<Task> createdDateSortedTasks =
            taskController.searchTasks(
                createdDateSortCriteria
            );

        log.info(
            "🕒 Sorting by Created Date (newest first):"
        );

        createdDateSortedTasks.forEach(task ->
            log.info(
                "  - {} (Created: {})",
                task.getTitle(),
                task.getCreatedAt()
            )
        );

        log.info(
            "✨ Strategy Pattern allows easy swapping of sorting algorithms at runtime!"
        );
    }

    private void simulateNotificationManagement() {
        log.info(
            "User manages notification preferences..."
        );

        log.info(
            "User subscribes to multiple tasks..."
        );

        for (int i = 1; i <= 2; i++) {
            notificationController.subscribeToTask(i, 1);

            log.info(
                "🔔 Subscribed to task {}",
                i
            );
        }

        log.info(
            "User unsubscribes from a task..."
        );

        notificationController.unsubscribeFromTask(2, 1);

        log.info("🔕 Unsubscribed from task 2");

        List<Task> availableTasks =
            taskController.searchTasks(
                new TaskSearchCriteria()
            );

        if (!availableTasks.isEmpty()) {
            int taskId = availableTasks.get(0).getId();

            List<TaskChangeLog> notifications =
                notificationController.getTaskHistory(taskId);

            log.info(
                "📢 Notifications for task {}: {} updates",
                taskId,
                notifications.size()
            );
        } else {
            log.warn(
                "⚠️ No tasks available for notification history"
            );
        }

        log.info(
            "📱 Real-time notification received: Task status changed!"
        );
    }
}
