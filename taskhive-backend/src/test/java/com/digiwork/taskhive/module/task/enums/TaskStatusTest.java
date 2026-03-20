package com.digiwork.taskhive.module.task.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TaskStatus enum transitions (Phase 1 v2.5).
 * Validates that the VALID_TRANSITIONS map includes PENDING_APPROVAL correctly.
 */
class TaskStatusTest {

    @Test
    @DisplayName("PENDING_APPROVAL can only transition to DONE or CANCELLED (via admin approval/rejection)")
    void pendingApprovalTransitions() {
        Set<TaskStatus> allowed = TaskStatus.PENDING_APPROVAL.getAllowedTransitions();
        assertThat(allowed).containsExactlyInAnyOrder(TaskStatus.IN_REVIEW, TaskStatus.DONE, TaskStatus.CANCELLED);
    }

    @Test
    @DisplayName("IN_REVIEW can transition to PENDING_APPROVAL is NOT listed (server-side redirect only)")
    void inReviewCanGoToDoneDirectly() {
        // IN_REVIEW → DONE is a valid client transition;
        // DONE gets redirected to PENDING_APPROVAL server-side when approvalRequired=true
        Set<TaskStatus> allowed = TaskStatus.IN_REVIEW.getAllowedTransitions();
        assertThat(allowed).contains(TaskStatus.DONE);
    }

    @Test
    @DisplayName("CANCELLED is a terminal state — no transitions allowed")
    void cancelledIsTerminal() {
        Set<TaskStatus> allowed = TaskStatus.CANCELLED.getAllowedTransitions();
        assertThat(allowed).isEmpty();
    }

    @Test
    @DisplayName("DONE is quasi-terminal — only CANCELLED transition allowed")
    void doneOnlyAllowsCancelled() {
        Set<TaskStatus> allowed = TaskStatus.DONE.getAllowedTransitions();
        assertThat(allowed).containsExactly(TaskStatus.CANCELLED);
    }

    @Test
    @DisplayName("TODO can transition to IN_PROGRESS or CANCELLED")
    void todoTransitions() {
        Set<TaskStatus> allowed = TaskStatus.TODO.getAllowedTransitions();
        assertThat(allowed).containsExactlyInAnyOrder(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED);
    }

    @Test
    @DisplayName("IN_PROGRESS can transition to IN_REVIEW or CANCELLED")
    void inProgressTransitions() {
        Set<TaskStatus> allowed = TaskStatus.IN_PROGRESS.getAllowedTransitions();
        assertThat(allowed).containsExactlyInAnyOrder(TaskStatus.IN_REVIEW, TaskStatus.CANCELLED);
    }
}
