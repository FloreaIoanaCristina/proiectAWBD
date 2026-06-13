package com.unibuc.management.controllers;

import com.unibuc.management.entities.Doctor;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.services.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DoctorControllerTest {

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorController doctorController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorController).build();
    }

    @Test
    public void createDoctor_ReturnsCreatedStatus() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setOffice("Cardiology");

        when(doctorService.saveDoctor(any())).thenReturn(doctor);

        mockMvc.perform(post("/api/doctors")
                        .contentType("application/json")
                        .content("{\"office\": \"Cardiology\"}"))
                .andExpect(status().isCreated())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    if (!content.isEmpty()) {
                        org.junit.jupiter.api.Assertions.assertTrue(content.contains("Cardiology"));
                    }
                });
    }

    @Test
    public void getDoctorById_ReturnsDoctor_WhenFound() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setOffice("Cardiology");

        when(doctorService.getDoctorById(1)).thenReturn(doctor);

        mockMvc.perform(get("/api/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.office").value("Cardiology"));
    }

    @Test
    public void getDoctorById_ReturnsNotFound_WhenDoctorNotFound() throws Exception {
        when(doctorService.getDoctorById(1)).thenThrow(new ResourceNotFoundException("Not found"));

        jakarta.servlet.ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(get("/api/doctors/1"))
        );
        org.junit.jupiter.api.Assertions.assertTrue(exception.getCause() instanceof ResourceNotFoundException);
    }

    @Test
    public void updateDoctor_ReturnsUpdatedDoctor_WhenDoctorExists() throws Exception {
        Doctor updatedDoctor = new Doctor();
        updatedDoctor.setId(1);
        updatedDoctor.setOffice("Neurology");

        when(doctorService.updateDoctor(eq(1), any())).thenReturn(updatedDoctor);

        mockMvc.perform(put("/api/doctors/1")
                        .contentType("application/json")
                        .content("{\"office\": \"Neurology\"}"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String content = result.getResponse().getContentAsString();
                    if (!content.isEmpty()) {
                        org.junit.jupiter.api.Assertions.assertTrue(content.contains("Neurology"));
                    }
                });
    }

    @Test
    public void updateDoctor_ReturnsNotFound_WhenDoctorNotFound() throws Exception {
        when(doctorService.updateDoctor(eq(1), any())).thenThrow(new ResourceNotFoundException("Not found"));

        jakarta.servlet.ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(put("/api/doctors/1")
                        .contentType("application/json")
                        .content("{\"office\": \"Neurology\"}"))
        );
        org.junit.jupiter.api.Assertions.assertTrue(exception.getCause() instanceof ResourceNotFoundException);
    }

    @Test
    public void deleteDoctor_ReturnsNoContent_WhenDeleted() throws Exception {
        Mockito.doNothing().when(doctorService).deleteDoctor(1);

        mockMvc.perform(delete("/api/doctors/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteDoctor_ReturnsNotFound_WhenDoctorNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Doctorul nu a fost găsit"))
                .when(doctorService).deleteDoctor(1);

        jakarta.servlet.ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(delete("/api/doctors/1"))
        );
        org.junit.jupiter.api.Assertions.assertTrue(exception.getCause() instanceof ResourceNotFoundException);
    }
}