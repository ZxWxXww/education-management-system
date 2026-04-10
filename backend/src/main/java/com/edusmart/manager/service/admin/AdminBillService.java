package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.dto.admin.BillSaveDTO;
import com.edusmart.manager.entity.EduBillEntity;

public interface AdminBillService {
    PageData<EduBillEntity> page(BillPageQueryDTO queryDTO);

    EduBillEntity getById(Long id);

    Long create(BillSaveDTO dto);

    void update(Long id, BillSaveDTO dto);

    void delete(Long id);
}
