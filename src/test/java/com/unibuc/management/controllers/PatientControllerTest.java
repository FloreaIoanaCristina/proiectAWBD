package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.dto.validation.PatientRequestDTO;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.exceptions.GlobalExceptionHandler;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.services.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patientController = new PatientController(patientService);
        mockMvc = MockMvcBuilders.standaloneSetup(patientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }
    @Test
    void testGetAllPatients() throws Exception {
        when(patientService.getAllPatients()).thenReturn(List.of(new Patient(), new Patient()));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").isNotEmpty());
    }

    @Test
    void testGetPatientById_Success() throws Exception {
        Integer patientId = 1;
        Patient patient = new Patient();
        patient.setId(patientId);

        when(patientService.getPatientById(patientId)).thenReturn(patient);

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId));
    }

    @Test
    void testGetPatientById_NotFound() throws Exception {
        Integer patientId = 1;

        when(patientService.getPatientById(patientId))
                .thenThrow(new ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreatePatient() throws Exception {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("John Doe");
        dto.setBirthDate(LocalDate.now().minusYears(25));
        dto.setSex(true);
        dto.setSubscription(false);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setName("John Doe");
        patient.setAge(dto.getBirthDate());
        patient.setSex(true);
        patient.setSubscription(false);

        when(patientService.createPatient(any(PatientRequestDTO.class))).thenReturn(patient);

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testUpdatePatient_Success() throws Exception {
        Integer patientId = 1;

        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Updated Name");
        dto.setBirthDate(LocalDate.now().minusYears(30));
        dto.setSex(true);
        dto.setSubscription(true);

        Patient updatedPatient = new Patient();
        updatedPatient.setId(patientId);
        updatedPatient.setName("Updated Name");
        updatedPatient.setAge(dto.getBirthDate());
        updatedPatient.setSex(true);
        updatedPatient.setSubscription(true);

        when(patientService.updatePatient(eq(patientId), any(PatientRequestDTO.class))).thenReturn(updatedPatient);

        mockMvc.perform(put("/api/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }
    @Test
    void testUpdatePatient_NotFound() throws Exception {
        Integer patientId = 1;

        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Updated Name");
        dto.setBirthDate(LocalDate.now().minusYears(30));
        dto.setSex(true);
        dto.setSubscription(true);

        when(patientService.updatePatient(eq(patientId), any(PatientRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Pacientul nu a fost găsit."));

        mockMvc.perform(put("/api/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
    @Test
    void testDeletePatient_Success() throws Exception {
        Integer patientId = 1;

        doNothing().when(patientService).deletePatient(patientId);

        mockMvc.perform(delete("/api/patients/{id}", patientId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePatient_NotFound() throws Exception {
        Integer patientId = 1;

        Mockito.doThrow(new ResourceNotFoundException("Not found"))
                .when(patientService).deletePatient(patientId);

        mockMvc.perform(delete("/api/patients/{id}", patientId))
                .andExpect(status().isNotFound());
    }
}