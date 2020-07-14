package com.sujunggu.web.dto;

import com.sujunggu.domain.department.Department;
import lombok.Getter;

@Getter
public class DepartmentListResponseDto {
    private String address;
    private String college;
    private String major;

    public DepartmentListResponseDto(Department entity) {
        this.address = entity.getAddress();
        this.college = entity.getCollege();
        this.major = entity.getMajor();
    }
}
