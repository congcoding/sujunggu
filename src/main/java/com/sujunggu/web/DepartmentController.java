package com.sujunggu.web;

import com.sujunggu.service.department.DepartmentService;
import com.sujunggu.web.dto.DepartmentListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/api/test")
    public List<DepartmentListResponseDto> test() {
        List<DepartmentListResponseDto> departmentList = departmentService.findAll();
        return departmentList;
    }

}
