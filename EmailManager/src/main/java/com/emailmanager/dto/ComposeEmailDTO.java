package com.emailmanager.dto;
import com.emailmanager.entity.EmailAttachment;

import lombok.Data;
import java.util.List;

@Data
public class ComposeEmailDTO {
    private String subject;
    private String content;
    private Boolean isHtml = false;

    // 核心修改：不再接收用户ID，而是直接接收邮箱地址字符串
    private List<String> toEmails;
    private List<String> ccEmails;
    private List<String> bccEmails;

    private List<EmailAttachment> attachments;
}