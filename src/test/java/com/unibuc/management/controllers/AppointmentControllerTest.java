package com.unibuc.management.controllers;

import com.unibuc.management.dto.validation.AppointmentRequestDTO;
import com.unibuc.management.entities.*;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private PatientService patientService;

    @Mock
    private MedicalServiceService medicalServiceService;

    @Mock
    private PaymentService paymentTypeService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private AppointmentController appointmentController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController).build();
    }

    @Test
    public void testCreateAppointment_Success() throws Exception {
        Integer patientId = 1;
        Integer medicalServiceId = 1;
        OffsetDateTime appointmentFrom = OffsetDateTime.now().plusDays(1);

        Patient patient = new Patient();
        patient.setId(patientId);
        MedicalService medicalService = new MedicalService();
        medicalService.setId(medicalServiceId);
        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(100);

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn("testUser");

        when(appointmentService.createAppointment(any(Authentication.class), any(AppointmentRequestDTO.class)))
                .thenReturn(savedAppointment);

        mockMvc.perform(post("/api/appointments")
                        .param("patientId", String.valueOf(patientId))
                        .param("medicalServiceId", String.valueOf(medicalServiceId))
                        .param("appointmentFrom", appointmentFrom.toString())
                        .principal(() -> "testUser"))
                .andExpect(status().isCreated());
    }
    @Test
    public void testGetAvailableTimeSlots_Success() throws Exception {
        Integer medicalServiceId = 1;
        String date = "2025-01-15";
        MedicalService medicalService = new MedicalService();
        OffsetDateTime availableSlot = OffsetDateTime.parse(date + "T09:00:00+00:00");

        when(medicalServiceService.getMedicalServiceById(medicalServiceId)).thenReturn(medicalService);
        when(appointmentService.getAvailableTimeSlots(medicalService, date)).thenReturn(Collections.singletonList(availableSlot));

        mockMvc.perform(get("/api/appointments/available-times")
                        .param("medicalServiceId", String.valueOf(medicalServiceId))
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(availableSlot.toInstant().getEpochSecond()));
    }

    @Test
    public void testGetAvailableTimeSlots_Failure() throws Exception {
        Integer medicalServiceId = 1;
        String date = "2025-01-15";

        when(medicalServiceService.getMedicalServiceById(medicalServiceId))
                .thenThrow(new ResourceNotFoundException("Serviciu negăsit"));

        jakarta.servlet.ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> {
                    mockMvc.perform(get("/api/appointments/available-times")
                            .param("medicalServiceId", String.valueOf(medicalServiceId))
                            .param("date", date));
                }
        );

        assertTrue(exception.getCause() instanceof ResourceNotFoundException);
        assertEquals("Serviciu negăsit", exception.getCause().getMessage());
    }
    @Test
    public void testGetAppointmentsByPatientId() throws Exception {
        Integer patientId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(1);

        List<Appointment> appointmentList = Collections.singletonList(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointmentList);

        when(appointmentService.getAppointmentsByPatientIdPaged(eq(patientId), any(Pageable.class)))
                .thenReturn(appointmentPage);

        mockMvc.perform(get("/api/appointments/patient/{patientId}", patientId)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "appointmentFrom,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void testSubmitFeedback_Success() throws Exception {
        Integer appointmentId = 1;
        float rating = 4.5f;

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn("testUser");

        Patient mockPatient = new Patient();
        mockPatient.setId(1);
        when(patientService.getPatientByUsername("testUser")).thenReturn(mockPatient);

        MedicalService mockService = new MedicalService();
        mockService.setRating(4.0);
        mockService.setNrOfRatings(5);

        Appointment mockAppointment = new Appointment();
        mockAppointment.setId(appointmentId);
        mockAppointment.setPatient(mockPatient);
        mockAppointment.setMedicalService(mockService);

        mockAppointment.setAppointmentFrom(OffsetDateTime.now().minusHours(1));

        when(appointmentService.findById(appointmentId)).thenReturn(Optional.of(mockAppointment));

        mockMvc.perform(post("/api/appointments/{appointmentId}/feedback", appointmentId)
                        .param("rating", String.valueOf(rating)))
                .andExpect(status().isOk())
                .andExpect(content().string("Feedback submitted successfully."));

        verify(medicalServiceService, times(1)).save(mockService);
        verify(appointmentService, times(1)).save(mockAppointment);
    }

    @Test
    public void testSubmitFeedback_Failure_TooSoon() throws Exception {
        Integer appointmentId = 1;
        float rating = 4.5f;

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn("pacient_test");

        Patient mockPatient = new Patient();
        mockPatient.setId(10);

        Appointment mockAppointment = new Appointment();
        mockAppointment.setId(appointmentId);
        mockAppointment.setPatient(mockPatient);
        mockAppointment.setAppointmentFrom(OffsetDateTime.now());
        mockAppointment.setMedicalService(new MedicalService());

        when(patientService.getPatientByUsername(anyString())).thenReturn(mockPatient);
        when(appointmentService.findById(any())).thenReturn(Optional.of(mockAppointment));

        mockMvc.perform(post("/api/appointments/" + appointmentId + "/feedback")
                        .param("rating", String.valueOf(rating)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Feedback can only be submitted after 30 minutes from the appointment start time."));
    }

    @Test
    public void testSubmitFeedback_Failure_AppointmentNotFound() throws Exception {
        Integer appointmentId = 1;
        float rating = 4.5f;

        when(appointmentService.findById(appointmentId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/appointments/{appointmentId}/feedback", appointmentId)
                        .param("rating", String.valueOf(rating)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Appointment not found or already completed."));
    }
}
