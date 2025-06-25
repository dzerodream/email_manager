package com.emailmanager.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRecipient {
    private Long id;
    private Long emailId;
    private String recipientEmail;
    private String recipientType;
}