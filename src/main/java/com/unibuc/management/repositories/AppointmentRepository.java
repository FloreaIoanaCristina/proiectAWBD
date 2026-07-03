package com.unibuc.management.repositories;
import com.unibuc.management.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("""
                SELECT a 
                FROM Appointment a 
                WHERE a.medicalService.id = :medicalServiceId 
                AND a.appointmentFrom BETWEEN :start AND :end
            """)
    List<Appointment> findByMedicalServiceAndDate(
            @Param("medicalServiceId") Integer medicalServiceId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    @Query("""
                SELECT a 
                FROM Appointment a 
                JOIN a.medicalService ms
                JOIN ms.medicalServiceDoctors d
                WHERE d.id = :doctorId 
                AND a.appointmentFrom BETWEEN :start AND :end
            """)
    List<Appointment> findByDoctorIdAndDate(
            @Param("doctorId") Integer doctorId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end
    );

    Page<Appointment> findByPatientId(Integer patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Integer doctorId, Pageable pageable);
    long countByPatientId(Integer patientId);
    long countByDoctorId(Integer doctorId);
}