package com.unibuc.management.controllers;

import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.services.MedicalServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class MedicalServiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MedicalServiceService medicalServiceService;

    @InjectMocks
    private MedicalServiceController medicalServiceController;

    private List<MedicalService> medicalServices;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        this.medicalServiceController = new MedicalServiceController(medicalServiceService);

        this.mockMvc = MockMvcBuilders.standaloneSetup(medicalServiceController).build();

        MedicalService medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setName("General Medicine");
        medicalService.setSpecialization("General");
        medicalService.setStartHour(9);
        medicalService.setEndHour(17);
        medicalService.setPrice(150.0);
        medicalService.setRating(4.5);
        medicalService.setNrOfRatings(10);

        MedicalService medicalService2 = new MedicalService();
        medicalService2.setId(2);
        medicalService2.setName("Cardiology");
        medicalService2.setSpecialization("Cardiology");
        medicalService2.setStartHour(9);
        medicalService2.setEndHour(17);
        medicalService2.setPrice(250.0);
        medicalService2.setRating(4.8);
        medicalService2.setNrOfRatings(12);

        medicalServices = Arrays.asList(medicalService, medicalService2);
    }
    @Test
    public void testGetAllMedicalServices() throws Exception {
        when(medicalServiceService.getAllServices()).thenReturn(medicalServices);

        mockMvc.perform(get("/api/medical-services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("General Medicine"))
                .andExpect(jsonPath("$[1].name").value("Cardiology"));
    }

    @Test
    public void testGetMedicalServicesBySpecialization() throws Exception {
        String specialization = "Cardiology";

        when(medicalServiceService.getServicesBySpecialization(specialization)).thenReturn(
                Arrays.asList(medicalServices.get(1))
        );

        mockMvc.perform(get("/api/medical-services/specialization/{specialization}", specialization))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Cardiology"))
                .andExpect(jsonPath("$[0].specialization").value("Cardiology"));
    }
}