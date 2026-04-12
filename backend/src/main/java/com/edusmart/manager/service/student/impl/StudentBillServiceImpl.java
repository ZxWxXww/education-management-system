package com.edusmart.manager.service.student.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.student.StudentBillPageItemDTO;
import com.edusmart.manager.dto.student.StudentBillPageQueryDTO;
import com.edusmart.manager.dto.student.StudentHourDeductionItemDTO;
import com.edusmart.manager.dto.student.StudentHourPackageItemDTO;
import com.edusmart.manager.dto.student.StudentHourPackageSummaryDTO;
import com.edusmart.manager.entity.EduBillEntity;
import com.edusmart.manager.entity.EduClassEntity;
import com.edusmart.manager.entity.EduCourseEntity;
import com.edusmart.manager.entity.EduHourDeductionRecordEntity;
import com.edusmart.manager.entity.EduStudentHourPackageEntity;
import com.edusmart.manager.entity.EduStudentProfileEntity;
import com.edusmart.manager.mapper.EduBillMapper;
import com.edusmart.manager.mapper.EduClassMapper;
import com.edusmart.manager.mapper.EduCourseMapper;
import com.edusmart.manager.mapper.EduHourDeductionRecordMapper;
import com.edusmart.manager.mapper.EduStudentHourPackageMapper;
import com.edusmart.manager.mapper.EduStudentProfileMapper;
import com.edusmart.manager.security.CurrentUserService;
import com.edusmart.manager.service.student.StudentBillService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentBillServiceImpl implements StudentBillService {
    private final EduBillMapper billMapper;
    private final EduClassMapper classMapper;
    private final EduStudentProfileMapper studentProfileMapper;
    private final EduCourseMapper courseMapper;
    private final EduStudentHourPackageMapper studentHourPackageMapper;
    private final EduHourDeductionRecordMapper hourDeductionRecordMapper;
    private final CurrentUserService currentUserService;

    public StudentBillServiceImpl(
            EduBillMapper billMapper,
            EduClassMapper classMapper,
            EduStudentProfileMapper studentProfileMapper,
            EduCourseMapper courseMapper,
            EduStudentHourPackageMapper studentHourPackageMapper,
            EduHourDeductionRecordMapper hourDeductionRecordMapper,
            CurrentUserService currentUserService
    ) {
        this.billMapper = billMapper;
        this.classMapper = classMapper;
        this.studentProfileMapper = studentProfileMapper;
        this.courseMapper = courseMapper;
        this.studentHourPackageMapper = studentHourPackageMapper;
        this.hourDeductionRecordMapper = hourDeductionRecordMapper;
        this.currentUserService = currentUserService;
    }

    @Override
    public PageData<StudentBillPageItemDTO> page(StudentBillPageQueryDTO queryDTO) {
        Long studentProfileId = getCurrentStudentProfile().getId();
        LambdaQueryWrapper<EduBillEntity> wrapper = new LambdaQueryWrapper<EduBillEntity>()
                .eq(EduBillEntity::getStudentId, studentProfileId)
                .orderByDesc(EduBillEntity::getId);
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isBlank()) {
            wrapper.eq(EduBillEntity::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getMonth() != null && !queryDTO.getMonth().isBlank()) {
            wrapper.apply("DATE_FORMAT(created_at, '%Y-%m') = {0}", queryDTO.getMonth());
        }
        Page<EduBillEntity> page = billMapper.selectPage(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        return new PageData<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                buildBillItems(page.getRecords())
        );
    }

    @Override
    public StudentBillPageItemDTO getById(Long id) {
        EduBillEntity bill = billMapper.selectById(id);
        if (bill == null || !Objects.equals(bill.getStudentId(), getCurrentStudentProfile().getId())) {
            throw new ResponseStatusException(NOT_FOUND, "账单不存在");
        }
        return buildBillItems(Collections.singletonList(bill)).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "账单不存在"));
    }

    @Override
    public StudentHourPackageSummaryDTO getHourPackageSummary() {
        Long studentProfileId = getCurrentStudentProfile().getId();
        List<EduStudentHourPackageEntity> packages = studentHourPackageMapper.selectList(
                new LambdaQueryWrapper<EduStudentHourPackageEntity>()
                        .eq(EduStudentHourPackageEntity::getStudentId, studentProfileId)
                        .orderByDesc(EduStudentHourPackageEntity::getUpdatedAt)
                        .orderByDesc(EduStudentHourPackageEntity::getId)
        );
        List<EduHourDeductionRecordEntity> deductions = hourDeductionRecordMapper.selectList(
                new LambdaQueryWrapper<EduHourDeductionRecordEntity>()
                        .eq(EduHourDeductionRecordEntity::getStudentId, studentProfileId)
                        .orderByDesc(EduHourDeductionRecordEntity::getBizDate)
                        .orderByDesc(EduHourDeductionRecordEntity::getId)
                        .last("limit 5")
        );

        Map<Long, EduCourseEntity> courseMap = loadCourseMap(packages);
        Map<Long, EduClassEntity> classMap = loadClassMap(deductions);
        Map<Long, EduStudentHourPackageEntity> packageMap = packages.stream()
                .collect(Collectors.toMap(EduStudentHourPackageEntity::getId, item -> item, (left, right) -> left));

        List<StudentHourPackageItemDTO> packageItems = packages.stream().map(item -> {
            StudentHourPackageItemDTO dto = new StudentHourPackageItemDTO();
            dto.setId(item.getId());
            dto.setOrderId(item.getOrderId());
            dto.setCourseId(item.getCourseId());
            dto.setCourseName(resolveCourseName(courseMap.get(item.getCourseId())));
            dto.setTotalHours(item.getTotalHours());
            dto.setUsedHours(item.getUsedHours());
            dto.setRemainingHours(item.getRemainingHours());
            dto.setEffectiveDate(item.getEffectiveDate());
            dto.setExpireDate(item.getExpireDate());
            dto.setStatus(resolvePackageStatus(item));
            dto.setUpdatedAt(item.getUpdatedAt());
            return dto;
        }).toList();

        List<StudentHourDeductionItemDTO> deductionItems = deductions.stream().map(item -> {
            StudentHourDeductionItemDTO dto = new StudentHourDeductionItemDTO();
            dto.setId(item.getId());
            dto.setHourPackageId(item.getHourPackageId());
            dto.setClassId(item.getClassId());
            dto.setSessionId(item.getSessionId());
            dto.setClassName(resolveClassName(classMap.get(item.getClassId())));
            EduStudentHourPackageEntity hourPackage = packageMap.get(item.getHourPackageId());
            dto.setCourseId(hourPackage == null ? null : hourPackage.getCourseId());
            dto.setCourseName(hourPackage == null ? "-" : resolveCourseName(courseMap.get(hourPackage.getCourseId())));
            dto.setDeductHours(item.getDeductHours());
            dto.setDeductType(item.getDeductType());
            dto.setBizDate(item.getBizDate());
            dto.setRemark(item.getRemark());
            dto.setCreatedAt(item.getCreatedAt());
            return dto;
        }).toList();

        StudentHourPackageSummaryDTO summary = new StudentHourPackageSummaryDTO();
        summary.setTotalRemainingHours(packageItems.stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .map(StudentHourPackageItemDTO::getRemainingHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setActivePackageCount((int) packageItems.stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getStatus()))
                .count());
        summary.setPackages(packageItems);
        summary.setDeductions(deductionItems);
        return summary;
    }

    private EduStudentProfileEntity getCurrentStudentProfile() {
        EduStudentProfileEntity studentProfile = studentProfileMapper.selectOne(
                new LambdaQueryWrapper<EduStudentProfileEntity>()
                        .eq(EduStudentProfileEntity::getUserId, currentUserService.getCurrentUserId())
        );
        if (studentProfile == null) {
            throw new ResponseStatusException(NOT_FOUND, "学生档案不存在");
        }
        return studentProfile;
    }

    private List<StudentBillPageItemDTO> buildBillItems(List<EduBillEntity> bills) {
        if (bills == null || bills.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> classIds = bills.stream()
                .map(EduBillEntity::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, EduClassEntity> classMap = classIds.isEmpty()
                ? Collections.emptyMap()
                : classMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));

        return bills.stream().map(bill -> {
            StudentBillPageItemDTO item = new StudentBillPageItemDTO();
            item.setId(bill.getId());
            item.setBillNo(bill.getBillNo());
            item.setStudentId(bill.getStudentId());
            item.setClassId(bill.getClassId());
            item.setBillType(bill.getBillType());
            item.setAmount(bill.getAmount());
            item.setPaidAmount(bill.getPaidAmount());
            item.setStatus(bill.getStatus());
            item.setDueDate(bill.getDueDate());
            item.setRemark(bill.getRemark());
            item.setCreatedAt(bill.getCreatedAt());
            item.setUpdatedAt(bill.getUpdatedAt());
            EduClassEntity clazz = classMap.get(bill.getClassId());
            item.setClassName(clazz == null ? "" : clazz.getClassName());
            return item;
        }).collect(Collectors.toList());
    }

    private Map<Long, EduCourseEntity> loadCourseMap(List<EduStudentHourPackageEntity> packages) {
        List<Long> courseIds = packages.stream()
                .map(EduStudentHourPackageEntity::getCourseId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (courseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(EduCourseEntity::getId, item -> item, (left, right) -> right));
    }

    private Map<Long, EduClassEntity> loadClassMap(List<EduHourDeductionRecordEntity> deductions) {
        List<Long> classIds = deductions.stream()
                .map(EduHourDeductionRecordEntity::getClassId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (classIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return classMapper.selectBatchIds(classIds).stream()
                .collect(Collectors.toMap(EduClassEntity::getId, item -> item, (left, right) -> right));
    }

    private String resolveCourseName(EduCourseEntity course) {
        return course == null ? "-" : course.getCourseName();
    }

    private String resolveClassName(EduClassEntity clazz) {
        return clazz == null ? "-" : clazz.getClassName();
    }

    private String resolvePackageStatus(EduStudentHourPackageEntity hourPackage) {
        if (hourPackage.getExpireDate() != null && hourPackage.getExpireDate().isBefore(LocalDate.now())) {
            return "EXPIRED";
        }
        if (hourPackage.getRemainingHours() == null || hourPackage.getRemainingHours().compareTo(BigDecimal.ZERO) <= 0) {
            return "CLOSED";
        }
        return hourPackage.getStatus();
    }
}
