package com.unibuc.management.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.clients.MedicalClient;
import com.unibuc.management.dto.request.PaymentRequestDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import com.unibuc.management.PaymentServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = PaymentServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaymentIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicalClient medicalClient;

    private PaymentRequestDTO paymentDto() {
        PaymentRequestDTO dto = new PaymentRequestDTO();
        dto.setAmount(250.0);
        dto.setAppointmentId(1);
        dto.setPaymentMethod("Card");
        dto.setStatus("PENDING");
        dto.setPaymentDate(LocalDate.now());
        return dto;
    }

    @Test
    @WithMockUser(username = "doc", roles = {"DOCTOR"})
    void integration_CreateAndGetPayment_Flow() throws Exception {
        when(medicalClient.getAppointmentSummary(any())).thenReturn(
                AppointmentSummaryDTO.builder().id(1).patientId(1).build());

        MvcResult createResult = mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(250.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long id = created.get("id").asLong();

        mockMvc.perform(get("/api/payments/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(250.0));
    }

    @Test
    void integration_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/payments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "doc", roles = {"DOCTOR"})
    void integration_GetNonExistentPayment_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/9999"))
                .andExpect(status().isNotFound());
    }
}