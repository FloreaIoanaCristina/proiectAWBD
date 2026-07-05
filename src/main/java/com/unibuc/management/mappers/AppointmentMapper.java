package com.unibuc.management.mappers;

import com.unibuc.management.domain.Appointment;
import com.unibuc.management.dto.response.AppointmentResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDTO toDto(Appointment appointment) {

        if (appointment == null) {
            return null;
        }

        return AppointmentResponseDTO.builder()
                .id(appointment.getId())
                .appointmentFrom(appointment.getAppointmentFrom())
                .status(appointment.getStatus())
                .payment(PaymentMapper.toSummary(appointment.getPayment()))
                .medicalService(MedicalServiceMapper.toSummary(appointment.getMedicalService()))
                .doctor(DoctorMapper.toSummary(appointment.getDoctor()))
                .patient(PatientMapper.toSummary(appointment.getPatient()))
                .build();
    }
}