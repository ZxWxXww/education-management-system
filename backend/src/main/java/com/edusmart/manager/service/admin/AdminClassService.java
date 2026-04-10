package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.ClassPageQueryDTO;
import com.edusmart.manager.dto.admin.ClassSaveDTO;
import com.edusmart.manager.entity.EduClassEntity;

public interface AdminClassService {
    PageData<EduClassEntity> page(ClassPageQueryDTO queryDTO);

    EduClassEntity getById(Long id);

    Long create(ClassSaveDTO dto);

    void update(Long id, ClassSaveDTO dto);

    void delete(Long id);
}
