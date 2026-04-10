package com.edusmart.manager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class BasePageQueryDTO {
    @Min(1)
    private long current = 1;

    @Min(1)
    @Max(200)
    private long size = 10;

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
