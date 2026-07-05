package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.domain.Patient;
import com.unibuc.management.mappers.PatientMapper;
import com.unibuc.management.services.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientService patientService;

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getAllPatients_ShouldReturnOk() throws Exception {

        when(patientService.getAllPatientsPaged(any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk());

        verify(patientService).getAllPatientsPaged(any());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void getAllPatientsUnpaged_ShouldReturnOk() throws Exception {

        when(patientService.getAllPatients())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/patients/all"))
                .andExpect(status().isOk());

        verify(patientService).getAllPatients();
    }

    @Test
    @WithMockUser
    void getPatientById_ShouldReturnOk() throws Exception {

        Patient patient = new Patient();
        patient.setId(1);

        when(patientService.getPatientById(1))
                .thenReturn(patient);

        mockMvc.perform(get("/api/patients/1"))
                .andExpect(status().isOk());

        verify(patientService).getPatientById(1);
    }

    @Test
    @WithMockUser
    void createPatient_ShouldReturnCreated() throws Exception {

        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Ion Popescu");
        dto.setBirthDate(LocalDate.of(1990, 5, 10));
        dto.setSex(true);
        dto.setSubscription(true);
        dto.setMedicalRecord("Healthy");

        Patient patient = new Patient();
        patient.setId(1);

        when(patientService.createPatient(any(PatientRequestDTO.class)))
                .thenReturn(PatientMapper.toResponseDTO(patient));

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(patientService).createPatient(any(PatientRequestDTO.class));
    }

    @Test
    @WithMockUser
    void updatePatient_ShouldReturnOk() throws Exception {

        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Ion Popescu");
        dto.setBirthDate(LocalDate.of(1990, 5, 10));
        dto.setSex(true);
        dto.setSubscription(true);
        dto.setMedicalRecord("Updated");

        Patient patient = new Patient();
        patient.setId(1);

        when(patientService.updatePatient(eq(1), any(PatientRequestDTO.class)))
                .thenReturn(PatientMapper.toResponseDTO(patient));

        mockMvc.perform(put("/api/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(patientService).updatePatient(eq(1), any(PatientRequestDTO.class));
    }

    @Test
    @WithMockUser
    void deletePatient_ShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/patients/1"))
                .andExpect(status().isNoContent());

        verify(patientService).deletePatient(1);
    }

    @Test
    @WithMockUser
    void createPatient_WithInvalidDto_ShouldReturnBadRequest() throws Exception {

        PatientRequestDTO dto = new PatientRequestDTO();

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(patientService);
    }
}