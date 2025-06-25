// 文件路径: com/emailmanager/service/impl/EmailServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.Email;
import com.emailmanager.entity.EmailAttachment;
import com.emailmanager.entity.Mailbox;
import com.emailmanager.entity.User;
import com.emailmanager.mapper.EmailAttachmentMapper;
import com.emailmanager.mapper.EmailMapper;
import com.emailmanager.mapper.MailboxMapper;
import com.emailmanager.mapper.UserMapper;
import com.emailmanager.service.SpamRuleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * EmailServiceImpl 的最终完整单元测试类
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock private MailboxMapper mailboxMapper;
    @Mock private EmailMapper emailMapper;
    @Mock private EmailAttachmentMapper emailAttachmentMapper;
    @Mock private UserMapper userMapper;
    @Mock private SpamRuleService spamRuleService;

    @InjectMocks
    private EmailServiceImpl emailService;

    // --- createDraft ---
    @Test
    @DisplayName("创建草稿 - 成功")
    void testCreateDraft_Success() {
        // Arrange
        Email email = new Email();
        email.setSenderId(1L);
        List<EmailAttachment> attachments = Arrays.asList(new EmailAttachment());

        doAnswer(invocation -> {
            invocation.getArgument(0, Email.class).setId(100L);
            return 1;
        }).when(emailMapper).insert(any(Email.class));

        // Act
        Long draftId = emailService.createDraft(email, null, null, null, attachments);

        // Assert
        assertEquals(100L, draftId);

        verify(emailMapper, times(1)).insert(any(Email.class));
        verify(mailboxMapper, times(1)).insert(any(Mailbox.class));
        verify(emailAttachmentMapper, times(1)).insert(any(EmailAttachment.class));
    }

    // --- sendEmail ---
    @Test
    @DisplayName("发送邮件 - 成功，收件人为内部用户且非垃圾邮件")
    void testSendEmail_Success_ToInbox() {
        Long emailId = 100L, senderId = 1L, recipientId = 2L;
        String recipientEmail = "recipient@example.com";

        when(mailboxMapper.findByUserIdAndEmailId(senderId, emailId)).thenReturn(new Mailbox(null, senderId, emailId, "DRAFT", 1, 0, null, null, null));
        when(emailMapper.findWithAttachmentsById(emailId)).thenReturn(new Email());
        when(userMapper.findById(senderId)).thenReturn(new User());
        when(userMapper.findByEmail(recipientEmail)).thenReturn(new User(recipientId, null, null, null, null, null, null, null, 1, null, null, null));
        when(spamRuleService.isSpam(anyLong(), any(), any(), any())).thenReturn(false); // 不是垃圾邮件

        boolean result = emailService.sendEmail(emailId, senderId, Arrays.asList(recipientEmail), null, null);

        assertTrue(result);
        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxMapper, times(1)).insert(captor.capture());
        assertEquals("INBOX", captor.getValue().getFolder());
        verify(emailMapper, times(1)).update(any(Email.class));
    }

    @Test
    @DisplayName("发送邮件 - 成功，收件人为内部用户且为垃圾邮件")
    void testSendEmail_Success_ToTrash() {
        Long emailId = 101L, senderId = 1L, recipientId = 2L;
        String recipientEmail = "recipient@example.com";

        when(mailboxMapper.findByUserIdAndEmailId(senderId, emailId)).thenReturn(new Mailbox(null, senderId, emailId, "DRAFT", 1, 0, null, null, null));
        when(emailMapper.findWithAttachmentsById(emailId)).thenReturn(new Email());
        when(userMapper.findById(senderId)).thenReturn(new User());
        when(userMapper.findByEmail(recipientEmail)).thenReturn(new User(recipientId, null, null, null, null, null, null, null, 1, null, null, null));
        when(spamRuleService.isSpam(anyLong(), any(), any(), any())).thenReturn(true); // 是垃圾邮件

        emailService.sendEmail(emailId, senderId, Arrays.asList(recipientEmail), null, null);

        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxMapper).insert(captor.capture());
        assertEquals("TRASH", captor.getValue().getFolder());
    }

    @Test
    @DisplayName("发送邮件 - 失败（不是草稿）")
    void testSendEmail_Failure_NotADraft() {
        when(mailboxMapper.findByUserIdAndEmailId(1L, 102L)).thenReturn(new Mailbox(null, 1L, 102L, "SENT", 1, 0, null, null, null));
        assertThrows(RuntimeException.class, () -> emailService.sendEmail(102L, 1L, null, null, null));
    }

    // --- 邮件管理操作 ---

    @Test
    @DisplayName("移动到垃圾箱")
    void testMoveToTrash() {
        Mailbox entry = new Mailbox();
        entry.setFolder("INBOX");
        when(mailboxMapper.findByUserIdAndEmailId(1L, 1L)).thenReturn(entry);
        emailService.moveToTrash(1L, 1L);
        verify(mailboxMapper).update(entry);
        assertEquals("TRASH", entry.getFolder());
    }

    @Test
    @DisplayName("从垃圾箱恢复 - 到收件箱")
    void testRestoreFromTrash_ToInbox() {
        Mailbox entry = new Mailbox();
        entry.setFolder("TRASH");
        Email email = new Email();
        email.setSenderId(2L); // 发件人不是当前用户
        email.setStatus("SENT");

        when(mailboxMapper.findByUserIdAndEmailId(1L, 1L)).thenReturn(entry);
        when(emailMapper.findWithAttachmentsById(1L)).thenReturn(email);

        emailService.restoreFromTrash(1L, 1L);
        verify(mailboxMapper).update(entry);
        assertEquals("INBOX", entry.getFolder());
    }

    @Test
    @DisplayName("永久删除")
    void testPermanentDeleteEmail() {
        Mailbox entry = new Mailbox();
        entry.setId(101L);
        when(mailboxMapper.findByUserIdAndEmailId(1L, 1L)).thenReturn(entry);
        when(mailboxMapper.deleteById(101L)).thenReturn(1);
        assertTrue(emailService.permanentDeleteEmail(1L, 1L));
        verify(mailboxMapper).deleteById(101L);
    }

    @Test
    @DisplayName("星标邮件")
    void testStarEmail() {
        Mailbox entry = new Mailbox();
        entry.setIsStarred(0);
        when(mailboxMapper.findByUserIdAndEmailId(1L, 1L)).thenReturn(entry);
        emailService.starEmail(1L, 1L);
        ArgumentCaptor<Mailbox> captor = ArgumentCaptor.forClass(Mailbox.class);
        verify(mailboxMapper).update(captor.capture());
        assertEquals(1, captor.getValue().getIsStarred());
    }

    // --- 邮件列表查询 ---

    @Test
    @DisplayName("获取收件箱列表")
    void testGetInboxEmails() {
        Long userId = 1L;
        when(mailboxMapper.findByUserIdAndFolder(userId, "INBOX")).thenReturn(Collections.singletonList(new Mailbox()));
        assertEquals(1, emailService.getInboxEmails(userId).size());
        verify(mailboxMapper).findByUserIdAndFolder(userId, "INBOX");
    }

    @Test
    @DisplayName("获取已发送列表")
    void testGetSentEmails() {
        Long userId = 1L;
        when(mailboxMapper.findSentMailboxEntriesByUserId(userId)).thenReturn(Arrays.asList(new Mailbox(), new Mailbox()));
        assertEquals(2, emailService.getSentEmails(userId).size());
        verify(mailboxMapper).findSentMailboxEntriesByUserId(userId);
    }

    @Test
    @DisplayName("搜索邮件")
    void testSearchEmails() {
        Long userId = 1L;
        String keyword = "test";
        when(mailboxMapper.searchByKeyword(userId, keyword)).thenReturn(Collections.singletonList(new Mailbox()));
        List<Mailbox> result = emailService.searchEmails(userId, keyword);
        assertFalse(result.isEmpty());
        verify(mailboxMapper).searchByKeyword(userId, keyword);
    }

    @Test
    @DisplayName("搜索邮件 - 关键词为空")
    void testSearchEmails_EmptyKeyword() {
        List<Mailbox> result = emailService.searchEmails(1L, "  ");
        assertTrue(result.isEmpty());
        verify(mailboxMapper, never()).searchByKeyword(anyLong(), anyString());
    }
}