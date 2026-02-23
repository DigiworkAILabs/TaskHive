package com.digiwork.taskhive.module.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchRequest {

    private String query;
    private int page = 0;
    private int size = 10;
}
