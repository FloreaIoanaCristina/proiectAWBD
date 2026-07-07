package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.dto.request.MedicalServiceRequestDTO;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.mappers.DoctorMapper;
import com.unibuc.management.mappers.MedicalServiceMapper;
import com.unibuc.management.services.MedicalServiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicalServiceController.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MedicalServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicalServiceService medicalServiceService;

    @Test
    @WithMockUser
    void getAllMedicalServices_ShouldReturnOk() throws Exception {

        when(medicalServiceService.getAllServices())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/medical-services"))
                .andExpect(status().isOk());

        verify(medicalServiceService).getAllServices();
    }

    @Test
    @WithMockUser
    void getMedicalServiceById_ShouldReturnOk() throws Exception {

        MedicalService service = new MedicalService();
        service.setId(1);

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(service);

        mockMvc.perform(get("/api/medical-services/1"))
                .andExpect(status().isOk());

        verify(medicalServiceService).getMedicalServiceById(1);
    }

    @Test
    @WithMockUser
    void getBySpecialization_ShouldReturnOk() throws Exception {

        when(medicalServiceService.getServicesBySpecialization("Cardiology"))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/medical-services/specialization/Cardiology"))
                .andExpect(status().isOk());

        verify(medicalServiceService)
                .getServicesBySpecialization("Cardiology");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void createService_ShouldReturnCreated() throws Exception {

        MedicalServiceRequestDTO dto = new MedicalServiceRequestDTO();
        dto.setName("Consultatie");
        dto.setSpecialization("Cardiology");
        dto.setStartHour(8);
        dto.setEndHour(16);
        dto.setPrice(250.0);

        MedicalService service = new MedicalService();
        service.setId(1);

        when(medicalServiceService.saveFromDto(any(MedicalServiceRequestDTO.class)))
                .thenReturn(MedicalServiceMapper.toResponseDTO(service));

        mockMvc.perform(post("/api/medical-services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(medicalServiceService)
                .saveFromDto(any(MedicalServiceRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void updateService_ShouldReturnOk() throws Exception {

        MedicalServiceRequestDTO dto = new MedicalServiceRequestDTO();
        dto.setName("Consultatie");
        dto.setSpecialization("Cardiology");
        dto.setStartHour(8);
        dto.setEndHour(16);
        dto.setPrice(250.0);

        MedicalService service = new MedicalService();
        service.setId(1);

        when(medicalServiceService.update(eq(1), any(MedicalServiceRequestDTO.class)))
                .thenReturn(MedicalServiceMapper.toResponseDTO(service));

        mockMvc.perform(put("/api/medical-services/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(medicalServiceService)
                .update(eq(1), any(MedicalServiceRequestDTO.class));
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void deleteService_ShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/medical-services/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(medicalServiceService).delete(1);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void createService_WithInvalidDto_ShouldReturnBadRequest() throws Exception {

        MedicalServiceRequestDTO dto = new MedicalServiceRequestDTO();

        mockMvc.perform(post("/api/medical-services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(medicalServiceService);
    }
}