package com.example.demo.repository;

import com.example.demo.entity.MpinRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MpinRepository extends JpaRepository<MpinRecordEntity, String> {
}
