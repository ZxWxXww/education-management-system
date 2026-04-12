package com.edusmart.manager.service.teacher;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchTaskPageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchTaskPageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceBatchUpdateDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageItemDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendancePageQueryDTO;
import com.edusmart.manager.dto.teacher.TeacherAttendanceSaveDTO;
import com.edusmart.manager.dto.teacher.TeacherBatchAttendanceSaveDTO;

public interface TeacherAttendanceService {
    PageData<TeacherAttendancePageItemDTO> page(TeacherAttendancePageQueryDTO queryDTO);
    TeacherAttendancePageItemDTO getById(Long id);
    Long create(TeacherAttendanceSaveDTO dto);
    void update(Long id, TeacherAttendanceSaveDTO dto);
    void batchUpdate(TeacherAttendanceBatchUpdateDTO dto);
    void delete(Long id);
    Long createBatchTask(TeacherBatchAttendanceSaveDTO dto);
    TeacherAttendanceBatchTaskPageItemDTO getBatchTask(Long id);
    PageData<TeacherAttendanceBatchTaskPageItemDTO> pageBatchTasks(TeacherAttendanceBatchTaskPageQueryDTO queryDTO);
}
