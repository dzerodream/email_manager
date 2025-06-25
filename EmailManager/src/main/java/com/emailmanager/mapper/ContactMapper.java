package com.emailmanager.mapper;

import com.emailmanager.entity.Contact;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 联系人Mapper接口
 */
@Mapper
public interface ContactMapper {
    /**
     * 插入联系人
     */
    int insert(Contact contact);

    /**
     * 根据ID查询联系人
     */
    Contact findById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 更新联系人
     */
    int update(Contact contact);

    /**
     * 删除联系人
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 查询用户的所有联系人
     */
    List<Contact> findByUserId(Long userId);

    /**
     * 根据姓名搜索联系人
     */
    List<Contact> searchByName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 根据邮箱搜索联系人
     */
    List<Contact> searchByEmail(@Param("userId") Long userId, @Param("email") String email);

    /**
     * 检查邮箱是否已存在
     */
    boolean isEmailExists(@Param("userId") Long userId, @Param("email") String email);

    /**
     * 根据月份查询联系人生日
     */
    List<Contact> findBirthdaysByMonth(@Param("userId") Long userId, @Param("month") int month);

    /**
     * 查询未来一段时间内即将到来的生日
     */
    List<Contact> findUpcomingBirthdays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询今天过生日的联系人
     */
    List<Contact> findTodayBirthdays(@Param("userId") Long userId);
} 