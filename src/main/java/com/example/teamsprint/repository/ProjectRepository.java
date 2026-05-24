package com.example.teamsprint.repository;

import com.example.teamsprint.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository  extends JpaRepository<Project, Long> {
    Page<Project> findByUsers_Id(Long userId, Pageable pageable);
}
