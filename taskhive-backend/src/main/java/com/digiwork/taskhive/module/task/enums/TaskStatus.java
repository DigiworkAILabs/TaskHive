package com.digiwork.taskhive.module.task.enums;

import java.util.Map;
import java.util.Set;

public enum TaskStatus {

    TODO,
    IN_PROGRESS,
    IN_REVIEW,
    DONE,
    CANCELLED;

    private static final Map<TaskStatus, Set<TaskStatus>> VALID_TRANSITIONS = Map.of(
            TODO, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(IN_REVIEW, CANCELLED),
            IN_REVIEW, Set.of(DONE, IN_PROGRESS, CANCELLED),
            DONE, Set.of(CANCELLED),
            CANCELLED, Set.of());

    public boolean canTransitionTo(TaskStatus target) {
        return VALID_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
