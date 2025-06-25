package com.emailmanager.mapper;

import com.emailmanager.entity.Mailbox;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MailboxMapper {
    int insert(Mailbox mailbox);
    int update(Mailbox mailbox);
    Mailbox findByUserIdAndEmailId(@Param("userId") Long userId, @Param("emailId") Long emailId);
    List<Mailbox> findByUserIdAndFolder(@Param("userId") Long userId, @Param("folder") String folder);
    List<Mailbox> findStarredByUserId(@Param("userId") Long userId);
    int deleteByEmailId(@Param("emailId") Long emailId);
    List<Mailbox> findSentMailboxEntriesByUserId(@Param("userId") Long userId);
    List<Mailbox> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    // --- 新增的方法 ---
    Mailbox findById(Long id);

    int deleteById(Long id);

    void deleteByUserIdAndFolder(@Param("userId") Long userId, @Param("folder") String folder);

    List<Mailbox> findAllByUserIdAndEmailId(@Param("userId") Long userId, @Param("emailId") Long emailId);

}