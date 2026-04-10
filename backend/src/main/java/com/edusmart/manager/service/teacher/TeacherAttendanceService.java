package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceSaveDTO;
import com.edusmart.manager.dto.teacher.TeacherBatchAttendanceSaveDTO;
import com.edusmart.manager.entity.EduAttendanceBatchTaskEntity;
import com.edusmart.manager.entity.EduAttendanceRecordEntity;

public interface TeacherAttendanceService {
    PageData<EduAttendanceRecordEntity> page(TeacherAttendancePageQueryDTO queryDTO);
    EduAttendanceRecordEntity getById(Long id);
    Long create(TeacherAttendanceSaveDTO dto);
    void update(Long id, TeacherAttendanceSaveDTO dto);
    void delete(Long id);
    Long createBatchTask(TeacherBatchAttendanceSaveDTO dto);
    EduAttendanceBatchTaskEntity getBatchTask(Long id);
}
