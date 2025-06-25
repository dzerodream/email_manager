package com.emailmanager.service;

import com.emailmanager.entity.Email;
import com.emailmanager.entity.EmailAttachment;
import com.emailmanager.entity.Mailbox;
import java.util.List;

public interface EmailService {

    Long createDraft(Email email, List<String> toEmails, List<String> ccEmails, List<String> bccEmails, List<EmailAttachment> attachments);
    boolean sendEmail(Long emailId, Long senderId, List<String> toEmails, List<String> ccEmails, List<String> bccEmails);

    boolean moveToTrash(Long userId, Long mailboxId);
    boolean restoreFromTrash(Long userId, Long mailboxId);
    boolean permanentDeleteEmail(Long userId, Long mailboxId);

    Email getEmailDetails(Long userId, Long mailboxId);

    boolean starEmail(Long userId, Long mailboxId);
    boolean unstarEmail(Long userId, Long mailboxId);
    boolean markAsRead(Long userId, Long mailboxId);
    boolean markAsUnread(Long userId, Long mailboxId);
    boolean archiveEmail(Long userId, Long mailboxId);
    boolean unarchiveEmail(Long userId, Long mailboxId);

    List<Mailbox> getInboxEmails(Long userId);
    List<Mailbox> getSentEmails(Long userId);
    List<Mailbox> getDraftEmails(Long userId);
    List<Mailbox> getTrashEmails(Long userId);
    List<Mailbox> getArchivedEmails(Long userId);
    List<Mailbox> getStarredEmails(Long userId);
    List<Mailbox> searchEmails(Long userId, String keyword);

    void emptyTrash(Long userId);

}