package com.emailmanager.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
    private Long id;
    private Long senderId;
    private String subject;
    private String content;
    private Integer isHtml;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    private List<EmailAttachment> attachments;
    private User sender;

    // 不再需要复杂的嵌套列表，用一个简单的字符串来接收收件人信息
    private String toRecipients;
}