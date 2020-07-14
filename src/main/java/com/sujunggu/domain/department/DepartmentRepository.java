package com.sujunggu.domain.department;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    @Query("SELECT d FROM Department d")
    List<Department> findAll();
}
