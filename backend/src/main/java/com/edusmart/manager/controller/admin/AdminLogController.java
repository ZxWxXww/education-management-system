package com.edusmart.manager.controller.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.common.Result;
import com.edusmart.manager.dto.admin.AdminLogArchiveStrategyDTO;
import com.edusmart.manager.dto.admin.AdminLogPageItemDTO;
import com.edusmart.manager.dto.admin.AdminLogPageQueryDTO;
import jakarta.servlet.http.HttpServletResponse;
import com.edusmart.manager.service.admin.AdminLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN') and hasAuthority('setting:log:view')")
public class AdminLogController {
    private final AdminLogService adminLogService;

    public AdminLogController(AdminLogService adminLogService) {
        this.adminLogService = adminLogService;
    }

    @PostMapping("/page")
    public Result<PageData<AdminLogPageItemDTO>> page(@RequestBody AdminLogPageQueryDTO queryDTO) {
        return Result.success(adminLogService.page(queryDTO));
    }

    @GetMapping("/archive-strategy")
    public Result<AdminLogArchiveStrategyDTO> archiveStrategy() {
        return Result.success(adminLogService.getArchiveStrategy());
    }

    @PutMapping("/archive-strategy")
    public Result<Void> saveArchiveStrategy(@RequestBody AdminLogArchiveStrategyDTO dto) {
        adminLogService.saveArchiveStrategy(dto);
        return Result.success(null);
    }

    @GetMapping("/export")
    public void export(AdminLogPageQueryDTO queryDTO, HttpServletResponse response) throws IOException {
        byte[] bytes = adminLogService.exportCsv(queryDTO);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''admin-log-export.csv");
        response.getOutputStream().write(bytes);
        response.flushBuffer();
    }
}
