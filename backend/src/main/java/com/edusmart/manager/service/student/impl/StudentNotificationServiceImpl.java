package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentNotificationItemDTO;
import com.edusmart.manager.dto.student.StudentNotificationPageQueryDTO;
import com.edusmart.manager.entity.EduNotificationEntity;
import com.edusmart.manager.entity.EduNotificationReadEntity;
import com.edusmart.manager.mapper.EduNotificationMapper;
import com.edusmart.manager.mapper.EduNotificationReadMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentNotificationServiceImpl implements StudentNotificationService {
    private final EduNotificationMapper notificationMapper;
    private final EduNotificationReadMapper notificationReadMapper;
    private final CurrentUserService currentUserService;

    public StudentNotificationServiceImpl(
            EduNotificationMapper notificationMapper,
            EduNotificationReadMapper notificationReadMapper,
            CurrentUserService currentUserService
    ) {
        this.notificationMapper = notificationMapper;
        this.notificationReadMapper = notificationReadMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentNotificationItemDTO> pageNotifications(StudentNotificationPageQueryDTO queryDTO) {
        Long currentUserId = currentUserService.getCurrentUserId();
        QueryWrapper<EduNotificationEntity> w = new QueryWrapper<>();
        w.and(x -> x.eq("target_role", "ALL").or().eq("target_role", "STUDENT"));
        w.and(x -> x.isNull("target_user_id").or().eq("target_user_id", currentUserId));
        if (queryDTO.getNoticeType() != null && !queryDTO.getNoticeType().isBlank()) {
            w.eq("notice_type", queryDTO.getNoticeType());
        }
        w.eq("status", "PUBLISHED");
        w.orderByDesc("id");
        Page<EduNotificationEntity> p = notificationMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), w);
        Map<Long, EduNotificationReadEntity> readMap = listReadMap(currentUserId, p.getRecords());
        List<StudentNotificationItemDTO> records = p.getRecords().stream()
                .map(item -> toNotificationItem(item, readMap.containsKey(item.getId())))
                .filter(item -> queryDTO.getIsRead() == null || item.isRead() == (queryDTO.getIsRead() == 1))
                .collect(Collectors.toList());
        return new PageData<>(p.getCurrent(), p.getSize(), p.getTotal(), records);
    }

    @Override
    public StudentNotificationItemDTO getNotification(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        EduNotificationEntity notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new ResponseStatusException(NOT_FOUND, "通知不存在");
        }
        validateAccessible(notification, currentUserId);
        EduNotificationReadEntity read = notificationReadMapper.selectOne(
                new QueryWrapper<EduNotificationReadEntity>()
                        .eq("notification_id", id)
                        .eq("user_id", currentUserId)
        );
        return toNotificationItem(notification, read != null && Integer.valueOf(1).equals(read.getIsRead()));
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        EduNotificationEntity notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new ResponseStatusException(NOT_FOUND, "通知不存在");
        }
        validateAccessible(notification, currentUserId);
        QueryWrapper<EduNotificationReadEntity> w = new QueryWrapper<>();
        w.eq("notification_id", notificationId).eq("user_id", currentUserId);
        EduNotificationReadEntity read = notificationReadMapper.selectOne(w);
        if (read == null) {
            read = new EduNotificationReadEntity();
            read.setNotificationId(notificationId);
            read.setUserId(currentUserId);
            read.setIsRead(1);
            read.setReadAt(LocalDateTime.now());
            notificationReadMapper.insert(read);
            return;
        }
        read.setIsRead(1);
        read.setReadAt(LocalDateTime.now());
        notificationReadMapper.updateById(read);
    }

    private Map<Long, EduNotificationReadEntity> listReadMap(Long currentUserId, List<EduNotificationEntity> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return Map.of();
        }
        List<Long> notificationIds = notifications.stream().map(EduNotificationEntity::getId).collect(Collectors.toList());
        return notificationReadMapper.selectList(new QueryWrapper<EduNotificationReadEntity>()
                        .eq("user_id", currentUserId)
                        .eq("is_read", 1)
                        .in("notification_id", notificationIds))
                .stream()
                .collect(Collectors.toMap(EduNotificationReadEntity::getNotificationId, Function.identity(), (left, right) -> right));
    }

    private StudentNotificationItemDTO toNotificationItem(EduNotificationEntity notification, boolean isRead) {
        StudentNotificationItemDTO item = new StudentNotificationItemDTO();
        item.setId(notification.getId());
        item.setTitle(notification.getTitle());
        item.setContent(notification.getContent());
        item.setNoticeType(notification.getNoticeType());
        item.setBizRefType(notification.getBizRefType());
        item.setBizRefId(notification.getBizRefId());
        item.setPublishTime(notification.getPublishTime());
        item.setRead(isRead);
        return item;
    }

    private void validateAccessible(EduNotificationEntity notification, Long currentUserId) {
        boolean roleAllowed = "ALL".equalsIgnoreCase(notification.getTargetRole()) || "STUDENT".equalsIgnoreCase(notification.getTargetRole());
        boolean userAllowed = notification.getTargetUserId() == null || notification.getTargetUserId().equals(currentUserId);
        if (!roleAllowed || !userAllowed) {
            throw new ResponseStatusException(NOT_FOUND, "通知不存在");
        }
    }
}
