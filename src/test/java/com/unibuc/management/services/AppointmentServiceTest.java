package com.unibuc.management.services;

import com.unibuc.management.entities.*;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.repositories.*;
import com.unibuc.management.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.unibuc.management.dto.validation.AppointmentRequestDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PaidTimeOffRepository ptoRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private PaymentService paymentService;
    @Mock private MedicalServiceService medicalServiceService;
    @Mock private PatientRepository patientRepository;
    @Mock private MedicalServiceRepository medicalServiceRepository;
    @InjectMocks
    private AppointmentService appointmentService;

    private MedicalService medicalService;
    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        appointmentService = new AppointmentService(
            appointmentRepository,
            ptoRepository,
            doctorRepository,
            patientRepository,
            paymentService,
            medicalServiceService,
            medicalServiceRepository
        );

        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setStartHour(9);
        medicalService.setEndHour(17);

        patient = new Patient();
        patient.setId(1);
        patient.setName("Test Patient");
    }

    @Test
    void testCreateAppointment_Success() {
        OffsetDateTime slotTime = OffsetDateTime.of(2025, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC);

        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setMedicalServiceId(1);
        dto.setDoctorId(2);
        dto.setAppointmentFrom(slotTime);

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(1L, "patientUser", "password", Collections.emptyList());

        when(auth.getPrincipal()).thenReturn(userDetails);
        when(auth.getAuthorities()).thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")));

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));
        when(doctorRepository.findById(2)).thenReturn(Optional.of(new Doctor()));
        when(paymentService.createPaymentForPatient(any(), any(), any())).thenReturn(new Payment());

        AppointmentService spyService = spy(appointmentService);
        doReturn(Collections.singletonList(slotTime)).when(spyService)
                .getAvailableTimeSlots(any(), anyString());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(i -> i.getArguments()[0]);

        Appointment result = spyService.createAppointment(auth, dto);

        assertNotNull(result);
        assertEquals("Appointed", result.getStatus());
        verify(appointmentRepository).save(any(Appointment.class));
        verify(paymentService).createPaymentForPatient(eq(patient), eq(medicalService), any());
    }

    @Test
    void testCreateAppointment_InvalidSlot_ThrowsException() {
        OffsetDateTime invalidSlot = OffsetDateTime.of(2025, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC);

        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setMedicalServiceId(1);
        dto.setDoctorId(2);
        dto.setAppointmentFrom(invalidSlot);

        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(1L, "patientUser", "password", Collections.emptyList());

        when(auth.getPrincipal()).thenReturn(userDetails);
        when(auth.getAuthorities()).thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_PATIENT")));

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));

        AppointmentService spyService = spy(appointmentService);
        doReturn(Collections.emptyList()).when(spyService)
                .getAvailableTimeSlots(any(), anyString());

        assertThrows(InvalidActionException.class, () -> {
            spyService.createAppointment(auth, dto);
        });
    }

    @Test
    void testSubmitFeedback_Success() {
        Integer apptId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(apptId);
        appointment.setPatient(patient);
        appointment.setAppointmentFrom(OffsetDateTime.now().minusMinutes(35));
        medicalService.setRating(4.5);
        medicalService.setNrOfRatings(10);

        appointment.setMedicalService(medicalService);

        when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));

        appointmentService.submitFeedback(apptId, 5.0f, patient.getId());

        assertEquals("Completed", appointment.getStatus());
        verify(medicalServiceService).save(any(MedicalService.class));
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void testSubmitFeedback_TooSoon_ThrowsException() {
        Integer apptId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(apptId);
        appointment.setPatient(patient);
        appointment.setAppointmentFrom(OffsetDateTime.now());

        when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));

        assertThrows(InvalidActionException.class, () -> {
            appointmentService.submitFeedback(apptId, 5.0f, patient.getId());
        });
    }

    @Test
    void testGetAvailableTimeSlots() {
        String date = "2027-01-14";

        medicalService.setId(1);
        medicalService.setStartHour(9);
        medicalService.setEndHour(17);

        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentFrom(OffsetDateTime.of(2027, 1, 14, 9, 0, 0, 0, ZoneOffset.UTC));

        when(appointmentRepository.findByMedicalServiceAndDate(eq(1), any(), any()))
                .thenReturn(Collections.singletonList(existingAppointment));

        PaidTimeOff pto = new PaidTimeOff();
        pto.setPtoFrom(OffsetDateTime.of(2027, 1, 14, 10, 0, 0, 0, ZoneOffset.UTC));
        pto.setPtoTo(OffsetDateTime.of(2027, 1, 14, 12, 0, 0, 0, ZoneOffset.UTC));

        when(ptoRepository.findByDoctorAndDate(eq(1), any(), any()))
                .thenReturn(Collections.singletonList(pto));

        Doctor doctor = new Doctor();
        doctor.setId(1);
        when(doctorRepository.findByMedicalServiceId(eq(1))).thenReturn(Optional.of(doctor));

        List<OffsetDateTime> availableSlots = appointmentService.getAvailableTimeSlots(medicalService, date);

        assertNotNull(availableSlots);
        assertTrue(availableSlots.size() > 0, "Lista de sloturi nu ar trebui să fie goală!");
        assertEquals(OffsetDateTime.of(2027, 1, 14, 9, 30, 0, 0, ZoneOffset.UTC), availableSlots.get(0));
        assertEquals(OffsetDateTime.of(2027, 1, 14, 16, 30, 0, 0, ZoneOffset.UTC), availableSlots.get(availableSlots.size() - 1));
    }

    @Test
    void testGetAppointmentsByPatientId() {
        Integer patientId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(patientId);

        Page<Appointment> appointmentPage = new PageImpl<>(Collections.singletonList(appointment));
        Pageable pageable = PageRequest.of(0, 10);

        when(appointmentRepository.findByPatientId(patientId, pageable)).thenReturn(appointmentPage);

        Page<Appointment> result = appointmentService.getAppointmentsByPatientIdPaged(patientId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(101, result.getContent().get(0).getId());

        verify(appointmentRepository, times(1)).findByPatientId(patientId, pageable);
    }

    @Test
    void testFindById_Success() {
        Integer appointmentId = 1;
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        Optional<Appointment> foundAppointment = appointmentService.findById(appointmentId);

        assertTrue(foundAppointment.isPresent());
        assertEquals(appointmentId, foundAppointment.get().getId());
    }

    @Test
    void testFindById_NotFound() {
        Integer appointmentId = 1;

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        Optional<Appointment> foundAppointment = appointmentService.findById(appointmentId);

        assertFalse(foundAppointment.isPresent());
    }

    @Test
    void testSaveAppointment() {
        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setMedicalService(medicalService);

        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        Appointment savedAppointment = appointmentService.save(appointment);

        assertNotNull(savedAppointment);
        assertEquals(1, savedAppointment.getId());
        verify(appointmentRepository, times(1)).save(appointment);
    }
}
