package com.unibuc.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unibuc.management.domain.*;
import com.unibuc.management.dto.LoginRequest;
import com.unibuc.management.dto.RegisterRequest;
import com.unibuc.management.dto.request.AppointmentRequestDTO;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.repositories.*;
import com.unibuc.management.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ManagementApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MedicalServiceRepository medicalServiceRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void cleanUp() {
        patientRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    @WithMockUser(username = "test_pacient", authorities = {"ROLE_PATIENT", "ROLE_DOCTOR"})
    void integration_Patient_CreateAndGet_Flow() throws Exception {

        PatientRequestDTO dto = new PatientRequestDTO();
        dto.setName("Integration Test Patient");
        dto.setBirthDate(LocalDate.of(1995, 5, 20));
        dto.setSex(true);
        dto.setSubscription(false);
        dto.setMedicalRecord("Healthy");
        dto.setInsuranceProviderId(null);

        mockMvc.perform(post("/api/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Integration Test Patient"));
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

    @Test
    void integration_Register_Login_And_GetCurrentUser() throws Exception {

        RegisterRequest register = new RegisterRequest();
        register.setUsername("patient1");
        register.setPassword("password123");
        register.setRole("PATIENT");
        register.setFullName("Integration Patient");
        register.setAge(LocalDate.of(1995, 5, 20));
        register.setSex(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("patient1", "password123");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("patient1"))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        mockMvc.perform(get("/auth/me")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("patient1"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.patientId").exists());
    }

    @Test
    void integration_Appointment_CreatesPendingPayment() throws Exception {

        // ---------- Register patient ----------
        RegisterRequest register = new RegisterRequest();
        register.setUsername("patient1");
        register.setPassword("password123");
        register.setRole("PATIENT");
        register.setFullName("Integration Patient");
        register.setAge(LocalDate.of(1998, 5, 20));
        register.setSex(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // ---------- Login ----------
        LoginRequest login = new LoginRequest(
                "patient1",
                "password123"
        );

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) loginResult.getRequest().getSession(false);

        assertNotNull(session);

        // ---------- Create Medical Service ----------
        MedicalService service = new MedicalService();
        service.setName("Consultatie");
        service.setSpecialization("Cardiologie");
        service.setPrice(250.0);
        service.setStartHour(8);
        service.setEndHour(16);
        service.setRating(0.0);
        service.setNrOfRatings(0);

        service = medicalServiceRepository.save(service);

        // ---------- Create Doctor ----------
        User doctorUser = new User();
        doctorUser.setUsername("doctor1");
        doctorUser.setPassword(passwordEncoder.encode("password"));
        doctorUser.setRole(Role.DOCTOR);

        doctorUser = userRepository.save(doctorUser);

        Doctor doctor = new Doctor();
        doctor.setName("Doctor Test");
        doctor.setOffice("101");
        doctor.setNumberOfPTOdays(25);
        doctor.setMedicalService(service);
        doctor.setUser(doctorUser);
        doctor = doctorRepository.save(doctor);

        service.getMedicalServiceDoctors().add(doctor);
        medicalServiceRepository.save(service);

        // ---------- Obtain available slot ----------
        String date = LocalDate.now().plusDays(1).toString();

        MvcResult slotResult = mockMvc.perform(
                        get("/api/appointments/available-times")
                                .session(session)
                                .param("medicalServiceId", service.getId().toString())
                                .param("doctorId", doctor.getId().toString())
                                .param("date", date)
                )
                .andExpect(status().isOk())
                .andReturn();

        OffsetDateTime slot =
                objectMapper.readValue(
                                slotResult.getResponse().getContentAsString(),
                                new TypeReference<List<OffsetDateTime>>() {})
                        .get(0);

        // ---------- Create appointment ----------
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setMedicalServiceId(service.getId());
        dto.setDoctorId(doctor.getId());
        dto.setAppointmentFrom(slot);

        mockMvc.perform(post("/api/appointments")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Appointed"))
                .andExpect(jsonPath("$.payment").exists());

        // ---------- Verify Appointment ----------
        assertEquals(1, appointmentRepository.count());

        Appointment appointment =
                appointmentRepository.findAll().get(0);

        assertNotNull(appointment.getPayment());

        // ---------- Verify Payment ----------
        assertEquals(1, paymentRepository.count());

        Payment payment =
                paymentRepository.findAll().get(0);

        assertEquals("PENDING", payment.getStatus());
        assertEquals(
                0,
                BigDecimal.valueOf(250.0).compareTo(payment.getAmount())
        );

        assertEquals(
                appointment.getId(),
                payment.getAppointment().getId()
        );
    }
}