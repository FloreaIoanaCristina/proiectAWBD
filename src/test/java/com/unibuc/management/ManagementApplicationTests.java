package com.unibuc.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ManagementApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void cleanUp() {
        patientRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    @WithMockUser(username = "test_pacient", authorities = {"ROLE_PATIENT", "ROLE_DOCTOR"})
    public void integration_Patient_CreateAndGet_Flow() throws Exception {
        Patient patient = new Patient();
        patient.setName("Integration Test Patient");
        patient.setAge(java.time.LocalDate.of(1995, 5, 20));
        patient.setSex(Boolean.TRUE);
        patient.setSubscription(false);
        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andDo(print())
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Integration Test Patient"));
    }

    @Test
    public void integration_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test_doctor", authorities = {"ROLE_DOCTOR"})
    public void integration_GetNonExistentPatient_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/patients/9999"))
                .andExpect(status().isNotFound());
    }
}