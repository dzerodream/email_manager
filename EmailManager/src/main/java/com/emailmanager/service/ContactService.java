package com.emailmanager.service;

import com.emailmanager.entity.Contact;
import java.time.LocalDate;
import java.util.List;

public interface ContactService {
    /**
     * 添加联系人
     * @param contact 联系人信息
     * @throws RuntimeException 如果邮箱已存在
     */
    void addContact(Contact contact); // 返回类型改为 void

    /**
     * 更新联系人信息
     * @param contact 联系人信息
     * @throws RuntimeException 如果联系人不存在或邮箱已存在
     */
    void updateContact(Contact contact); // 返回类型改为 void

    /**
     * 删除联系人
     * @param id 联系人ID
     */
    void deleteContact(Long id, Long userId);

    /**
     * 根据ID查询联系人
     * @param id 联系人ID
     * @return 联系人信息
     */
    Contact findById(Long id, Long userId);

    /**
     * 查询用户的所有联系人
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<Contact> findByUserId(Long userId);

    /**
     * 根据姓名搜索联系人
     * @param userId 用户ID
     * @param name 姓名关键词
     * @return 联系人列表
     */
    List<Contact> searchByName(Long userId, String name);

    /**
     * 根据邮箱搜索联系人
     * @param userId 用户ID
     * @param email 邮箱关键词
     * @return 联系人列表
     */
    List<Contact> searchByEmail(Long userId, String email);

    /**
     * 检查邮箱是否已存在于联系人中
     * @param userId 用户ID
     * @param email 邮箱
     * @return 是否存在
     */
    boolean isEmailExists(Long userId, String email);

    /**
     * 导入联系人
     * @param userId 用户ID
     * @param contacts 联系人列表
     * @return 成功导入的数量
     */
    int importContacts(Long userId, List<Contact> contacts);

    /**
     * 导出联系人
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<Contact> exportContacts(Long userId);

    /**
     * 根据月份查询联系人生日
     * @param userId 用户ID
     * @param month 月份 (1-12)
     * @return 联系人列表
     * @throws IllegalArgumentException 如果月份不在1-12之间
     */
    List<Contact> findBirthdaysByMonth(Long userId, int month);

    /**
     * 查询未来一段时间内即将到来的生日
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 联系人列表
     * @throws IllegalArgumentException 如果开始日期晚于结束日期
     */
    List<Contact> findUpcomingBirthdays(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询今天过生日的联系人
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<Contact> findTodayBirthdays(Long userId);
}