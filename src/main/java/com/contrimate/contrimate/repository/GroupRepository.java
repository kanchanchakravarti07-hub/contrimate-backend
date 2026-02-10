package com.contrimate.contrimate.repository;

import com.contrimate.contrimate.entity.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<AppGroup, Long> {
}