package com.unibuc.management.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.dto.request.PtoRequestDTO;
import com.unibuc.management.services.DoctorScheduleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorScheduleController.class)
class DoctorScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DoctorScheduleService doctorScheduleService;

    @Test
    @WithMockUser
    void getDoctorScheduleForDay_ShouldReturnOk() throws Exception {

        when(doctorScheduleService.getDoctorScheduleForDay(eq(1), eq("2026-07-15")))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/doctor-schedule/day")
                        .param("doctorId", "1")
                        .param("date", "2026-07-15"))
                .andExpect(status().isOk());

        verify(doctorScheduleService)
                .getDoctorScheduleForDay(1, "2026-07-15");
    }

    @Test
    @WithMockUser
    void getDoctorLeaves_ShouldReturnOk() throws Exception {

        when(doctorScheduleService.getDoctorLeaves(1))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/doctor-schedule/pto/1"))
                .andExpect(status().isOk());

        verify(doctorScheduleService).getDoctorLeaves(1);
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor1")
    void schedulePTO_ShouldReturnOk() throws Exception {

        PtoRequestDTO dto = new PtoRequestDTO();
        dto.setDoctorId(1);
        dto.setStartDate(LocalDate.now().plusDays(2));
        dto.setEndDate(LocalDate.now().plusDays(5));

        mockMvc.perform(post("/api/doctor-schedule/schedulePTO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(doctorScheduleService)
                .schedulePTO(any(PtoRequestDTO.class), eq("doctor1"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor1")
    void updatePTO_ShouldReturnOk() throws Exception {

        PtoRequestDTO dto = new PtoRequestDTO();
        dto.setDoctorId(1);
        dto.setStartDate(LocalDate.now().plusDays(3));
        dto.setEndDate(LocalDate.now().plusDays(6));

        mockMvc.perform(put("/api/doctor-schedule/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(doctorScheduleService)
                .updatePTO(eq(10), any(PtoRequestDTO.class), eq("doctor1"));
    }

    @Test
    @WithMockUser(roles = "DOCTOR", username = "doctor1")
    void deletePTO_ShouldReturnNoContent() throws Exception {

        mockMvc.perform(delete("/api/doctor-schedule/10"))
                .andExpect(status().isNoContent());

        verify(doctorScheduleService)
                .deletePTO(10, "doctor1");
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void schedulePTO_WithInvalidDto_ShouldReturnBadRequest() throws Exception {

        PtoRequestDTO dto = new PtoRequestDTO();

        mockMvc.perform(post("/api/doctor-schedule/schedulePTO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(doctorScheduleService);
    }
}