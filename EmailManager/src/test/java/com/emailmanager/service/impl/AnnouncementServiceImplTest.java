// 文件路径: com/emailmanager/service/impl/AnnouncementServiceImplTest.java
package com.emailmanager.service.impl;

import com.emailmanager.entity.Announcement;
import com.emailmanager.mapper.AnnouncementMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @Mock
    private AnnouncementMapper announcementMapper;

    @InjectMocks
    private AnnouncementServiceImpl announcementService;

    @Test
    @DisplayName("发布公告 - 应设置默认状态和时间")
    void testPublishAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setTitle("Test Title");

        announcementService.publishAnnouncement(announcement);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).insert(captor.capture());

        Announcement captured = captor.getValue();
        assertEquals(1, captured.getStatus());
        assertNotNull(captured.getPublishTime());
        assertNotNull(captured.getCreatedTime());
        assertNotNull(captured.getUpdatedTime());
    }

    @Test
    @DisplayName("更新公告 - 应更新时间戳")
    void testUpdateAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setId(1L);

        announcementService.updateAnnouncement(announcement);

        ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        verify(announcementMapper).update(captor.capture());

        assertNotNull(captor.getValue().getUpdatedTime());
    }
}