package com.digiwork.taskhive.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private boolean success;
    private String message;
    private List<String> errors;
    private String path;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static ErrorResponse of(String message, String path) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(String message, List<String> errors, String path) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .path(path)
                .build();
    }
}
