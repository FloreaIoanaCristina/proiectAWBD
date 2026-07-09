package com.unibuc.management.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import com.unibuc.management.dto.response.AppointmentResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.exceptions.UnauthorizedAccessException;
import com.unibuc.management.repositories.*;
import com.unibuc.management.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import com.unibuc.management.dto.request.AppointmentRequestDTO;
import com.unibuc.management.domain.*;
import com.unibuc.management.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    @Spy
    @InjectMocks
    private AppointmentService appointmentService;

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PaidTimeOffRepository ptoRepository;
    @Mock
    private DoctorService doctorService;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientService patientService;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private MedicalServiceService medicalServiceService;
    @Mock
    private MedicalServiceRepository medicalServiceRepository;
    @Mock
    private Authentication authentication;

    private Patient patient;
    private Doctor doctor;
    private MedicalService medicalService;
    @BeforeEach
    void setUp() {

        patient = new Patient();
        patient.setId(1);

        doctor = new Doctor();
        doctor.setId(2);

        medicalService = new MedicalService();
        medicalService.setId(1);
        medicalService.setStartHour(8);
        medicalService.setEndHour(18);
        medicalService.setRating(4.5);
        medicalService.setNrOfRatings(10);

    }
    @Test
    void findById_shouldReturnAppointment() {

        Appointment appointment = new Appointment();
        appointment.setId(10);

        when(appointmentRepository.findById(10))
                .thenReturn(Optional.of(appointment));

        Optional<AppointmentResponseDTO> result =
                appointmentService.findById(10);

        assertTrue(result.isPresent());
        assertEquals(10, result.get().getId());

        verify(appointmentRepository).findById(10);
    }
    @Test
    void findById_shouldReturnEmptyOptional() {

        when(appointmentRepository.findById(99))
                .thenReturn(Optional.empty());

        Optional<AppointmentResponseDTO> result =
                appointmentService.findById(99);

        assertTrue(result.isEmpty());

        verify(appointmentRepository).findById(99);
    }
    @Test
    void getAppointmentsByPatientIdPaged_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Appointment appointment = new Appointment();
        appointment.setId(1);

        Page<Appointment> page =
                new PageImpl<>(List.of(appointment));

        when(appointmentRepository.findByPatientId(1, pageable))
                .thenReturn(page);

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointmentsByPatientIdPaged(1, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getId());

        verify(appointmentRepository)
                .findByPatientId(1, pageable);
    }
    @Test
    void getAppointmentsByDoctorIdPaged_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Appointment appointment = new Appointment();
        appointment.setId(5);

        Page<Appointment> page =
                new PageImpl<>(List.of(appointment));

        when(appointmentRepository.findByDoctorId(2, pageable))
                .thenReturn(page);

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointmentsByDoctorIdPaged(2, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(5, result.getContent().get(0).getId());

        verify(appointmentRepository)
                .findByDoctorId(2, pageable);
    }

    @Test
    void submitFeedback_shouldUpdateRatingAndCompleteAppointment() {

        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);
        appointment.setAppointmentFrom(
                OffsetDateTime.now().minusHours(1));

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(patient));

        appointmentService.submitFeedback(1, 5.0f, "patient");

        assertEquals("Completed", appointment.getStatus());
        assertEquals(11, medicalService.getNrOfRatings());
        assertEquals(4.545454545454546,
                medicalService.getRating());

        verify(medicalServiceService).save(medicalService);
        verify(appointmentRepository).save(appointment);
    }
    @Test
    void submitFeedback_shouldThrowWhenAppointmentNotFound() {

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.submitFeedback(1, 5.0f, "patient")
        );

        verify(appointmentRepository).findById(1);
        verifyNoInteractions(medicalServiceService);
    }

    @Test
    void submitFeedback_shouldThrowWhenPatientNotFound() {

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);
        appointment.setAppointmentFrom(
                OffsetDateTime.now().minusHours(1));

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.submitFeedback(1, 5.0f, "patient")
        );

        verify(patientRepository)
                .findByUserUsername("patient");
    }

    @Test
    void submitFeedback_shouldThrowWhenAppointmentBelongsToAnotherPatient() {

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);
        appointment.setAppointmentFrom(
                OffsetDateTime.now().minusHours(1));

        Patient anotherPatient = new Patient();
        anotherPatient.setId(99);

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(anotherPatient));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> appointmentService.submitFeedback(1, 5.0f, "patient")
        );

        verify(appointmentRepository).findById(1);
    }

    @Test
    void submitFeedback_shouldThrowWhenAppointmentNotFinished() {

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);
        appointment.setAppointmentFrom(
                OffsetDateTime.now().minusMinutes(10));

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(patient));

        assertThrows(
                InvalidActionException.class,
                () -> appointmentService.submitFeedback(1, 5.0f, "patient")
        );

        verifyNoInteractions(medicalServiceService);
    }

    @Test
    void getAvailableTimeSlots_shouldReturnAllSlotsWhenNothingBlocksThem() {

        medicalService.setStartHour(8);
        medicalService.setEndHour(10);

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(medicalService);

        when(appointmentRepository.findByMedicalServiceAndDate(
                eq(1), any(), any()))
                .thenReturn(List.of());

        when(ptoRepository.findActivePtoForDoctorInDay(
                eq(2), any(), any()))
                .thenReturn(List.of());

        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .toLocalDate()
                .toString();

        List<OffsetDateTime> result =
                appointmentService.getAvailableTimeSlots(1, 2, date);

        assertEquals(4, result.size());

        verify(medicalServiceService).getMedicalServiceById(1);
    }

    @Test
    void getAvailableTimeSlots_shouldExcludeExistingAppointment() {

        medicalService.setStartHour(8);
        medicalService.setEndHour(10);

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(medicalService);

        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .toLocalDate()
                .toString();

        OffsetDateTime occupiedSlot =
                LocalDate.parse(date)
                        .atTime(8,30)
                        .atOffset(ZoneOffset.UTC);

        Appointment appointment = new Appointment();
        appointment.setAppointmentFrom(occupiedSlot);

        Doctor doctor = new Doctor();
        doctor.setId(2);

        appointment.setDoctor(doctor);

        when(appointmentRepository.findByMedicalServiceAndDate(
                eq(1), any(), any()))
                .thenReturn(List.of(appointment));

        when(ptoRepository.findActivePtoForDoctorInDay(
                eq(2), any(), any()))
                .thenReturn(List.of());

        List<OffsetDateTime> result =
                appointmentService.getAvailableTimeSlots(1,2,date);

        assertFalse(result.contains(occupiedSlot));
        assertEquals(3, result.size());
    }

    @Test
    void getAvailableTimeSlots_shouldExcludePtoSlots() {

        medicalService.setStartHour(8);
        medicalService.setEndHour(10);

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(medicalService);

        when(appointmentRepository.findByMedicalServiceAndDate(
                eq(1), any(), any()))
                .thenReturn(List.of());

        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .toLocalDate()
                .toString();

        PaidTimeOff pto = new PaidTimeOff();

        pto.setPtoFrom(
                LocalDate.parse(date)
                        .atTime(8,30)
                        .atOffset(ZoneOffset.UTC));

        pto.setPtoTo(
                LocalDate.parse(date)
                        .atTime(9,30)
                        .atOffset(ZoneOffset.UTC));

        when(ptoRepository.findActivePtoForDoctorInDay(
                eq(2), any(), any()))
                .thenReturn(List.of(pto));

        List<OffsetDateTime> result =
                appointmentService.getAvailableTimeSlots(1,2,date);

        assertEquals(2, result.size());

        assertFalse(result.contains(
                LocalDate.parse(date)
                        .atTime(8,30)
                        .atOffset(ZoneOffset.UTC)));

        assertFalse(result.contains(
                LocalDate.parse(date)
                        .atTime(9,0)
                        .atOffset(ZoneOffset.UTC)));
    }

    @Test
    void getAvailableTimeSlots_shouldWorkWithoutDoctor() {

        medicalService.setStartHour(8);
        medicalService.setEndHour(9);

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(medicalService);

        when(appointmentRepository.findByMedicalServiceAndDate(
                eq(1), any(), any()))
                .thenReturn(List.of());

        String date = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .toLocalDate()
                .toString();

        List<OffsetDateTime> result =
                appointmentService.getAvailableTimeSlots(1,null,date);

        assertEquals(2, result.size());

        verify(ptoRepository, never())
                .findActivePtoForDoctorInDay(any(), any(), any());
    }
    @Test
    void updateAppointment_shouldUpdateAppointment() {

        OffsetDateTime newDate = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setAppointmentFrom(newDate.minusDays(1));
        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(authentication.getName()).thenReturn("patient");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(Role.PATIENT);

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(patient));

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(medicalService);

        when(appointmentRepository.findByMedicalServiceAndDate(
                eq(1), any(), any()))
                .thenReturn(List.of());

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponseDTO updated =
                appointmentService.updateAppointment(1, newDate, authentication);

        assertEquals(newDate, updated.getAppointmentFrom());

        verify(appointmentRepository).save(appointment);
    }
    @Test
    void updateAppointment_shouldThrowWhenDateIsInPast() {

        OffsetDateTime yesterday =
                OffsetDateTime.now().minusDays(1);

        assertThrows(
                InvalidActionException.class,
                () -> appointmentService.updateAppointment(
                        1,
                        yesterday,
                        authentication)
        );

        verifyNoInteractions(appointmentRepository);
    }

    @Test
    void updateAppointment_shouldThrowWhenAppointmentNotFound() {

        OffsetDateTime tomorrow =
                OffsetDateTime.now().plusDays(1);

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.updateAppointment(
                        1,
                        tomorrow,
                        authentication)
        );

        verify(appointmentRepository).findById(1);
    }
    @Test
    void updateAppointment_shouldThrowWhenSlotUnavailable() {

        OffsetDateTime newDate = OffsetDateTime.now(ZoneOffset.UTC)
                .plusDays(1)
                .withHour(10)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setPatient(patient);
        appointment.setMedicalService(medicalService);

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(authentication.getName()).thenReturn("patient");

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(patient));

        when(medicalServiceService.getMedicalServiceById(1))
                .thenReturn(medicalService);

        Appointment occupied = new Appointment();
        occupied.setAppointmentFrom(newDate);

        when(appointmentRepository.findByMedicalServiceAndDate(
                eq(1), any(), any()))
                .thenReturn(List.of(occupied));

        assertThrows(
                InvalidActionException.class,
                () -> appointmentService.updateAppointment(
                        1,
                        newDate,
                        authentication)
        );
    }
    @Test
    void deleteAppointment_shouldDeleteAppointment_whenPatientOwnsAppointment() {

        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setPatient(patient);

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(authentication.getName())
                .thenReturn("patient");

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(patient));

        appointmentService.deleteAppointment(1, authentication);

        verify(appointmentRepository).delete(appointment);
    }
    @Test
    void deleteAppointment_shouldThrowWhenAppointmentNotFound() {

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> appointmentService.deleteAppointment(1, authentication)
        );

        verify(appointmentRepository, never()).delete(any());
    }

    @Test
    void deleteAppointment_shouldThrowWhenPatientDoesNotOwnAppointment() {

        Patient owner = new Patient();
        owner.setId(999);

        Appointment appointment = new Appointment();
        appointment.setId(1);
        appointment.setPatient(owner);

        when(appointmentRepository.findById(1))
                .thenReturn(Optional.of(appointment));

        when(authentication.getName())
                .thenReturn("patient");

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        when(patientRepository.findByUserUsername("patient"))
                .thenReturn(Optional.of(patient));

        assertThrows(
                UnauthorizedAccessException.class,
                () -> appointmentService.deleteAppointment(1, authentication)
        );

        verify(appointmentRepository, never()).delete(any());
    }
    @Test
    void getAppointmentsByPatientId_shouldReturnAppointmentsForOwnerPatient() {

        Pageable pageable = PageRequest.of(0, 10);

        Appointment appointment = new Appointment();
        appointment.setId(1);

        Page<Appointment> page =
                new PageImpl<>(List.of(appointment));

        when(authentication.getName())
                .thenReturn("patient");

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        when(patientService.getPatientByUsername("patient"))
                .thenReturn(patient);

        when(appointmentRepository.findByPatientId(1, pageable))
                .thenReturn(page);

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointmentsByPatientId(
                        1,
                        pageable,
                        authentication);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getId());

        verify(appointmentRepository)
                .findByPatientId(1, pageable);
    }

    @Test
    void getAppointmentsByPatientId_shouldFilterAppointmentsForDoctor() {

        Pageable pageable = PageRequest.of(0, 10);

        Doctor currentDoctor = new Doctor();
        currentDoctor.setId(5);

        MedicalService service = new MedicalService();
        service.setMedicalServiceDoctors(Set.of(currentDoctor));

        Appointment appointment = new Appointment();
        appointment.setMedicalService(service);

        Page<Appointment> page =
                new PageImpl<>(List.of(appointment));

        when(authentication.getName())
                .thenReturn("doctor");

        doReturn(Collections.singleton(Role.DOCTOR))
                .when(authentication)
                .getAuthorities();

        when(doctorService.getDoctorByUsername("doctor"))
                .thenReturn(currentDoctor);

        when(appointmentRepository.findByPatientId(1, pageable))
                .thenReturn(page);

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointmentsByPatientId(
                        1,
                        pageable,
                        authentication);

        assertEquals(1, result.getContent().size());

        verify(appointmentRepository)
                .findByPatientId(1, pageable);
    }

    @Test
    void getAppointmentsByDoctorId_shouldReturnAppointments() {

        Pageable pageable = PageRequest.of(0, 10);

        Doctor doctor = new Doctor();
        doctor.setId(5);

        Appointment appointment = new Appointment();
        appointment.setId(1);

        Page<Appointment> page = new PageImpl<>(List.of(appointment));

        when(authentication.getName())
                .thenReturn("doctor");

        when(doctorService.getDoctorByUsername("doctor"))
                .thenReturn(doctor);

        when(appointmentRepository.findByDoctorId(5, pageable))
                .thenReturn(page);

        Page<AppointmentResponseDTO> result =
                appointmentService.getAppointmentsByDoctorId(
                        5,
                        pageable,
                        authentication);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().get(0).getId());

        verify(appointmentRepository)
                .findByDoctorId(5, pageable);
    }
    @Test
    void getAppointmentsByDoctorId_shouldThrowWhenAccessingAnotherDoctor() {

        Pageable pageable = PageRequest.of(0, 10);

        Doctor currentDoctor = new Doctor();
        currentDoctor.setId(5);

        when(authentication.getName())
                .thenReturn("doctor");

        when(doctorService.getDoctorByUsername("doctor"))
                .thenReturn(currentDoctor);

        assertThrows(
                UnauthorizedAccessException.class,
                () -> appointmentService.getAppointmentsByDoctorId(
                        10,
                        pageable,
                        authentication)
        );

        verify(appointmentRepository, never())
                .findByDoctorId(anyInt(), any());
    }

    @Test
    void createAppointment_shouldCreateAppointment_whenPatientRole() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        OffsetDateTime slot = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).withMinute(0).withSecond(0).withNano(0);

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));

        when(dto.getMedicalServiceId()).thenReturn(1);
        when(dto.getDoctorId()).thenReturn(null);
        when(dto.getAppointmentFrom()).thenReturn(slot);

        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));

        doReturn(List.of(slot))
                .when(appointmentService)
                .getAvailableTimeSlots(anyInt(), any(), any());

        when(paymentService.createPaymentForPatient(any(), any(), any()))
                .thenReturn(new Payment());

        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AppointmentResponseDTO result = appointmentService.createAppointment(authentication, dto);

        assertEquals(patient.getId(), result.getPatient().getId());
        assertEquals(medicalService.getId(), result.getMedicalService().getId());
    }
    @Test
    void createAppointment_shouldCreateAppointment_whenStaffProvidesPatientId() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        OffsetDateTime slot = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).withMinute(0).withSecond(0).withNano(0);

        doReturn(Collections.singleton(Role.DOCTOR))
                .when(authentication)
                .getAuthorities();

        when(dto.getPatientId()).thenReturn(1);
        when(dto.getMedicalServiceId()).thenReturn(1);
        when(dto.getDoctorId()).thenReturn(2);
        when(dto.getAppointmentFrom()).thenReturn(slot);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));
        when(doctorRepository.findById(2)).thenReturn(Optional.of(doctor));

        doReturn(List.of(slot))
                .when(appointmentService)
                .getAvailableTimeSlots(anyInt(), any(), any());

        when(paymentService.createPaymentForPatient(any(), any(), any()))
                .thenReturn(new Payment());

        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AppointmentResponseDTO result = appointmentService.createAppointment(authentication, dto);

        assertNotNull(result.getDoctor());
    }
    @Test
    void createAppointment_shouldThrow_whenStaffMissingPatientId() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        doReturn(Collections.singleton(Role.DOCTOR))
                .when(authentication)
                .getAuthorities();

        when(dto.getPatientId()).thenReturn(null);

        assertThrows(InvalidActionException.class,
                () -> appointmentService.createAppointment(authentication, dto));
    }

    @Test
    void createAppointment_shouldThrow_whenPatientNotFoundForAuthenticatedUser() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        OffsetDateTime slot = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> appointmentService.createAppointment(authentication, dto));
    }

    @Test
    void createAppointment_shouldThrow_whenMedicalServiceNotFound() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);

        when(patientRepository.findByUserId(1L))
                .thenReturn(Optional.of(patient));

        when(dto.getMedicalServiceId()).thenReturn(1);

        when(medicalServiceRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> appointmentService.createAppointment(authentication, dto));
    }

    @Test
    void createAppointment_shouldThrow_whenSlotNotAvailable() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        OffsetDateTime slot = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        doReturn(Collections.singleton(Role.PATIENT))
                .when(authentication)
                .getAuthorities();

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(1L);

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));

        when(dto.getMedicalServiceId()).thenReturn(1);
        when(dto.getDoctorId()).thenReturn(null);
        when(dto.getAppointmentFrom()).thenReturn(slot);

        doReturn(List.of())
                .when(appointmentService)
                .getAvailableTimeSlots(anyInt(), any(), any());

        assertThrows(InvalidActionException.class,
                () -> appointmentService.createAppointment(authentication, dto));
    }

    @Test
    void createAppointment_shouldThrow_whenDoctorNotFound() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        OffsetDateTime slot = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        doReturn(Collections.singleton(Role.DOCTOR))
                .when(authentication)
                .getAuthorities();

        when(dto.getPatientId()).thenReturn(1);
        when(dto.getMedicalServiceId()).thenReturn(1);
        when(dto.getDoctorId()).thenReturn(2);
        when(dto.getAppointmentFrom()).thenReturn(slot);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));
        when(doctorRepository.findById(2)).thenReturn(Optional.empty());

        doReturn(List.of(slot))
                .when(appointmentService)
                .getAvailableTimeSlots(anyInt(), any(), any());

        assertThrows(EntityNotFoundException.class,
                () -> appointmentService.createAppointment(authentication, dto));
    }

    @Test
    void createAppointment_shouldCreateAppointment_whenStaffWithoutDoctor() {

        AppointmentRequestDTO dto = mock(AppointmentRequestDTO.class);

        OffsetDateTime slot = OffsetDateTime.now(ZoneOffset.UTC).plusDays(1);

        doReturn(Collections.singleton(Role.DOCTOR))
                .when(authentication)
                .getAuthorities();

        when(dto.getPatientId()).thenReturn(1);
        when(dto.getMedicalServiceId()).thenReturn(1);
        when(dto.getDoctorId()).thenReturn(null);
        when(dto.getAppointmentFrom()).thenReturn(slot);

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(medicalServiceRepository.findById(1)).thenReturn(Optional.of(medicalService));

        doReturn(List.of(slot))
                .when(appointmentService)
                .getAvailableTimeSlots(anyInt(), any(), any());

        when(paymentService.createPaymentForPatient(any(), any(), any()))
                .thenReturn(new Payment());

        when(appointmentRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AppointmentResponseDTO result = appointmentService.createAppointment(authentication, dto);

        assertNull(result.getDoctor());
    }
}
