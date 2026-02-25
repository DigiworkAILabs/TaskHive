package com.digiwork.taskhive.module.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    private Boolean emailEnabled;
    private Boolean inAppEnabled;
    private Boolean taskAssigned;
    private Boolean taskOverdue;
    private Boolean dailyDigest;
    private LocalTime digestTime;
}
