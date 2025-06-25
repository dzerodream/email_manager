// 文件路径: com/emailmanager/service/impl/ContactServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.Contact;
import com.emailmanager.mapper.ContactMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private ContactServiceImpl contactService;

    @Test
    @DisplayName("添加联系人 - 成功")
    void testAddContact_Success() {
        Long userId = 1L;
        Contact contact = new Contact(null, userId, "新联系人", "new@contact.com", null, null, null, null, null);
        when(contactMapper.isEmailExists(userId, "new@contact.com")).thenReturn(false);

        contactService.addContact(contact);

        verify(contactMapper).insert(contact);
        assertNotNull(contact.getCreatedTime());
    }

    @Test
    @DisplayName("添加联系人 - 失败（邮箱已存在）")
    void testAddContact_Failure_EmailExists() {
        Long userId = 1L;
        Contact contact = new Contact(null, userId, "重复联系人", "existing@contact.com", null, null, null, null, null);
        when(contactMapper.isEmailExists(userId, "existing@contact.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> contactService.addContact(contact));
        verify(contactMapper, never()).insert(any(Contact.class));
    }

    @Test
    @DisplayName("更新联系人 - 成功")
    void testUpdateContact_Success() {
        Long userId = 1L;
        Long contactId = 2L;
        Contact existingContact = new Contact(contactId, userId, "旧名字", "old@example.com", null, null, null, null, null);
        Contact contactToUpdate = new Contact(contactId, userId, "新名字", "new@example.com", null, null, null, null, null);

        when(contactMapper.findById(contactId, userId)).thenReturn(existingContact);
        when(contactMapper.isEmailExists(userId, "new@example.com")).thenReturn(false);

        contactService.updateContact(contactToUpdate);

        verify(contactMapper).update(any(Contact.class));
    }

    @Test
    @DisplayName("删除联系人 - 成功")
    void testDeleteContact_Success() {
        Long userId = 1L;
        Long contactId = 3L;
        when(contactMapper.deleteById(contactId, userId)).thenReturn(1);

        assertDoesNotThrow(() -> contactService.deleteContact(contactId, userId));
        verify(contactMapper).deleteById(contactId, userId);
    }

    @Test
    @DisplayName("删除联系人 - 失败（无权限或不存在）")
    void testDeleteContact_Failure_NoPermission() {
        Long userId = 1L;
        Long contactId = 99L;
        when(contactMapper.deleteById(contactId, userId)).thenReturn(0);

        assertThrows(RuntimeException.class, () -> contactService.deleteContact(contactId, userId));
    }

    @Test
    @DisplayName("根据月份查询生日 - 成功")
    void testFindBirthdaysByMonth_Success() {
        Long userId = 1L;
        int month = 6;
        when(contactMapper.findBirthdaysByMonth(userId, month)).thenReturn(Collections.singletonList(new Contact()));

        List<Contact> result = contactService.findBirthdaysByMonth(userId, month);

        assertFalse(result.isEmpty());
        verify(contactMapper).findBirthdaysByMonth(userId, month);
    }

    @Test
    @DisplayName("根据月份查询生日 - 失败（月份无效）")
    void testFindBirthdaysByMonth_Failure_InvalidMonth() {
        assertThrows(IllegalArgumentException.class, () -> contactService.findBirthdaysByMonth(1L, 13));
        assertThrows(IllegalArgumentException.class, () -> contactService.findBirthdaysByMonth(1L, 0));
        verify(contactMapper, never()).findBirthdaysByMonth(anyLong(), anyInt());
    }
}