package com.edusmart.manager.service;

import com.edusmart.manager.entity.EduAttendanceRecordEntity;

import java.time.LocalDate;

public interface HourPackageLedgerService {
    void issuePackagesForCompletedOrder(Long orderId, LocalDate effectiveDate);
    void reconcilePackagesForOrder(Long orderId, LocalDate effectiveDate);
    void refreshAttendanceDeduction(EduAttendanceRecordEntity attendanceRecord);
    void rollbackAttendanceDeduction(EduAttendanceRecordEntity attendanceRecord);
}
