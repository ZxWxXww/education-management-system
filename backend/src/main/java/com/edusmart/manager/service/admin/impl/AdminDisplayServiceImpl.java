package com.edusmart.manager.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.dto.admin.AdminDisplayConfigDTO;
import com.edusmart.manager.dto.admin.AdminDisplayPreviewItemDTO;
import com.edusmart.manager.dto.admin.AdminDisplaySaveDTO;
import com.edusmart.manager.entity.EduDisplaySettingEntity;
import com.edusmart.manager.entity.EduOperationLogEntity;
import com.edusmart.manager.entity.EduSystemSettingEntity;
import com.edusmart.manager.mapper.EduDisplaySettingMapper;
import com.edusmart.manager.mapper.EduOperationLogMapper;
import com.edusmart.manager.mapper.EduSystemSettingMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.admin.AdminDisplayService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminDisplayServiceImpl implements AdminDisplayService {
    private static final String SYSTEM_SCOPE = "SYSTEM";
    private static final String KEY_SITE_TITLE = "site_title";
    private static final String KEY_SITE_SUBTITLE = "site_subtitle";
    private static final String KEY_LOGIN_NOTICE = "login_notice";
    private static final String KEY_BRAND_COLOR = "brand_color";
    private static final String KEY_LOCALE = "locale";

    private final EduDisplaySettingMapper displaySettingMapper;
    private final EduSystemSettingMapper systemSettingMapper;
    private final EduOperationLogMapper operationLogMapper;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;

    public AdminDisplayServiceImpl(
            EduDisplaySettingMapper displaySettingMapper,
            EduSystemSettingMapper systemSettingMapper,
            EduOperationLogMapper operationLogMapper,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper
    ) {
        this.displaySettingMapper = displaySettingMapper;
        this.systemSettingMapper = systemSettingMapper;
        this.operationLogMapper = operationLogMapper;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminDisplayConfigDTO getCurrent() {
        ensureDefaults();
        EduDisplaySettingEntity display = getSystemDisplay();
        Map<String, EduSystemSettingEntity> settingMap = getDisplaySettings();
        return buildConfig(display, settingMap);
    }

    @Override
    public void save(AdminDisplaySaveDTO dto) {
        ensureDefaults();
        upsertSetting(KEY_SITE_TITLE, valueOrDefault(dto.getSiteTitle(), defaultDto().getSiteTitle()), "display", "站点标题");
        upsertSetting(KEY_SITE_SUBTITLE, valueOrDefault(dto.getSiteSubtitle(), defaultDto().getSiteSubtitle()), "display", "站点副标题");
        upsertSetting(KEY_LOGIN_NOTICE, valueOrDefault(dto.getLoginNotice(), defaultDto().getLoginNotice()), "display", "登录公告");
        upsertSetting(KEY_BRAND_COLOR, valueOrDefault(dto.getBrandColor(), defaultDto().getBrandColor()), "display", "品牌主色");
        upsertSetting(KEY_LOCALE, valueOrDefault(dto.getLocale(), defaultDto().getLocale()), "display", "语言环境");

        EduDisplaySettingEntity display = getSystemDisplay();
        display.setThemeMode(Boolean.TRUE.equals(dto.getDarkModeDefault()) ? "DARK" : "LIGHT");
        display.setLayoutMode(valueOrDefault(dto.getDashboardLayout(), defaultDto().getDashboardLayout()));
        display.setCustomJson(writeCustomJson(dto));
        if (display.getId() == null) {
            display.setScope(SYSTEM_SCOPE);
            displaySettingMapper.insert(display);
        } else {
            displaySettingMapper.updateById(display);
        }

        writeLog("保存展示设置", "DISPLAY_SETTING", String.valueOf(display.getId()), "SUCCESS",
                "更新站点标题、主题、布局和首页组件展示项");
    }

    @Override
    public AdminDisplayConfigDTO reset() {
        AdminDisplaySaveDTO dto = defaultDto();
        save(dto);
        writeLog("恢复默认展示设置", "DISPLAY_SETTING", SYSTEM_SCOPE, "SUCCESS", "恢复展示配置默认值");
        return getCurrent();
    }

    private void ensureDefaults() {
        List<EduSystemSettingEntity> settings = systemSettingMapper.selectList(new QueryWrapper<EduSystemSettingEntity>()
                .in("setting_key", List.of(KEY_SITE_TITLE, KEY_SITE_SUBTITLE, KEY_LOGIN_NOTICE, KEY_BRAND_COLOR, KEY_LOCALE)));
        Map<String, EduSystemSettingEntity> settingMap = settings.stream()
                .collect(Collectors.toMap(EduSystemSettingEntity::getSettingKey, item -> item, (left, right) -> left));
        AdminDisplaySaveDTO defaults = defaultDto();
        if (!settingMap.containsKey(KEY_SITE_TITLE)) {
            upsertSetting(KEY_SITE_TITLE, defaults.getSiteTitle(), "display", "站点标题");
        }
        if (!settingMap.containsKey(KEY_SITE_SUBTITLE)) {
            upsertSetting(KEY_SITE_SUBTITLE, defaults.getSiteSubtitle(), "display", "站点副标题");
        }
        if (!settingMap.containsKey(KEY_LOGIN_NOTICE)) {
            upsertSetting(KEY_LOGIN_NOTICE, defaults.getLoginNotice(), "display", "登录公告");
        }
        if (!settingMap.containsKey(KEY_BRAND_COLOR)) {
            upsertSetting(KEY_BRAND_COLOR, defaults.getBrandColor(), "display", "品牌主色");
        }
        if (!settingMap.containsKey(KEY_LOCALE)) {
            upsertSetting(KEY_LOCALE, defaults.getLocale(), "display", "语言环境");
        }

        EduDisplaySettingEntity display = getSystemDisplay();
        if (display.getId() == null) {
            display.setScope(SYSTEM_SCOPE);
            display.setThemeMode("DARK");
            display.setLayoutMode(defaults.getDashboardLayout());
            display.setCustomJson(writeCustomJson(defaults));
            displaySettingMapper.insert(display);
        }

        if (operationLogMapper.selectCount(new QueryWrapper<>()) == 0) {
            writeLog("初始化系统展示配置", "SYSTEM_SETTING", SYSTEM_SCOPE, "SUCCESS", "首次初始化机构设置与展示设置默认值");
        }
    }

    private Map<String, EduSystemSettingEntity> getDisplaySettings() {
        return systemSettingMapper.selectList(new QueryWrapper<EduSystemSettingEntity>()
                        .in("setting_key", List.of(KEY_SITE_TITLE, KEY_SITE_SUBTITLE, KEY_LOGIN_NOTICE, KEY_BRAND_COLOR, KEY_LOCALE)))
                .stream()
                .collect(Collectors.toMap(EduSystemSettingEntity::getSettingKey, item -> item, (left, right) -> left));
    }

    private EduDisplaySettingEntity getSystemDisplay() {
        EduDisplaySettingEntity display = displaySettingMapper.selectOne(new QueryWrapper<EduDisplaySettingEntity>().eq("scope", SYSTEM_SCOPE).last("limit 1"));
        return display == null ? new EduDisplaySettingEntity() : display;
    }

    private AdminDisplayConfigDTO buildConfig(EduDisplaySettingEntity display, Map<String, EduSystemSettingEntity> settingMap) {
        Map<String, Object> customMap = parseCustomJson(display.getCustomJson());
        AdminDisplayConfigDTO dto = new AdminDisplayConfigDTO();
        dto.setSiteTitle(getSettingValue(settingMap, KEY_SITE_TITLE, defaultDto().getSiteTitle()));
        dto.setSiteSubtitle(getSettingValue(settingMap, KEY_SITE_SUBTITLE, defaultDto().getSiteSubtitle()));
        dto.setDarkModeDefault("DARK".equalsIgnoreCase(display.getThemeMode()));
        dto.setDashboardLayout(valueOrDefault(display.getLayoutMode(), defaultDto().getDashboardLayout()));
        dto.setShowAttendanceWidget(Boolean.TRUE.equals(customMap.getOrDefault("showAttendanceWidget", defaultDto().getShowAttendanceWidget())));
        dto.setShowFinanceWidget(Boolean.TRUE.equals(customMap.getOrDefault("showFinanceWidget", defaultDto().getShowFinanceWidget())));
        dto.setLoginNotice(getSettingValue(settingMap, KEY_LOGIN_NOTICE, defaultDto().getLoginNotice()));
        dto.setBrandColor(getSettingValue(settingMap, KEY_BRAND_COLOR, defaultDto().getBrandColor()));
        dto.setLocale(getSettingValue(settingMap, KEY_LOCALE, defaultDto().getLocale()));
        dto.setPreviewRecords(buildPreviewRecords(dto, display, settingMap));
        return dto;
    }

    private List<AdminDisplayPreviewItemDTO> buildPreviewRecords(
            AdminDisplayConfigDTO dto,
            EduDisplaySettingEntity display,
            Map<String, EduSystemSettingEntity> settingMap
    ) {
        List<AdminDisplayPreviewItemDTO> items = new ArrayList<>();
        AdminDisplayPreviewItemDTO widget = new AdminDisplayPreviewItemDTO();
        widget.setTitle("首页组件");
        widget.setValue((Boolean.TRUE.equals(dto.getShowAttendanceWidget()) ? "考勤看板启用" : "考勤看板关闭")
                + " / "
                + (Boolean.TRUE.equals(dto.getShowFinanceWidget()) ? "财务看板启用" : "财务看板关闭"));
        widget.setCreatedAt(display.getCreatedAt());
        widget.setUpdatedAt(display.getUpdatedAt());
        items.add(widget);

        AdminDisplayPreviewItemDTO branding = new AdminDisplayPreviewItemDTO();
        branding.setTitle("品牌与公告");
        branding.setValue(dto.getSiteTitle() + " / " + dto.getBrandColor());
        LocalDateTime createdAt = settingMap.values().stream().map(EduSystemSettingEntity::getCreatedAt).filter(v -> v != null).min(LocalDateTime::compareTo).orElse(display.getCreatedAt());
        LocalDateTime updatedAt = settingMap.values().stream().map(EduSystemSettingEntity::getUpdatedAt).filter(v -> v != null).max(LocalDateTime::compareTo).orElse(display.getUpdatedAt());
        branding.setCreatedAt(createdAt);
        branding.setUpdatedAt(updatedAt);
        items.add(branding);
        return items;
    }

    private String getSettingValue(Map<String, EduSystemSettingEntity> settingMap, String key, String defaultValue) {
        EduSystemSettingEntity setting = settingMap.get(key);
        return setting == null || setting.getSettingValue() == null || setting.getSettingValue().isBlank()
                ? defaultValue
                : setting.getSettingValue();
    }

    private void upsertSetting(String key, String value, String group, String description) {
        EduSystemSettingEntity entity = systemSettingMapper.selectOne(new QueryWrapper<EduSystemSettingEntity>().eq("setting_key", key).last("limit 1"));
        if (entity == null) {
            entity = new EduSystemSettingEntity();
            entity.setSettingKey(key);
            entity.setSettingValue(value);
            entity.setSettingGroup(group);
            entity.setDescription(description);
            systemSettingMapper.insert(entity);
        } else {
            entity.setSettingValue(value);
            entity.setSettingGroup(group);
            entity.setDescription(description);
            systemSettingMapper.updateById(entity);
        }
    }

    private String writeCustomJson(AdminDisplaySaveDTO dto) {
        try {
            Map<String, Object> json = new HashMap<>();
            json.put("showAttendanceWidget", Boolean.TRUE.equals(dto.getShowAttendanceWidget()));
            json.put("showFinanceWidget", Boolean.TRUE.equals(dto.getShowFinanceWidget()));
            return objectMapper.writeValueAsString(json);
        } catch (Exception e) {
            return "{\"showAttendanceWidget\":true,\"showFinanceWidget\":true}";
        }
    }

    private Map<String, Object> parseCustomJson(String raw) {
        if (raw == null || raw.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void writeLog(String action, String targetType, String targetId, String result, String detail) {
        EduOperationLogEntity entity = new EduOperationLogEntity();
        entity.setUserId(currentUserService.getCurrentUserId());
        entity.setModule("机构设置");
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
        return request == null ? "-" : valueOrDefault(request.getRemoteAddr(), "-");
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private AdminDisplaySaveDTO defaultDto() {
        AdminDisplaySaveDTO dto = new AdminDisplaySaveDTO();
        dto.setSiteTitle("教培智管系统");
        dto.setSiteSubtitle("让教学管理更高效");
        dto.setDarkModeDefault(true);
        dto.setDashboardLayout("card-grid");
        dto.setShowAttendanceWidget(true);
        dto.setShowFinanceWidget(true);
        dto.setLoginNotice("系统将于每周日 23:00 进行维护，请提前保存数据。");
        dto.setBrandColor("#2563EB");
        dto.setLocale("zh-CN");
        return dto;
    }
}
