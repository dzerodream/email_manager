// 文件路径: com/emailmanager/service/impl/ContactServiceImpl.java

package com.emailmanager.service.impl;

import com.emailmanager.entity.Contact;
import com.emailmanager.mapper.ContactMapper;
import com.emailmanager.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactMapper contactMapper;

    @Override
    @Transactional
    public void addContact(Contact contact) {
        if (contactMapper.isEmailExists(contact.getUserId(), contact.getEmail())) {
            throw new RuntimeException("该邮箱已存在于您的通讯录中");
        }
        contact.setCreatedTime(LocalDateTime.now());
        contact.setUpdatedTime(LocalDateTime.now());
        contactMapper.insert(contact);
    }

    // --- 这是修正后的 updateContact 方法 ---
    @Override
    @Transactional
    public void updateContact(Contact contact) {
        // 1. 验证权限并获取旧数据
        // 使用我们加固后的findById方法，确保要更新的联系人存在且属于当前用户
        Contact existingContact = contactMapper.findById(contact.getId(), contact.getUserId());
        if (existingContact == null) {
            throw new RuntimeException("联系人不存在或无权修改");
        }

        // 2. 检查邮箱唯一性
        // 只有当用户试图修改邮箱时，才需要检查新邮箱是否与通讯录中其他联系人冲突
        if (contact.getEmail() != null && !contact.getEmail().equals(existingContact.getEmail())) {
            if (contactMapper.isEmailExists(contact.getUserId(), contact.getEmail())) {
                throw new RuntimeException("您要修改的邮箱地址已存在于其他联系人中");
            }
        }

        // 3. 执行更新
        // 为了安全，不应该让前端传入的contact对象直接覆盖所有字段
        // 而是将需要更新的字段设置到从数据库查出的existingContact对象上
        if (contact.getName() != null) {
            existingContact.setName(contact.getName());
        }
        if (contact.getEmail() != null) {
            existingContact.setEmail(contact.getEmail());
        }
        if (contact.getPhone() != null) {
            existingContact.setPhone(contact.getPhone());
        }
        if (contact.getBirthday() != null) {
            existingContact.setBirthday(contact.getBirthday());
        }
        if (contact.getRemark() != null) {
            existingContact.setRemark(contact.getRemark());
        }

        // 设置更新时间
        existingContact.setUpdatedTime(LocalDateTime.now());

        // 使用包含所有字段的existingContact对象进行更新，
        // 确保了那些不允许前端修改的字段（如userId）不会被篡改。
        contactMapper.update(existingContact);
    }

    // --- 这是修正后的 deleteContact 方法 ---
    @Override
    @Transactional
    public void deleteContact(Long id, Long userId) {
        int affectedRows = contactMapper.deleteById(id, userId);
        if (affectedRows == 0) {
            throw new RuntimeException("删除失败，联系人不存在或无权操作");
        }
    }

    // --- 这是修正后的 findById 方法 ---
    @Override
    public Contact findById(Long id, Long userId) {
        return contactMapper.findById(id, userId);
    }

    // --- 其他方法保持不变 ---

    @Override
    public List<Contact> findByUserId(Long userId) {
        return contactMapper.findByUserId(userId);
    }

    @Override
    public List<Contact> searchByName(Long userId, String name) {
        return contactMapper.searchByName(userId, name);
    }

    @Override
    public List<Contact> searchByEmail(Long userId, String email) {
        return contactMapper.searchByEmail(userId, email);
    }

    @Override
    public boolean isEmailExists(Long userId, String email) {
        return contactMapper.isEmailExists(userId, email);
    }

    @Override
    @Transactional
    public int importContacts(Long userId, List<Contact> contacts) {
        // ... (此方法逻辑保持不变) ...
        int successCount = 0;
        for (Contact contact : contacts) {
            try {
                contact.setUserId(userId);
                if (!isEmailExists(userId, contact.getEmail())) {
                    contact.setCreatedTime(LocalDateTime.now());
                    contact.setUpdatedTime(LocalDateTime.now());
                    contactMapper.insert(contact);
                    successCount++;
                }
            } catch (Exception e) {
                System.err.println("导入联系人失败: " + contact.getEmail() + ", 错误: " + e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    public List<Contact> exportContacts(Long userId) {
        return contactMapper.findByUserId(userId);
    }

    @Override
    public List<Contact> findBirthdaysByMonth(Long userId, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("月份必须在1-12之间");
        }
        return contactMapper.findBirthdaysByMonth(userId, month);
    }

    @Override
    public List<Contact> findUpcomingBirthdays(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return contactMapper.findUpcomingBirthdays(userId, startDate, endDate);
    }

    @Override
    public List<Contact> findTodayBirthdays(Long userId) {
        return contactMapper.findTodayBirthdays(userId);
    }
}