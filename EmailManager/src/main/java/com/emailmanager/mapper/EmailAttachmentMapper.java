package com.emailmanager.mapper;

import com.emailmanager.entity.EmailAttachment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 邮件附件Mapper接口
 */
@Mapper
public interface EmailAttachmentMapper {
    int insert(EmailAttachment attachment);
    EmailAttachment findById(Long id);
    int update(EmailAttachment attachment); // 用于更新附件信息 (现在有 updated_time)
    int deleteById(Long id);
    List<EmailAttachment> findAll();
    List<EmailAttachment> findByEmailId(@Param("emailId") Long emailId);

    /**
     * 根据邮件ID删除所有附件 (新增方法)
     */
    int deleteByEmailId(@Param("emailId") Long emailId);
}
