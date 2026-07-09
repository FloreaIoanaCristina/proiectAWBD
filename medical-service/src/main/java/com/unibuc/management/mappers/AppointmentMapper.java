package com.unibuc.management.mappers;

import com.unibuc.management.domain.Appointment;
import com.unibuc.management.dto.response.AppointmentResponseDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import lombok.experimental.UtilityClass;


@UtilityClass
public class AppointmentMapper {

    public AppointmentResponseDTO toDto(Appointment appointment) {

        if (appointment == null) {
            return null;
        }

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .appointmentFrom(appointment.getAppointmentFrom())
                .status(appointment.getStatus())
                .medicalService(MedicalServiceMapper.toSummary(appointment.getMedicalService()))
                .doctor(DoctorMapper.toSummary(appointment.getDoctor()))
                .patient(PatientMapper.toSummary(appointment.getPatient()))
                .build();
    }

    public AppointmentSummaryDTO toSummary(Appointment appointment) {

        if (appointment == null) {
            return null;
        }

        return AppointmentSummaryDTO.builder()
                .id(appointment.getId())
                .appointmentFrom(appointment.getAppointmentFrom())
                .status(appointment.getStatus())

                .patientId(
                        appointment.getPatient() != null
                                ? appointment.getPatient().getId()
                                : null
                )
                .patientName(
                        appointment.getPatient() != null
                                ? appointment.getPatient().getName()
                                : null
                )

                .doctorId(
                        appointment.getDoctor() != null
                                ? appointment.getDoctor().getId()
                                : null
                )
                .doctorName(
                        appointment.getDoctor() != null
                                ? appointment.getDoctor().getName()
                                : null
                )

                .medicalServiceId(
                        appointment.getMedicalService() != null
                                ? appointment.getMedicalService().getId()
                                : null
                )
                .medicalServiceName(
                        appointment.getMedicalService() != null
                                ? appointment.getMedicalService().getName()
                                : null
                )

                .build();
    }
}