package com.edusmart.manager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;
import com.edusmart.manager.entity.EduClassSessionEntity;
import com.edusmart.manager.entity.EduHourDeductionRecordEntity;
import com.edusmart.manager.entity.EduOrderEntity;
import com.edusmart.manager.entity.EduOrderItemEntity;
import com.edusmart.manager.entity.EduStudentHourPackageEntity;
import com.edusmart.manager.mapper.EduClassSessionMapper;
import com.edusmart.manager.mapper.EduHourDeductionRecordMapper;
import com.edusmart.manager.mapper.EduOrderItemMapper;
import com.edusmart.manager.mapper.EduOrderMapper;
import com.edusmart.manager.mapper.EduStudentHourPackageMapper;
import com.edusmart.manager.service.HourPackageLedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class HourPackageLedgerServiceImpl implements HourPackageLedgerService {
    private static final Set<String> DEDUCTIBLE_ATTENDANCE_STATUSES = Set.of("PRESENT", "LATE");

    private final EduOrderMapper orderMapper;
    private final EduOrderItemMapper orderItemMapper;
    private final EduStudentHourPackageMapper studentHourPackageMapper;
    private final EduHourDeductionRecordMapper hourDeductionRecordMapper;
    private final EduClassSessionMapper classSessionMapper;

    public HourPackageLedgerServiceImpl(EduOrderMapper orderMapper,
                                        EduOrderItemMapper orderItemMapper,
                                        EduStudentHourPackageMapper studentHourPackageMapper,
                                        EduHourDeductionRecordMapper hourDeductionRecordMapper,
                                        EduClassSessionMapper classSessionMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.studentHourPackageMapper = studentHourPackageMapper;
        this.hourDeductionRecordMapper = hourDeductionRecordMapper;
        this.classSessionMapper = classSessionMapper;
    }

    @Override
    @Transactional
    public void issuePackagesForCompletedOrder(Long orderId, LocalDate effectiveDate) {
        reconcilePackagesForOrder(orderId, effectiveDate);
    }

    @Override
    @Transactional
    public void reconcilePackagesForOrder(Long orderId, LocalDate effectiveDate) {
        if (orderId == null) {
            return;
        }
        EduOrderEntity order = orderMapper.selectById(orderId);
        if (order == null || !"HOUR_PACKAGE".equalsIgnoreCase(order.getOrderType())) {
            return;
        }

        List<EduOrderItemEntity> items = orderItemMapper.selectList(
                new QueryWrapper<EduOrderItemEntity>().eq("order_id", orderId).orderByAsc("id")
        );
        if (items.isEmpty()) {
            return;
        }

        Map<Long, BigDecimal> courseHourMap = new HashMap<>();
        for (EduOrderItemEntity item : items) {
            if (item.getCourseId() == null || item.getHourCount() == null || item.getHourCount().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            courseHourMap.merge(item.getCourseId(), item.getHourCount(), BigDecimal::add);
        }
        if (courseHourMap.isEmpty()) {
            return;
        }

        Map<Long, EduStudentHourPackageEntity> packageMap = new HashMap<>();
        studentHourPackageMapper.selectList(new QueryWrapper<EduStudentHourPackageEntity>().eq("order_id", orderId).orderByAsc("id"))
                .forEach(item -> {
                    if (item.getCourseId() != null) {
                        packageMap.putIfAbsent(item.getCourseId(), item);
                    }
                });

        LocalDate businessDate = effectiveDate == null ? LocalDate.now() : effectiveDate;
        BigDecimal payRatio = resolveOrderPayRatio(order);
        for (Map.Entry<Long, BigDecimal> entry : courseHourMap.entrySet()) {
            BigDecimal totalHours = entry.getValue();
            BigDecimal entitledHours = totalHours.multiply(payRatio).setScale(2, RoundingMode.HALF_UP);
            EduStudentHourPackageEntity hourPackage = packageMap.get(entry.getKey());
            if (hourPackage == null) {
                if (entitledHours.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                EduStudentHourPackageEntity entity = new EduStudentHourPackageEntity();
                entity.setStudentId(order.getStudentId());
                entity.setCourseId(entry.getKey());
                entity.setOrderId(orderId);
                entity.setTotalHours(totalHours);
                entity.setUsedHours(BigDecimal.ZERO);
                entity.setRemainingHours(entitledHours);
                entity.setEffectiveDate(businessDate);
                entity.setStatus(resolvePackageStatus(entity, businessDate));
                studentHourPackageMapper.insert(entity);
                continue;
            }

            BigDecimal usedHours = defaultAmount(hourPackage.getUsedHours());
            if (usedHours.compareTo(entitledHours) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "退款后应保留课时小于已扣课课时，无法回收");
            }

            hourPackage.setTotalHours(totalHours);
            if (hourPackage.getEffectiveDate() == null) {
                hourPackage.setEffectiveDate(businessDate);
            }
            hourPackage.setRemainingHours(entitledHours.subtract(usedHours));
            hourPackage.setStatus(resolvePackageStatus(hourPackage, businessDate));
            studentHourPackageMapper.updateById(hourPackage);
        }
    }

    @Override
    @Transactional
    public void refreshAttendanceDeduction(EduAttendanceRecordEntity attendanceRecord) {
        rollbackAttendanceDeduction(attendanceRecord);
        if (attendanceRecord == null || attendanceRecord.getId() == null || !isDeductibleStatus(attendanceRecord.getStatus())) {
            return;
        }
        if (attendanceRecord.getSessionId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "出勤记录缺少课节，无法扣课");
        }

        EduClassSessionEntity session = classSessionMapper.selectById(attendanceRecord.getSessionId());
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "课节不存在");
        }
        if (session.getCourseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "课节未绑定课程，无法扣课");
        }
        BigDecimal plannedHours = defaultAmount(session.getPlannedHours());
        if (plannedHours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "课节计划课时无效，无法扣课");
        }

        LocalDate businessDate = attendanceRecord.getAttendanceDate() == null ? LocalDate.now() : attendanceRecord.getAttendanceDate();
        List<EduStudentHourPackageEntity> packages = studentHourPackageMapper.selectList(
                new QueryWrapper<EduStudentHourPackageEntity>()
                        .eq("student_id", attendanceRecord.getStudentId())
                        .eq("course_id", session.getCourseId())
                        .orderByAsc("effective_date")
                        .orderByAsc("id")
        );

        BigDecimal remainingToDeduct = plannedHours;
        for (EduStudentHourPackageEntity hourPackage : packages) {
            normalizeExpiredPackage(hourPackage, businessDate);
            BigDecimal availableHours = defaultAmount(hourPackage.getRemainingHours());
            if (!"ACTIVE".equalsIgnoreCase(hourPackage.getStatus())
                    || availableHours.compareTo(BigDecimal.ZERO) <= 0
                    || !isPackageEffective(hourPackage, businessDate)) {
                continue;
            }

            BigDecimal deductHours = remainingToDeduct.min(availableHours);
            if (deductHours.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            hourPackage.setUsedHours(defaultAmount(hourPackage.getUsedHours()).add(deductHours));
            hourPackage.setRemainingHours(availableHours.subtract(deductHours));
            hourPackage.setStatus(resolvePackageStatus(hourPackage, businessDate));
            studentHourPackageMapper.updateById(hourPackage);

            EduHourDeductionRecordEntity deductionRecord = new EduHourDeductionRecordEntity();
            deductionRecord.setHourPackageId(hourPackage.getId());
            deductionRecord.setStudentId(attendanceRecord.getStudentId());
            deductionRecord.setClassId(attendanceRecord.getClassId());
            deductionRecord.setSessionId(attendanceRecord.getSessionId());
            deductionRecord.setDeductHours(deductHours);
            deductionRecord.setDeductType("CLASS_ATTEND");
            deductionRecord.setBizDate(businessDate);
            deductionRecord.setRemark(buildAttendanceRemark(attendanceRecord.getId()));
            hourDeductionRecordMapper.insert(deductionRecord);

            remainingToDeduct = remainingToDeduct.subtract(deductHours);
            if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }

        if (remainingToDeduct.compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "课时余额不足，无法完成扣课");
        }
    }

    @Override
    @Transactional
    public void rollbackAttendanceDeduction(EduAttendanceRecordEntity attendanceRecord) {
        if (attendanceRecord == null || attendanceRecord.getId() == null) {
            return;
        }

        List<EduHourDeductionRecordEntity> records = hourDeductionRecordMapper.selectList(
                new QueryWrapper<EduHourDeductionRecordEntity>()
                        .eq("deduct_type", "CLASS_ATTEND")
                        .eq("remark", buildAttendanceRemark(attendanceRecord.getId()))
                        .orderByAsc("id")
        );
        if (records.isEmpty()) {
            return;
        }

        LocalDate businessDate = attendanceRecord.getAttendanceDate() == null ? LocalDate.now() : attendanceRecord.getAttendanceDate();
        for (EduHourDeductionRecordEntity record : records) {
            EduStudentHourPackageEntity hourPackage = studentHourPackageMapper.selectById(record.getHourPackageId());
            if (hourPackage != null) {
                hourPackage.setUsedHours(defaultAmount(hourPackage.getUsedHours()).subtract(defaultAmount(record.getDeductHours())));
                hourPackage.setRemainingHours(defaultAmount(hourPackage.getRemainingHours()).add(defaultAmount(record.getDeductHours())));
                if (hourPackage.getUsedHours().compareTo(BigDecimal.ZERO) < 0) {
                    hourPackage.setUsedHours(BigDecimal.ZERO);
                }
                hourPackage.setStatus(resolvePackageStatus(hourPackage, businessDate));
                studentHourPackageMapper.updateById(hourPackage);
            }
            hourDeductionRecordMapper.deleteById(record.getId());
        }
    }

    private boolean isDeductibleStatus(String status) {
        return StringUtils.hasText(status) && DEDUCTIBLE_ATTENDANCE_STATUSES.contains(status.trim().toUpperCase());
    }

    private String buildAttendanceRemark(Long attendanceRecordId) {
        return "ATTENDANCE_RECORD:" + attendanceRecordId;
    }

    private BigDecimal resolveOrderPayRatio(EduOrderEntity order) {
        BigDecimal totalAmount = defaultAmount(order.getAmountTotal());
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal paidAmount = defaultAmount(order.getAmountPaid());
        if (paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (paidAmount.compareTo(totalAmount) >= 0) {
            return BigDecimal.ONE;
        }
        return paidAmount.divide(totalAmount, 8, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private boolean isPackageEffective(EduStudentHourPackageEntity hourPackage, LocalDate businessDate) {
        return (hourPackage.getEffectiveDate() == null || !hourPackage.getEffectiveDate().isAfter(businessDate))
                && (hourPackage.getExpireDate() == null || !hourPackage.getExpireDate().isBefore(businessDate));
    }

    private void normalizeExpiredPackage(EduStudentHourPackageEntity hourPackage, LocalDate businessDate) {
        if (hourPackage == null || hourPackage.getExpireDate() == null || !hourPackage.getExpireDate().isBefore(businessDate)) {
            return;
        }
        if (!Objects.equals(hourPackage.getStatus(), "EXPIRED")) {
            hourPackage.setStatus("EXPIRED");
            studentHourPackageMapper.updateById(hourPackage);
        }
    }

    private String resolvePackageStatus(EduStudentHourPackageEntity hourPackage, LocalDate businessDate) {
        if (hourPackage.getExpireDate() != null && hourPackage.getExpireDate().isBefore(businessDate)) {
            return "EXPIRED";
        }
        if (defaultAmount(hourPackage.getRemainingHours()).compareTo(BigDecimal.ZERO) <= 0) {
            return "CLOSED";
        }
        return "ACTIVE";
    }
}
