package com.digiwork.taskhive.module.task.enums;

import java.util.Map;
import java.util.Set;

public enum TaskStatus {

    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    PENDING_APPROVAL,
    DONE,
    CANCELLED;

    private static final Map<TaskStatus, Set<TaskStatus>> VALID_TRANSITIONS = Map.ofEntries(
            Map.entry(TODO, Set.of(IN_PROGRESS, CANCELLED)),
            Map.entry(IN_PROGRESS, Set.of(IN_REVIEW, CANCELLED)),
            Map.entry(IN_REVIEW, Set.of(DONE, IN_PROGRESS, CANCELLED, PENDING_APPROVAL)),
            Map.entry(PENDING_APPROVAL, Set.of(DONE, IN_REVIEW, CANCELLED)),
            Map.entry(DONE, Set.of(CANCELLED)),
            Map.entry(CANCELLED, Set.of()));

    public boolean canTransitionTo(TaskStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public Set<TaskStatus> getAllowedTransitions() {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of());
    }
}
