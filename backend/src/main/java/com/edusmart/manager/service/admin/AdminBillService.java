package com.edusmart.manager.service.admin;

import com.edusmart.manager.common.PageData;
import com.edusmart.manager.dto.admin.AdminBillPageItemDTO;
import com.edusmart.manager.dto.admin.AdminBillPaymentItemDTO;
import com.edusmart.manager.dto.admin.AdminBillPaymentSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillRefundSaveDTO;
import com.edusmart.manager.dto.admin.AdminBillStudentOptionDTO;
import com.edusmart.manager.dto.admin.BillPageQueryDTO;
import com.edusmart.manager.dto.admin.BillSaveDTO;

import java.util.List;

public interface AdminBillService {
    PageData<AdminBillPageItemDTO> page(BillPageQueryDTO queryDTO);

    AdminBillPageItemDTO getById(Long id);

    List<AdminBillStudentOptionDTO> listStudentOptions();

    Long create(BillSaveDTO dto);

    void update(Long id, BillSaveDTO dto);

    void delete(Long id);

    Long registerPayment(Long id, AdminBillPaymentSaveDTO dto);

    Long registerRefund(Long id, AdminBillRefundSaveDTO dto);

    List<AdminBillPaymentItemDTO> listPayments(Long id);
}
