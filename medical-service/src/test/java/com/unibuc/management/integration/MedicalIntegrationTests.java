package com.unibuc.management.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.clients.PaymentClient;
import com.unibuc.management.clients.UserClient;
import com.unibuc.management.MedicalServiceApplication;
import com.unibuc.management.domain.Appointment;
import com.unibuc.management.domain.Doctor;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.domain.Patient;
import com.unibuc.management.dto.request.AppointmentRequestDTO;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.dto.summary.PaymentSummaryDTO;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.PatientRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MedicalServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MedicalIntegrationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private MedicalServiceRepository medicalServiceRepository;
    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;

    @MockitoBean
    private PaymentClient paymentClient;
    @MockitoBean
    private UserClient userClient;
    @Test
    @WithMockUser(username = "staff", roles = {"PATIENT", "DOCTOR"})
    void integration_Patient_CreateAndGet_Flow() throws Exception {
        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Integration Test Patient");
        dto.setBirthDate(LocalDate.of(1995, 5, 20));
        dto.setSex(true);
        dto.setSubscription(false);
        dto.setMedicalRecord("Healthy");

        mockMvc.perform(post("/api/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Patient"));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Integration Test Patient"));
    }
    @Test
    void integration_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(username = "doc", roles = {"DOCTOR"})
    void integration_GetNonExistentPatient_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/patients/9999"))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(username = "patient1", roles = {"PATIENT"})
    void integration_Appointment_CallsPaymentService() throws Exception {
        MedicalService service = new MedicalService();
        service.setName("Consultatie");
        service.setSpecialization("Cardiologie");
        service.setPrice(250.0);
        service.setStartHour(8);
        service.setEndHour(16);
        service.setRating(0.0);
        service.setNrOfRatings(0);
        service = medicalServiceRepository.save(service);

        Doctor doctor = new Doctor();
        doctor.setName("Doctor Test");
        doctor.setOffice("101");
        doctor.setNumberOfPTOdays(25);
        doctor.setMedicalService(service);
        doctor = doctorRepository.save(doctor);

        Patient patient = new Patient();
        patient.setName("Integration Patient");
        patient.setAge(LocalDate.of(1998, 5, 20));
        patient.setSex(true);
        patient.setSubscription(false);
        patient.setUsername("patient1");
        patientRepository.save(patient);

        when(paymentClient.createForAppointment(any())).thenReturn(
                PaymentSummaryDTO.builder()
                        .id(1L)
                        .status("PENDING")
                        .amount(BigDecimal.valueOf(250))
                        .build());

        String date = LocalDate.now().plusDays(1).toString();

        MvcResult slotResult = mockMvc.perform(get("/api/appointments/available-times")
                        .param("medicalServiceId", service.getId().toString())
                        .param("doctorId", doctor.getId().toString())
                        .param("date", date))
                .andExpect(status().isOk())
                .andReturn();

        OffsetDateTime slot = objectMapper.readValue(
                        slotResult.getResponse().getContentAsString(),
                        new TypeReference<List<OffsetDateTime>>() {})
                .get(0);

        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setMedicalServiceId(service.getId());
        dto.setDoctorId(doctor.getId());
        dto.setAppointmentFrom(slot);

        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Appointed"))
                .andExpect(jsonPath("$.payment").exists())
                .andExpect(jsonPath("$.payment.status").value("PENDING"));

        assertEquals(1, appointmentRepository.count());
        Appointment appointment = appointmentRepository.findAll().get(0);
        assertEquals("Appointed", appointment.getStatus());

        verify(paymentClient).createForAppointment(any());
    }
}