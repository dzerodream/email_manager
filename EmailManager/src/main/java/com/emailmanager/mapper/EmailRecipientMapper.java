package com.emailmanager.mapper;

import com.emailmanager.entity.EmailRecipient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EmailRecipientMapper {
    /**
     * 批量插入收件人记录
     * @param recipients 收件人列表
     */
    void batchInsert(@Param("recipients") List<EmailRecipient> recipients);

    /**
     * 根据邮件ID查询所有收件人
     * @param emailId 邮件ID
     * @return 收件人列表
     */
    List<EmailRecipient> findByEmailId(@Param("emailId") Long emailId);
}