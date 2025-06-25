// com.emailmanager.controller.EmailController.java

package com.emailmanager.controller;

import com.emailmanager.dto.ComposeEmailDTO;
import com.emailmanager.entity.Email;
import com.emailmanager.entity.EmailAttachment;
import com.emailmanager.entity.Mailbox;
import com.emailmanager.service.EmailService;
import com.emailmanager.util.FileUploadUtil;
import com.emailmanager.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest; // 引入 HttpServletRequest
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/emails")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // --- 核心修改：将所有 HttpSession session 改为 HttpServletRequest request ---
    // --- 并将 session.getAttribute("user") 改为 request.getAttribute("userId") ---

    @GetMapping("/inbox")
    public Result<List<Mailbox>> getInboxEmails(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> mailboxes = emailService.getInboxEmails(currentUserId);
        return Result.success(mailboxes);
    }

    @GetMapping("/sent")
    public Result<List<Mailbox>> getSentEmails(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> mailboxes = emailService.getSentEmails(currentUserId);
        return Result.success(mailboxes);
    }

    @GetMapping("/drafts")
    public Result<List<Mailbox>> getDraftEmails(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> mailboxes = emailService.getDraftEmails(currentUserId);
        return Result.success(mailboxes);
    }

    @GetMapping("/trash")
    public Result<List<Mailbox>> getTrashEmails(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> mailboxes = emailService.getTrashEmails(currentUserId);
        return Result.success(mailboxes);
    }

    @GetMapping("/starred")
    public Result<List<Mailbox>> getStarredEmails(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> mailboxes = emailService.getStarredEmails(currentUserId);
        return Result.success(mailboxes);
    }

    @GetMapping("/archived")
    public Result<List<Mailbox>> getArchivedEmails(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> mailboxes = emailService.getArchivedEmails(currentUserId);
        return Result.success(mailboxes);
    }

    @GetMapping("/search")
    public Result<List<Mailbox>> searchEmails(@RequestParam("keyword") String keyword, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Mailbox> results = emailService.searchEmails(currentUserId, keyword);
        return Result.success(results);
    }

    @PostMapping("/drafts")
    public Result<Long> createDraft(@RequestBody ComposeEmailDTO composeDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Email email = new Email();
        email.setSenderId(currentUserId);
        email.setSubject(composeDTO.getSubject());
        email.setContent(composeDTO.getContent());
        email.setIsHtml(composeDTO.getIsHtml() != null && composeDTO.getIsHtml() ? 1 : 0);
        List<EmailAttachment> attachments = composeDTO.getAttachments() != null ? composeDTO.getAttachments() : new ArrayList<>();

        Long draftId = emailService.createDraft(
                email,
                composeDTO.getToEmails(),
                composeDTO.getCcEmails(),
                composeDTO.getBccEmails(),
                attachments
        );
        return Result.success("草稿创建成功", draftId);
    }

    @PostMapping("/{id}/send")
    public Result<Void> sendDraft(@PathVariable Long id, @RequestBody ComposeEmailDTO finalRecipientsDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        try {
            boolean success = emailService.sendEmail(
                    id,
                    currentUserId,
                    finalRecipientsDTO.getToEmails(),
                    finalRecipientsDTO.getCcEmails(),
                    finalRecipientsDTO.getBccEmails()
            );
            return success ? Result.success("邮件发送成功", null) : Result.failure(500, "邮件发送失败");
        } catch (RuntimeException e) {
            return Result.failure(400, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Email> getEmailDetails(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        Email email = emailService.getEmailDetails(currentUserId, id);
        return email != null ? Result.success(email) : Result.failure(403, "邮件不存在或无权查看");
    }

    // --- 邮件状态管理 API ---

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.markAsRead(currentUserId, id);
        return success ? Result.success("已标记为已读", null) : Result.failure(404, "操作失败");
    }

    // ... 对以下所有方法执行相同的替换 ...

    @PutMapping("/{id}/unread")
    public Result<Void> markAsUnread(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.markAsUnread(currentUserId, id);
        return success ? Result.success("已标记为未读", null) : Result.failure(404, "操作失败");
    }

    @PutMapping("/{id}/star")
    public Result<Void> starEmail(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.starEmail(currentUserId, id);
        return success ? Result.success("星标成功", null) : Result.failure(404, "操作失败");
    }

    @PutMapping("/{id}/unstar")
    public Result<Void> unstarEmail(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.unstarEmail(currentUserId, id);
        return success ? Result.success("取消星标成功", null) : Result.failure(404, "操作失败");
    }

    @PutMapping("/{id}/archive")
    public Result<Void> archiveEmail(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.archiveEmail(currentUserId, id);
        return success ? Result.success("邮件已归档", null) : Result.failure(404, "操作失败");
    }

    @PutMapping("/{id}/unarchive")
    public Result<Void> unarchiveEmail(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.unarchiveEmail(currentUserId, id);
        return success ? Result.success("邮件已从归档中恢复", null) : Result.failure(404, "操作失败");
    }

    @PutMapping("/{id}/trash")
    public Result<Void> moveToTrash(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.moveToTrash(currentUserId, id);
        return success ? Result.success("已移入垃圾箱", null) : Result.failure(404, "操作失败");
    }

    @PutMapping("/{id}/restore")
    public Result<Void> restoreFromTrash(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.restoreFromTrash(currentUserId, id);
        return success ? Result.success("邮件已恢复", null) : Result.failure(404, "操作失败");
    }

    @DeleteMapping("/{id}/permanent")
    public Result<Void> permanentDelete(@PathVariable("id") Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean success = emailService.permanentDeleteEmail(currentUserId, id);
        return success ? Result.success("邮件已永久删除", null) : Result.failure(404, "操作失败");
    }

    @PostMapping("/attachments/upload")
    public Result<EmailAttachment> uploadAttachment(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.failure(400, "上传文件不能为空");
        }
        String filePath = FileUploadUtil.saveFile(file);
        if (filePath != null) {
            EmailAttachment attachment = new EmailAttachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(filePath);
            attachment.setFileType(file.getContentType());
            attachment.setFileSize(file.getSize());
            return Result.success("文件上传成功", attachment);
        } else {
            return Result.failure(500, "文件上传失败");
        }
    }


    // EmailController.java
    @DeleteMapping("/trash/empty")
    public Result<Void> emptyTrash(HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        emailService.emptyTrash(currentUserId);
        return Result.success("垃圾箱已清空", null);
    }
}