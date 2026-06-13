package com.unibuc.management.services;

import com.unibuc.management.entities.*;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DoctorScheduleServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PaidTimeOffRepository ptoRepository;
    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorScheduleService doctorScheduleService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(1);
        doctor.setNumberOfPtodays(10);
        MedicalService ms = new MedicalService();
        ms.setId(100);
        doctor.setMedicalService(ms);
    }

    @Test
    void testSchedulePTO_Success() {
        when(doctorService.getDoctorById(1)).thenReturn(doctor);

        when(appointmentRepository.findByDoctorIdAndDate(eq(1), any(), any()))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() ->
                doctorScheduleService.schedulePTO(1, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1))
        );

        verify(ptoRepository, times(1)).save(any(PaidTimeOff.class));
    }

    @Test
    void testSchedulePTO_ConflictingAppointments() {
        when(doctorService.getDoctorById(1)).thenReturn(doctor);

        when(appointmentRepository.findByDoctorIdAndDate(eq(1), any(), any()))
                .thenReturn(List.of(new Appointment()));

        assertThrows(InvalidActionException.class, () ->
                doctorScheduleService.schedulePTO(1, OffsetDateTime.now(), OffsetDateTime.now().plusDays(1))
        );
    }
}


