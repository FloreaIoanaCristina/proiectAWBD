package com.unibuc.management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.clients.MedicalClient;
import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.dto.internal.ProfileIdDTO;
import com.unibuc.management.user_service.UserServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicalClient medicalClient;

    private RegisterRequest patientRegister() {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("patient1");
        register.setPassword("password123");
        register.setRole("PATIENT");
        register.setFullName("Integration Patient");
        register.setAge(LocalDate.of(1995, 5, 20));
        register.setSex(true);
        return register;
    }

    @Test
    void integration_Register_Login_And_GetCurrentUser() throws Exception {
        when(medicalClient.getPatientByUsername(eq("patient1")))
                .thenReturn(ProfileIdDTO.builder().id(1).build());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRegister())))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("patient1", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("patient1"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertNotNull(session);

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("patient1"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.patientId").value(1));
    }

    @Test
    void integration_Register_Duplicate_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRegister())))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRegister())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void integration_Register_InvalidPayload_ReturnsBadRequest() throws Exception {
        RegisterRequest invalid = new RegisterRequest();
        invalid.setUsername("ab");
        invalid.setPassword("123");
        invalid.setRole("PATIENT");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}