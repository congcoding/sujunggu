package com.sujunggu.service;

import com.sujunggu.domain.department.DepartmentRepository;
import com.sujunggu.web.dto.DepartmentListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentListResponseDto> findAll() {
        return departmentRepository.findAll().stream()
                .map(DepartmentListResponseDto::new)
                .collect(Collectors.toList());
    }
}
