package com.edusmart.manager.dto.student;

import java.math.BigDecimal;
import java.util.List;

public class StudentHourPackageSummaryDTO {
    private BigDecimal totalRemainingHours;
    private Integer activePackageCount;
    private List<StudentHourPackageItemDTO> packages;
    private List<StudentHourDeductionItemDTO> deductions;

    public BigDecimal getTotalRemainingHours() {
        return totalRemainingHours;
    }

    public void setTotalRemainingHours(BigDecimal totalRemainingHours) {
        this.totalRemainingHours = totalRemainingHours;
    }

    public Integer getActivePackageCount() {
        return activePackageCount;
    }

    public void setActivePackageCount(Integer activePackageCount) {
        this.activePackageCount = activePackageCount;
    }

    public List<StudentHourPackageItemDTO> getPackages() {
        return packages;
    }

    public void setPackages(List<StudentHourPackageItemDTO> packages) {
        this.packages = packages;
    }

    public List<StudentHourDeductionItemDTO> getDeductions() {
        return deductions;
    }

    public void setDeductions(List<StudentHourDeductionItemDTO> deductions) {
        this.deductions = deductions;
    }
}
