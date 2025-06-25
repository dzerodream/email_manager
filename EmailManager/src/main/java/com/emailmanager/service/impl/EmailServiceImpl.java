package com.emailmanager.service.impl;

import com.emailmanager.entity.*;
import com.emailmanager.mapper.*;
import com.emailmanager.service.EmailService;
import com.emailmanager.service.SpamRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EmailServiceImpl implements EmailService {

    private final EmailMapper emailMapper;
    private final MailboxMapper mailboxMapper;
    private final EmailAttachmentMapper emailAttachmentMapper;
    private final UserMapper userMapper;
    private final SpamRuleService spamRuleService;
    private final EmailRecipientMapper emailRecipientMapper;

    @Autowired
    public EmailServiceImpl(EmailMapper emailMapper, MailboxMapper mailboxMapper,
                            EmailAttachmentMapper emailAttachmentMapper, UserMapper userMapper,
                            SpamRuleService spamRuleService, EmailRecipientMapper emailRecipientMapper) {
        this.emailMapper = emailMapper;
        this.mailboxMapper = mailboxMapper;
        this.emailAttachmentMapper = emailAttachmentMapper;
        this.userMapper = userMapper;
        this.spamRuleService = spamRuleService;
        this.emailRecipientMapper = emailRecipientMapper;
    }

    private Mailbox getMailboxEntryForUser(Long userId, Long mailboxId) {
        if (userId == null || mailboxId == null) {
            return null;
        }
        Mailbox entry = mailboxMapper.findById(mailboxId);
        if (entry == null || !entry.getUserId().equals(userId)) {
            return null;
        }
        return entry;
    }

    private EmailRecipient createRecipient(Long emailId, String email, String type) {
        EmailRecipient recipient = new EmailRecipient();
        recipient.setEmailId(emailId);
        recipient.setRecipientEmail(email);
        recipient.setRecipientType(type);
        return recipient;
    }

    @Override
    @Transactional
    public Long createDraft(Email email, List<String> toEmails, List<String> ccEmails, List<String> bccEmails, List<EmailAttachment> attachments) {
        email.setStatus("DRAFT");
        emailMapper.insert(email);
        Long emailId = email.getId();

        Mailbox senderEntry = new Mailbox();
        senderEntry.setUserId(email.getSenderId());
        senderEntry.setEmailId(emailId);
        senderEntry.setFolder("DRAFT");
        senderEntry.setIsRead(1);
        senderEntry.setIsStarred(0);
        mailboxMapper.insert(senderEntry);

        if (attachments != null && !attachments.isEmpty()) {
            for (EmailAttachment attachment : attachments) {
                attachment.setEmailId(emailId);
                emailAttachmentMapper.insert(attachment);
            }
        }
        return emailId;
    }

    @Override
    @Transactional
    public boolean sendEmail(Long emailId, Long senderId, List<String> toEmails, List<String> ccEmails, List<String> bccEmails) {
        Mailbox draftEntry = mailboxMapper.findByUserIdAndEmailId(senderId, emailId);
        if (draftEntry == null || !"DRAFT".equals(draftEntry.getFolder())) {
            // 如果不是从草稿发送，而是直接发送新邮件，需要确保发件人有一条SENT记录
            // 或者，我们也可以在这里为发件人创建SENT记录（如果不存在的话）
            // 但当前的流程是从草稿发送，所以draftEntry应该存在
            if (draftEntry == null) { // 可能是直接调用发送，而不是从草稿
                draftEntry = new Mailbox();
                draftEntry.setUserId(senderId);
                draftEntry.setEmailId(emailId);
                draftEntry.setIsRead(1); // 自己发的邮件默认已读
                draftEntry.setIsStarred(0);
                mailboxMapper.insert(draftEntry); // 插入新的
            }
        }

        draftEntry.setFolder("SENT");
        draftEntry.setUpdatedTime(LocalDateTime.now()); // 明确更新时间为发送时间
        mailboxMapper.update(draftEntry);

        Email emailCore = emailMapper.findWithAttachmentsById(emailId);
        User sender = userMapper.findById(senderId);
        if (emailCore == null || sender == null) {
            throw new IllegalStateException("数据不一致：找不到邮件或发件人核心内容");
        }

        // 记录正式的收件人信息
        List<EmailRecipient> recipients = new ArrayList<>();
        if (toEmails != null && !toEmails.isEmpty()) {
            toEmails.forEach(email -> recipients.add(createRecipient(emailId, email, "TO")));
        }
        if (ccEmails != null && !ccEmails.isEmpty()) {
            ccEmails.forEach(email -> recipients.add(createRecipient(emailId, email, "CC")));
        }
        if (bccEmails != null && !bccEmails.isEmpty()) {
            bccEmails.forEach(email -> recipients.add(createRecipient(emailId, email, "BCC")));
        }
        if (!recipients.isEmpty()) {
            emailRecipientMapper.batchInsert(recipients);
        }

        Set<String> allRecipientEmails = new HashSet<>();
        if (toEmails != null) allRecipientEmails.addAll(toEmails);
        if (ccEmails != null) allRecipientEmails.addAll(ccEmails);
        // Bcc通常不显示给其他收件人，但在系统内部处理时需要知道
        if (bccEmails != null) allRecipientEmails.addAll(bccEmails);

        for (String emailAddress : allRecipientEmails) {
            User recipientUser = userMapper.findByEmail(emailAddress);
            if (recipientUser != null) { // 系统内用户
                boolean isSpam = spamRuleService.isSpam(recipientUser.getId(), sender.getEmail(), emailCore.getSubject(), emailCore.getContent());
                String targetFolder = isSpam ? "TRASH" : "INBOX";

                // --- 核心修改在这里 ---
                // 检查该收件人是否已经有了这条邮件的 INBOX/TRASH 记录
                // 注意：我们不能用 findByUserIdAndEmailId，因为它可能返回 SENT 或 DRAFT 记录
                // 需要一个更精确的查询，或者在这里做判断

                Mailbox existingRecipientMailbox = null;
                // 尝试查找该用户是否已有此邮件的 INBOX 或 TRASH 记录
                List<Mailbox> userMailboxesForThisEmail = mailboxMapper.findAllByUserIdAndEmailId(recipientUser.getId(), emailId);
                for(Mailbox mb : userMailboxesForThisEmail) {
                    if ("INBOX".equals(mb.getFolder()) || "TRASH".equals(mb.getFolder())) {
                        existingRecipientMailbox = mb;
                        break;
                    }
                }

                if (existingRecipientMailbox == null) {
                    // 如果发件人就是收件人，我们仍然要为他创建一条 INBOX/TRASH 记录
                    // 这与他已有的 SENT 记录是不同的mailbox条目
                    Mailbox recipientEntry = new Mailbox();
                    recipientEntry.setUserId(recipientUser.getId());
                    recipientEntry.setEmailId(emailId);
                    recipientEntry.setFolder(targetFolder);
                    recipientEntry.setIsRead(0); // 发给自己的邮件，在收件箱里初始也应是未读
                    recipientEntry.setIsStarred(0);
                    mailboxMapper.insert(recipientEntry);
                } else {
                    // 如果已经存在INBOX/TRASH记录（例如，用户先删除了又被重新发送，或被多次抄送）
                    // 可以选择更新它（比如更新时间），或者什么都不做
                    // 为简单起见，我们先假设如果存在就不再重复创建
                    // 但如果邮件内容更新了，这里可能需要更复杂的逻辑
                }

            } else { // 外部邮箱
                System.out.println("向外部邮箱 " + emailAddress + " 发送邮件...");
                // 这里将来集成真正的邮件发送服务
            }
        }

        Email emailToUpdate = new Email();
        emailToUpdate.setId(emailId);
        emailToUpdate.setStatus("SENT");
        emailMapper.update(emailToUpdate); // 标记邮件核心状态为已发送

        return true;
    }

    @Override
    @Transactional
    public boolean moveToTrash(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null || "TRASH".equals(entry.getFolder())) {
            return false;
        }
        entry.setFolder("TRASH");
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean restoreFromTrash(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null || !"TRASH".equals(entry.getFolder())) {
            return false;
        }
        Email emailCore = emailMapper.findWithAttachmentsById(entry.getEmailId());
        if (emailCore == null) {
            throw new IllegalStateException("数据不一致：找不到邮件核心内容");
        }
        if ("DRAFT".equals(emailCore.getStatus())) {
            entry.setFolder("DRAFT");
        } else if (emailCore.getSenderId().equals(userId)) {
            entry.setFolder("SENT");
        } else {
            entry.setFolder("INBOX");
        }
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean permanentDeleteEmail(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null) {
            return false;
        }
        // 注意：由于外键约束ON DELETE CASCADE，删除emails表的记录会自动删除所有关联的mailbox和recipients记录。
        // 但如果只想删除此用户邮箱中的条目，而不影响邮件本身和其他收件人，则只删除mailbox记录。
        // 目前我们的逻辑是只删除mailbox条目。
        return mailboxMapper.deleteById(entry.getId()) > 0;
    }

    @Override
    public Email getEmailDetails(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null) {
            return null;
        }
        if ("INBOX".equals(entry.getFolder()) && entry.getIsRead() == 0) {
            markAsRead(userId, mailboxId);
        }
        return emailMapper.findWithAttachmentsById(entry.getEmailId());
    }

    @Override
    @Transactional
    public boolean starEmail(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null) return false;
        entry.setIsStarred(1);
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean unstarEmail(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null) return false;
        entry.setIsStarred(0);
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean markAsRead(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null || entry.getIsRead() == 1) return false;
        entry.setIsRead(1);
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean markAsUnread(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null || entry.getIsRead() == 0) return false;
        entry.setIsRead(0);
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean archiveEmail(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null || "ARCHIVE".equals(entry.getFolder()) || "TRASH".equals(entry.getFolder())) {
            return false;
        }
        entry.setFolder("ARCHIVE");
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    @Transactional
    public boolean unarchiveEmail(Long userId, Long mailboxId) {
        Mailbox entry = getMailboxEntryForUser(userId, mailboxId);
        if (entry == null || !"ARCHIVE".equals(entry.getFolder())) {
            return false;
        }
        Email emailCore = emailMapper.findWithAttachmentsById(entry.getEmailId());
        if (emailCore == null) {
            throw new IllegalStateException("数据不一致：找不到邮件核心内容");
        }
        if ("DRAFT".equals(emailCore.getStatus())) {
            entry.setFolder("DRAFT");
        } else if (emailCore.getSenderId().equals(userId)) {
            entry.setFolder("SENT");
        } else {
            entry.setFolder("INBOX");
        }
        return mailboxMapper.update(entry) > 0;
    }

    @Override
    public List<Mailbox> getInboxEmails(Long userId) {
        return mailboxMapper.findByUserIdAndFolder(userId, "INBOX");
    }

    @Override
    public List<Mailbox> getSentEmails(Long userId) {
        return mailboxMapper.findSentMailboxEntriesByUserId(userId);
    }

    @Override
    public List<Mailbox> getDraftEmails(Long userId) {
        return mailboxMapper.findByUserIdAndFolder(userId, "DRAFT");
    }

    @Override
    public List<Mailbox> getTrashEmails(Long userId) {
        return mailboxMapper.findByUserIdAndFolder(userId, "TRASH");
    }

    @Override
    public List<Mailbox> getArchivedEmails(Long userId) {
        return mailboxMapper.findByUserIdAndFolder(userId, "ARCHIVE");
    }

    @Override
    public List<Mailbox> getStarredEmails(Long userId) {
        return mailboxMapper.findStarredByUserId(userId);
    }

    @Override
    public List<Mailbox> searchEmails(Long userId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return mailboxMapper.searchByKeyword(userId, keyword);
    }

    @Override
    @Transactional
    public void emptyTrash(Long userId) {
        // 这里我们只删除mailbox条目，邮件核心内容和附件是否删除取决于业务逻辑。
        // 目前的设计是保留，这样如果其他收件人还在，邮件不会丢失。
        mailboxMapper.deleteByUserIdAndFolder(userId, "TRASH");
    }
}