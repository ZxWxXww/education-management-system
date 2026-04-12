package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.dto.admin.AdminTeachingRecentActivityDTO;
import com.edusmart.manager.dto.admin.AdminTeachingResourceOverviewDTO;
import com.edusmart.manager.entity.EduAssignmentEntity;
import com.edusmart.manager.entity.EduExamEntity;
import com.edusmart.manager.entity.EduExamScoreEntity;
import com.edusmart.manager.entity.EduTeachingResourceEntity;
import com.edusmart.manager.mapper.EduAssignmentMapper;
import com.edusmart.manager.mapper.EduExamMapper;
import com.edusmart.manager.mapper.EduExamScoreMapper;
import com.edusmart.manager.mapper.EduTeachingResourceMapper;
import com.edusmart.manager.service.admin.AdminTeachingResourceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminTeachingResourceServiceImpl implements AdminTeachingResourceService {
    private final EduTeachingResourceMapper teachingResourceMapper;
    private final EduAssignmentMapper assignmentMapper;
    private final EduExamScoreMapper examScoreMapper;
    private final EduExamMapper examMapper;

    public AdminTeachingResourceServiceImpl(
            EduTeachingResourceMapper teachingResourceMapper,
            EduAssignmentMapper assignmentMapper,
            EduExamScoreMapper examScoreMapper,
            EduExamMapper examMapper
    ) {
        this.teachingResourceMapper = teachingResourceMapper;
        this.assignmentMapper = assignmentMapper;
        this.examScoreMapper = examScoreMapper;
        this.examMapper = examMapper;
    }

    @Override
    public AdminTeachingResourceOverviewDTO getOverview() {
        List<EduTeachingResourceEntity> resources = teachingResourceMapper.selectList(new QueryWrapper<EduTeachingResourceEntity>().orderByDesc("updated_at", "id"));
        List<EduAssignmentEntity> assignments = assignmentMapper.selectList(new QueryWrapper<EduAssignmentEntity>().orderByDesc("publish_time", "id"));
        List<EduExamScoreEntity> scores = examScoreMapper.selectList(new QueryWrapper<EduExamScoreEntity>().orderByDesc("publish_time", "id"));
        Map<Long, EduExamEntity> examMap = examMapper.selectBatchIds(
                        scores.stream().map(EduExamScoreEntity::getExamId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduExamEntity::getId, item -> item, (left, right) -> left));

        LocalDateTime anchor = assignments.stream()
                .map(item -> item.getPublishTime() != null ? item.getPublishTime() : item.getCreatedAt())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        LocalDateTime weekStart = anchor.minusDays(6);

        AdminTeachingResourceOverviewDTO dto = new AdminTeachingResourceOverviewDTO();
        dto.setTotalAssignments((long) assignments.size());
        dto.setPublishedThisWeek(assignments.stream()
                .map(item -> item.getPublishTime() != null ? item.getPublishTime() : item.getCreatedAt())
                .filter(Objects::nonNull)
                .filter(time -> !time.isBefore(weekStart) && !time.isAfter(anchor))
                .count());
        dto.setGradeRecords((long) scores.size());
        dto.setAvgScore(scores.isEmpty()
                ? BigDecimal.ZERO
                : scores.stream()
                        .map(EduExamScoreEntity::getScore)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(scores.stream().map(EduExamScoreEntity::getScore).filter(Objects::nonNull).count() == 0 ? 1 : scores.stream().map(EduExamScoreEntity::getScore).filter(Objects::nonNull).count()), 1, RoundingMode.HALF_UP));
        dto.setRecentActivities(buildRecentActivities(resources, assignments, scores, examMap));
        return dto;
    }

    private List<AdminTeachingRecentActivityDTO> buildRecentActivities(
            List<EduTeachingResourceEntity> resources,
            List<EduAssignmentEntity> assignments,
            List<EduExamScoreEntity> scores,
            Map<Long, EduExamEntity> examMap
    ) {
        List<ActivityHolder> all = new ArrayList<>();
        resources.stream().limit(5).forEach(item ->
                all.add(new ActivityHolder("资源", "资源《" + item.getTitle() + "》已更新", item.getUpdatedAt() != null ? item.getUpdatedAt() : item.getCreatedAt())));
        assignments.stream().limit(5).forEach(item ->
                all.add(new ActivityHolder("作业", "作业《" + item.getTitle() + "》已发布", item.getPublishTime() != null ? item.getPublishTime() : item.getCreatedAt())));
        scores.stream().limit(5).forEach(item -> {
            EduExamEntity exam = examMap.get(item.getExamId());
            String examName = exam == null ? "未命名考试" : exam.getExamName();
            all.add(new ActivityHolder("成绩", "考试《" + examName + "》成绩已录入", item.getPublishTime() != null ? item.getPublishTime() : item.getUpdatedAt()));
        });

        return all.stream()
                .filter(item -> item.time() != null)
                .sorted(Comparator.comparing(ActivityHolder::time).reversed())
                .limit(5)
                .map(item -> {
                    AdminTeachingRecentActivityDTO dto = new AdminTeachingRecentActivityDTO();
                    dto.setType(item.type());
                    dto.setTitle(item.title());
                    dto.setTime(formatRelativeTime(item.time()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private String formatRelativeTime(LocalDateTime time) {
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = Math.max(0, duration.toMinutes());
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + "分钟前";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + "小时前";
        }
        long days = duration.toDays();
        return days + "天前";
    }

    private record ActivityHolder(String type, String title, LocalDateTime time) {
    }
}
