package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentNotificationPageQueryDTO;
import com.edusmart.manager.entity.EduNotificationEntity;
import com.edusmart.manager.entity.EduNotificationReadEntity;
import com.edusmart.manager.mapper.EduNotificationMapper;
import com.edusmart.manager.mapper.EduNotificationReadMapper;
import com.edusmart.manager.service.student.StudentNotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StudentNotificationServiceImpl implements StudentNotificationService {
    private final EduNotificationMapper notificationMapper;
    private final EduNotificationReadMapper notificationReadMapper;

    public StudentNotificationServiceImpl(EduNotificationMapper notificationMapper, EduNotificationReadMapper notificationReadMapper) {
        this.notificationMapper = notificationMapper;
        this.notificationReadMapper = notificationReadMapper;
    }

    @Override
    public PageData<EduNotificationEntity> pageNotifications(StudentNotificationPageQueryDTO queryDTO) {
        QueryWrapper<EduNotificationEntity> w = new QueryWrapper<>();
        w.and(x -> x.eq("target_role", "ALL").or().eq("target_role", "STUDENT"));
        if (queryDTO.getUserId() != null) {
            w.and(x -> x.isNull("target_user_id").or().eq("target_user_id", queryDTO.getUserId()));
        }
        if (queryDTO.getNoticeType() != null && !queryDTO.getNoticeType().isBlank()) {
            w.eq("notice_type", queryDTO.getNoticeType());
        }
        w.eq("status", "PUBLISHED");
        w.orderByDesc("id");
        Page<EduNotificationEntity> p = notificationMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), p.getRecords());
    }

    @Override
    public EduNotificationEntity getNotification(Long id) {
        return notificationMapper.selectById(id);
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        QueryWrapper<EduNotificationReadEntity> w = new QueryWrapper<>();
        w.eq("notification_id", notificationId).eq("user_id", userId);
        EduNotificationReadEntity read = notificationReadMapper.selectOne(w);
        if (read == null) {
            read = new EduNotificationReadEntity();
            read.setNotificationId(notificationId);
            read.setUserId(userId);
            read.setIsRead(1);
            read.setReadAt(LocalDateTime.now());
            notificationReadMapper.insert(read);
            return;
        }
        read.setIsRead(1);
        read.setReadAt(LocalDateTime.now());
        notificationReadMapper.updateById(read);
    }
}
