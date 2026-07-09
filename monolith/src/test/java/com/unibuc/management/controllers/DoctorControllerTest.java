package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.dto.request.DoctorRequestDTO;
import com.unibuc.management.domain.Doctor;
import com.unibuc.management.mappers.DoctorMapper;
import com.unibuc.management.services.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DoctorControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    DoctorService doctorService;

    @Test
    void getAllDoctors() throws Exception {

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr. House");

        when(doctorService.getAllDoctors())
                .thenReturn(List.of(doctor).stream().map(DoctorMapper::toResponseDTO).toList());

        mockMvc.perform(get("/api/doctors")
                    .with(user("patient").roles("PATIENT"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Dr. House"));

        verify(doctorService).getAllDoctors();
    }

    @Test
    void getDoctorById() throws Exception {

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr. House");

        when(doctorService.getDoctorById(1))
                .thenReturn(doctor);

        mockMvc.perform(get("/api/doctors/1")
                    .with(user("patient").roles("PATIENT"))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Dr. House"));

        verify(doctorService).getDoctorById(1);
    }

    @Test
    void getDoctorsPaged() throws Exception {

        when(doctorService.getDoctorsPaged(any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/doctors/paged")
                    .with(user("patient").roles("PATIENT"))
                    .with(csrf()))
                .andExpect(status().isOk());

        verify(doctorService).getDoctorsPaged(any());
    }

    @Test
    void createDoctor() throws Exception {

        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setName("Dr. House");
        dto.setOffice("Cabinet 1");
        dto.setMedicalServiceId(1);
        dto.setNumberOfPtodays(30);

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr. House");

        when(doctorService.createDoctor(any(DoctorRequestDTO.class)))
                .thenReturn(DoctorMapper.toResponseDTO(doctor));

        mockMvc.perform(post("/api/doctors")
                        .with(SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(doctorService).createDoctor(any(DoctorRequestDTO.class));
    }

    @Test
    void updateDoctor() throws Exception {

        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setName("Updated Doctor");
        dto.setOffice("Cabinet 2");
        dto.setMedicalServiceId(1);
        dto.setNumberOfPtodays(25);

        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Updated Doctor");

        when(doctorService.updateDoctor(eq(1), any(DoctorRequestDTO.class)))
                .thenReturn(DoctorMapper.toResponseDTO(doctor));

        mockMvc.perform(put("/api/doctors/1")
                        .with(SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Doctor"));

        verify(doctorService).updateDoctor(eq(1), any(DoctorRequestDTO.class));
    }

    @Test
    void deleteDoctor() throws Exception {

        doNothing().when(doctorService).deleteDoctor(1);

        mockMvc.perform(delete("/api/doctors/1")
                        .with(SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(doctorService).deleteDoctor(1);
    }

    @Test
    void createDoctor_InvalidRequest_ShouldReturnBadRequest() throws Exception {

        DoctorRequestDTO dto = new DoctorRequestDTO();

        mockMvc.perform(post("/api/doctors")
                        .with(SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }
}