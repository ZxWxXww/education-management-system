package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminClassPageItemDTO;
import com.edusmart.manager.dto.admin.ClassPageQueryDTO;
import com.edusmart.manager.dto.admin.ClassSaveDTO;

public interface AdminClassService {
    PageData<AdminClassPageItemDTO> page(ClassPageQueryDTO queryDTO);

    AdminClassPageItemDTO getById(Long id);

    Long create(ClassSaveDTO dto);

    void update(Long id, ClassSaveDTO dto);

    void delete(Long id);
}
