package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.dto.admin.AdminSettingsModuleStatusDTO;
import com.edusmart.manager.dto.admin.AdminSettingsOverviewDTO;
import com.edusmart.manager.entity.EduDisplaySettingEntity;
import com.edusmart.manager.entity.EduOperationLogEntity;
import com.edusmart.manager.entity.EduSystemSettingEntity;
import com.edusmart.manager.mapper.EduDisplaySettingMapper;
import com.edusmart.manager.mapper.EduOperationLogMapper;
import com.edusmart.manager.mapper.EduSystemSettingMapper;
import com.edusmart.manager.service.admin.AdminSettingsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminSettingsServiceImpl implements AdminSettingsService {
    private final EduSystemSettingMapper systemSettingMapper;
    private final EduDisplaySettingMapper displaySettingMapper;
    private final EduOperationLogMapper operationLogMapper;
    private final AdminDisplayServiceImpl adminDisplayService;

    public AdminSettingsServiceImpl(
            EduSystemSettingMapper systemSettingMapper,
            EduDisplaySettingMapper displaySettingMapper,
            EduOperationLogMapper operationLogMapper,
            AdminDisplayServiceImpl adminDisplayService
    ) {
        this.systemSettingMapper = systemSettingMapper;
        this.displaySettingMapper = displaySettingMapper;
        this.operationLogMapper = operationLogMapper;
        this.adminDisplayService = adminDisplayService;
    }

    @Override
    public AdminSettingsOverviewDTO getOverview() {
        adminDisplayService.getCurrent();

        List<EduSystemSettingEntity> settings = systemSettingMapper.selectList(new QueryWrapper<EduSystemSettingEntity>().orderByAsc("created_at"));
        List<EduDisplaySettingEntity> displays = displaySettingMapper.selectList(new QueryWrapper<EduDisplaySettingEntity>().orderByAsc("created_at"));
        List<EduOperationLogEntity> logs = operationLogMapper.selectList(new QueryWrapper<EduOperationLogEntity>().orderByDesc("created_at"));

        AdminSettingsOverviewDTO dto = new AdminSettingsOverviewDTO();
        dto.setSystemSettingCount((long) settings.size());
        dto.setLogStrategyCount(settings.stream()
                .filter(item -> item.getSettingGroup() != null && "log".equalsIgnoreCase(item.getSettingGroup()))
                .count());
        dto.setDisplayTemplateCount((long) displays.size());
        dto.setPendingApprovalCount(0L);
        dto.setModuleStatusList(buildModuleStatus(settings, displays, logs));
        return dto;
    }

    private List<AdminSettingsModuleStatusDTO> buildModuleStatus(
            List<EduSystemSettingEntity> settings,
            List<EduDisplaySettingEntity> displays,
            List<EduOperationLogEntity> logs
    ) {
        List<AdminSettingsModuleStatusDTO> rows = new ArrayList<>();

        AdminSettingsModuleStatusDTO settingRow = new AdminSettingsModuleStatusDTO();
        settingRow.setModule("基础参数");
        settingRow.setOwner("教务处");
        settingRow.setState(settings.isEmpty() ? "待初始化" : "运行中");
        settingRow.setCreatedAt(settings.stream().map(EduSystemSettingEntity::getCreatedAt).filter(v -> v != null).min(java.time.LocalDateTime::compareTo).orElse(null));
        settingRow.setUpdatedAt(settings.stream().map(EduSystemSettingEntity::getUpdatedAt).filter(v -> v != null).max(java.time.LocalDateTime::compareTo).orElse(null));
        rows.add(settingRow);

        AdminSettingsModuleStatusDTO displayRow = new AdminSettingsModuleStatusDTO();
        displayRow.setModule("展示设置");
        displayRow.setOwner("产品组");
        displayRow.setState(displays.isEmpty() ? "待初始化" : "运行中");
        displayRow.setCreatedAt(displays.stream().map(EduDisplaySettingEntity::getCreatedAt).filter(v -> v != null).min(java.time.LocalDateTime::compareTo).orElse(null));
        displayRow.setUpdatedAt(displays.stream().map(EduDisplaySettingEntity::getUpdatedAt).filter(v -> v != null).max(java.time.LocalDateTime::compareTo).orElse(null));
        rows.add(displayRow);

        AdminSettingsModuleStatusDTO logRow = new AdminSettingsModuleStatusDTO();
        logRow.setModule("日志管理");
        logRow.setOwner("运维组");
        logRow.setState(logs.isEmpty() ? "待初始化" : "运行中");
        logRow.setCreatedAt(logs.stream().map(EduOperationLogEntity::getCreatedAt).filter(v -> v != null).min(java.time.LocalDateTime::compareTo).orElse(null));
        logRow.setUpdatedAt(logs.stream().map(EduOperationLogEntity::getUpdatedAt).filter(v -> v != null).max(java.time.LocalDateTime::compareTo).orElse(null));
        rows.add(logRow);

        return rows;
    }
}
