package com.unibuc.management.repositories;

import com.unibuc.management.entities.PaidTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PaidTimeOffRepository extends JpaRepository<PaidTimeOff, Integer> {

    @Query("SELECT p FROM PaidTimeOff p WHERE p.doctor.id = :doctorId " +
            "AND p.ptoFrom <= :endOfDay AND p.ptoTo >= :startOfDay")
    List<PaidTimeOff> findActivePtoForDoctorInDay(
            @Param("doctorId") Integer doctorId,
            @Param("startOfDay") OffsetDateTime startOfDay,
            @Param("endOfDay") OffsetDateTime endOfDay
    );

}