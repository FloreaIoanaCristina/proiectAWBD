package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.dto.request.AppointmentRequestDTO;
import com.unibuc.management.domain.Appointment;
import com.unibuc.management.dto.response.AppointmentResponseDTO;
import com.unibuc.management.repositories.UserRepository;
import com.unibuc.management.services.AppointmentService;
import com.unibuc.management.services.DoctorService;
import com.unibuc.management.services.MedicalServiceService;
import com.unibuc.management.services.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.time.OffsetDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class AppointmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AppointmentService appointmentService;

    @MockitoBean
    PatientService patientService;

    @MockitoBean
    MedicalServiceService medicalServiceService;

    @MockitoBean
    DoctorService doctorService;
    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    DataSource dataSource;

    @Test
    void createAppointment() throws Exception {

        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setMedicalServiceId(1);
        dto.setDoctorId(2);
        dto.setAppointmentFrom(OffsetDateTime.now().plusDays(1));
        dto.setStatus("Appointed");

        AppointmentResponseDTO appointment = new AppointmentResponseDTO();
        appointment.setId(1);

        when(appointmentService.createAppointment(any(Authentication.class), any(AppointmentRequestDTO.class)))
                .thenReturn(appointment);

        mockMvc.perform(post("/api/appointments")
                        .with(user("patient").roles("PATIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(appointmentService).createAppointment(any(), any());
    }

    @Test
    void getAvailableTimeSlots() throws Exception {

        when(appointmentService.getAvailableTimeSlots(1, null, "2026-08-10"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/appointments/available-times")
                        .param("medicalServiceId", "1")
                        .param("date", "2026-08-10"))
                .andExpect(status().isOk());

        verify(appointmentService)
                .getAvailableTimeSlots(1, null, "2026-08-10");
    }

    @Test
    void getAppointmentsByPatient() throws Exception {

        when(appointmentService.getAppointmentsByPatientId(anyInt(), any(), any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/appointments/patient/5")
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isOk());

        verify(appointmentService)
                .getAppointmentsByPatientId(eq(5), any(), any());
    }

    @Test
    void submitFeedback() throws Exception {

        doNothing().when(appointmentService)
                .submitFeedback(10, 5.0f, "patient");

        mockMvc.perform(post("/api/appointments/10/feedback")
                        .with(user("patient").roles("PATIENT"))
                        .param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Feedback submitted successfully."));

        verify(appointmentService)
                .submitFeedback(10, 5.0f, "patient");
    }

    @Test
    void deleteAppointment() throws Exception {

        doNothing().when(appointmentService)
                .deleteAppointment(eq(1), any(Authentication.class));

        mockMvc.perform(delete("/api/appointments/1")
                        .with(user("patient").roles("PATIENT")))
                .andExpect(status().isNoContent());

        verify(appointmentService)
                .deleteAppointment(eq(1), any(Authentication.class));
    }
}