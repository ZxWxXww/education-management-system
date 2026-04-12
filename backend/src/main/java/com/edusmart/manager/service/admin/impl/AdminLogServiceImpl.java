package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminLogArchiveStrategyDTO;
import com.edusmart.manager.dto.admin.AdminLogPageItemDTO;
import com.edusmart.manager.dto.admin.AdminLogPageQueryDTO;
import com.edusmart.manager.entity.EduOperationLogEntity;
import com.edusmart.manager.entity.EduSystemSettingEntity;
import com.edusmart.manager.entity.EduUserEntity;
import com.edusmart.manager.mapper.EduOperationLogMapper;
import com.edusmart.manager.mapper.EduSystemSettingMapper;
import com.edusmart.manager.mapper.EduUserMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.admin.AdminLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class AdminLogServiceImpl implements AdminLogService {
    private static final String KEY_LOG_ARCHIVE_ENABLED = "log_archive_enabled";
    private static final String KEY_LOG_ARCHIVE_RETENTION_DAYS = "log_archive_retention_days";
    private static final int DEFAULT_RETENTION_DAYS = 180;

    private final EduOperationLogMapper operationLogMapper;
    private final EduSystemSettingMapper systemSettingMapper;
    private final EduUserMapper userMapper;
    private final CurrentUserService currentUserService;

    public AdminLogServiceImpl(
            EduOperationLogMapper operationLogMapper,
            EduSystemSettingMapper systemSettingMapper,
            EduUserMapper userMapper,
            CurrentUserService currentUserService
    ) {
        this.operationLogMapper = operationLogMapper;
        this.systemSettingMapper = systemSettingMapper;
        this.userMapper = userMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<AdminLogPageItemDTO> page(AdminLogPageQueryDTO queryDTO) {
        QueryWrapper<EduOperationLogEntity> wrapper = buildWrapper(queryDTO);
        Page<EduOperationLogEntity> page = operationLogMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        List<AdminLogPageItemDTO> items = toPageItems(page.getRecords());
        return new PageData<>(page.getCurrent(), page.getSize(), page.getTotal(), items);
    }

    @Override
    public AdminLogArchiveStrategyDTO getArchiveStrategy() {
        ensureArchiveDefaults();
        EduSystemSettingEntity enabledSetting = getSetting(KEY_LOG_ARCHIVE_ENABLED);
        EduSystemSettingEntity retentionSetting = getSetting(KEY_LOG_ARCHIVE_RETENTION_DAYS);

        AdminLogArchiveStrategyDTO dto = new AdminLogArchiveStrategyDTO();
        dto.setAutoArchiveEnabled(enabledSetting == null || !"false".equalsIgnoreCase(enabledSetting.getSettingValue()));
        dto.setRetentionDays(parseRetentionDays(retentionSetting == null ? null : retentionSetting.getSettingValue()));
        dto.setUpdatedAt(Stream.of(enabledSetting, retentionSetting)
                .filter(Objects::nonNull)
                .map(EduSystemSettingEntity::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        return dto;
    }

    @Override
    public void saveArchiveStrategy(AdminLogArchiveStrategyDTO dto) {
        ensureArchiveDefaults();
        boolean enabled = dto != null && Boolean.TRUE.equals(dto.getAutoArchiveEnabled());
        int retentionDays = dto == null ? DEFAULT_RETENTION_DAYS : parseRetentionDays(String.valueOf(dto.getRetentionDays()));
        upsertSetting(KEY_LOG_ARCHIVE_ENABLED, String.valueOf(enabled), "log", "日志自动归档开关");
        upsertSetting(KEY_LOG_ARCHIVE_RETENTION_DAYS, String.valueOf(retentionDays), "log", "日志保留天数");
        writeLog("保存日志归档策略", "LOG_ARCHIVE_STRATEGY", "SYSTEM", "SUCCESS",
                "自动归档=" + enabled + "，保留天数=" + retentionDays);
    }

    @Override
    public byte[] exportCsv(AdminLogPageQueryDTO queryDTO) {
        List<AdminLogPageItemDTO> items = toPageItems(operationLogMapper.selectList(buildWrapper(queryDTO)));
        StringBuilder csv = new StringBuilder();
        csv.append('\uFEFF');
        csv.append("日志ID,操作人,模块,操作行为,来源IP,等级,详情,创建时间,更新时间\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AdminLogPageItemDTO item : items) {
            csv.append(csvValue(item.getId() == null ? "-" : "LOG-" + String.format("%08d", item.getId()))).append(',');
            csv.append(csvValue(item.getOperator())).append(',');
            csv.append(csvValue(item.getModule())).append(',');
            csv.append(csvValue(item.getAction())).append(',');
            csv.append(csvValue(item.getIp())).append(',');
            csv.append(csvValue(item.getLevel())).append(',');
            csv.append(csvValue(item.getDetail())).append(',');
            csv.append(csvValue(item.getCreatedAt() == null ? "-" : item.getCreatedAt().format(formatter))).append(',');
            csv.append(csvValue(item.getUpdatedAt() == null ? "-" : item.getUpdatedAt().format(formatter))).append('\n');
        }
        writeLog("导出日志", "OPERATION_LOG", "EXPORT", "SUCCESS", "导出日志条数=" + items.size());
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private QueryWrapper<EduOperationLogEntity> buildWrapper(AdminLogPageQueryDTO queryDTO) {
        AdminLogPageQueryDTO safeQuery = queryDTO == null ? new AdminLogPageQueryDTO() : queryDTO;
        QueryWrapper<EduOperationLogEntity> wrapper = new QueryWrapper<>();
        if (safeQuery.getModule() != null && !safeQuery.getModule().isBlank() && !"全部模块".equals(safeQuery.getModule())) {
            wrapper.eq("module", safeQuery.getModule());
        }
        if (safeQuery.getLevel() != null && !safeQuery.getLevel().isBlank() && !"全部等级".equals(safeQuery.getLevel())) {
            wrapper.eq("result", "ERROR".equalsIgnoreCase(safeQuery.getLevel()) ? "FAIL" : "SUCCESS");
        }
        if (safeQuery.getStartDate() != null && !safeQuery.getStartDate().isBlank()) {
            wrapper.ge("created_at", LocalDate.parse(safeQuery.getStartDate()).atStartOfDay());
        }
        if (safeQuery.getEndDate() != null && !safeQuery.getEndDate().isBlank()) {
            wrapper.lt("created_at", LocalDate.parse(safeQuery.getEndDate()).plusDays(1).atStartOfDay());
        }
        if (safeQuery.getKeyword() != null && !safeQuery.getKeyword().isBlank()) {
            String keyword = safeQuery.getKeyword().trim();
            wrapper.and(w -> w.like("action", keyword)
                    .or().like("detail", keyword)
                    .or().like("ip", keyword)
                    .or().like("target_id", keyword));
        }
        wrapper.orderByDesc("created_at", "id");
        return wrapper;
    }

    private List<AdminLogPageItemDTO> toPageItems(List<EduOperationLogEntity> records) {
        Map<Long, EduUserEntity> userMap = records.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(records.stream().map(EduOperationLogEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(EduUserEntity::getId, item -> item, (left, right) -> left));

        return records.stream().map(item -> {
            EduUserEntity user = item.getUserId() == null ? null : userMap.get(item.getUserId());
            AdminLogPageItemDTO dto = new AdminLogPageItemDTO();
            dto.setId(item.getId());
            dto.setOperator(user == null ? "system" : (user.getUsername() != null ? user.getUsername() : user.getRealName()));
            dto.setModule(item.getModule());
            dto.setAction(item.getAction());
            dto.setIp(item.getIp());
            dto.setLevel("FAIL".equalsIgnoreCase(item.getResult()) ? "ERROR" : "INFO");
            dto.setDetail(item.getDetail());
            dto.setCreatedAt(item.getCreatedAt());
            dto.setUpdatedAt(item.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    private void ensureArchiveDefaults() {
        if (getSetting(KEY_LOG_ARCHIVE_ENABLED) == null) {
            upsertSetting(KEY_LOG_ARCHIVE_ENABLED, "true", "log", "日志自动归档开关");
        }
        if (getSetting(KEY_LOG_ARCHIVE_RETENTION_DAYS) == null) {
            upsertSetting(KEY_LOG_ARCHIVE_RETENTION_DAYS, String.valueOf(DEFAULT_RETENTION_DAYS), "log", "日志保留天数");
        }
    }

    private EduSystemSettingEntity getSetting(String key) {
        return systemSettingMapper.selectOne(new QueryWrapper<EduSystemSettingEntity>().eq("setting_key", key).last("limit 1"));
    }

    private void upsertSetting(String key, String value, String group, String description) {
        EduSystemSettingEntity setting = getSetting(key);
        if (setting == null) {
            setting = new EduSystemSettingEntity();
            setting.setSettingKey(key);
            setting.setSettingValue(value);
            setting.setSettingGroup(group);
            setting.setDescription(description);
            systemSettingMapper.insert(setting);
        } else {
            setting.setSettingValue(value);
            setting.setSettingGroup(group);
            setting.setDescription(description);
            systemSettingMapper.updateById(setting);
        }
    }

    private int parseRetentionDays(String rawValue) {
        try {
            int value = Integer.parseInt(rawValue);
            return value > 0 ? value : DEFAULT_RETENTION_DAYS;
        } catch (Exception ignore) {
            return DEFAULT_RETENTION_DAYS;
        }
    }

    private String csvValue(String value) {
        String safe = value == null ? "-" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private void writeLog(String action, String targetType, String targetId, String result, String detail) {
        EduOperationLogEntity entity = new EduOperationLogEntity();
        entity.setUserId(currentUserService.getCurrentUserId());
        entity.setModule("日志管理");
        entity.setAction(action);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        entity.setIp(resolveClientIp());
        entity.setResult(result);
        entity.setDetail(detail);
        operationLogMapper.insert(entity);
    }

    private String resolveClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return request == null ? "-" : (request.getRemoteAddr() == null || request.getRemoteAddr().isBlank() ? "-" : request.getRemoteAddr());
    }
}
