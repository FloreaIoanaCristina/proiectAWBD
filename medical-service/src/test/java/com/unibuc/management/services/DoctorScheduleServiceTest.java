package com.unibuc.management.services;

import com.unibuc.management.domain.*;
import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.dto.request.PtoRequestDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorScheduleServiceTest {

    @InjectMocks
    private DoctorScheduleService doctorScheduleService;

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PaidTimeOffRepository ptoRepository;
    @Mock private DoctorService doctorService;
    @Mock private DoctorRepository doctorRepository;

    private Doctor doctor;
    private Appointment appointment;
    private PaidTimeOff pto;
    private PtoRequestDTO dto;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(1);
        doctor.setNumberOfPTOdays(10);

        MedicalService ms = new MedicalService();
        ms.setId(100);
        doctor.setMedicalService(ms);

        appointment = new Appointment();
        appointment.setAppointmentFrom(OffsetDateTime.parse("2026-07-05T08:00Z"));

        pto = new PaidTimeOff();
        pto.setPtoFrom(OffsetDateTime.parse("2026-07-05T10:00Z"));
        pto.setPtoTo(OffsetDateTime.parse("2026-07-05T12:00Z"));

        dto = new PtoRequestDTO();
        dto.setDoctorId(1);
        dto.setStartDate(LocalDate.of(2026, 7, 10));
        dto.setEndDate(LocalDate.of(2026, 7, 12));
    }

    @Test
    void getDoctorScheduleForDay_shouldReturnMergedSchedule() {
        when(doctorService.getDoctorById(1)).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndDate(anyInt(), any(), any()))
                .thenReturn(List.of(appointment));
        when(ptoRepository.findActivePtoForDoctorInDay(anyInt(), any(), any()))
                .thenReturn(List.of(pto));

        List<ScheduleEntry> result =
                doctorScheduleService.getDoctorScheduleForDay(1, "2026-07-05");

        assertEquals(2, result.size());
    }

    @Test
    void getDoctorScheduleForDay_shouldThrow_whenInvalidDate() {
        when(doctorService.getDoctorById(1)).thenReturn(doctor);

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.getDoctorScheduleForDay(1, "07-05-2026"));
    }

    @Test
    void schedulePTO_shouldCreatePTO_whenValid() {
        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);
        when(doctorService.getDoctorById(1)).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndDate(anyInt(), any(), any()))
                .thenReturn(List.of());

        doctorScheduleService.schedulePTO(dto, "doc");

        verify(ptoRepository).save(any(PaidTimeOff.class));
        verify(doctorRepository).save(doctor);
    }

    @Test
    void schedulePTO_shouldThrow_whenDifferentDoctor() {
        Doctor current = new Doctor();
        current.setId(2);

        when(doctorService.getDoctorByUsername("doc")).thenReturn(current);

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.schedulePTO(dto, "doc"));
    }

    @Test
    void schedulePTO_shouldThrow_whenEndBeforeStart() {
        dto.setEndDate(LocalDate.of(2026, 7, 1));

        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.schedulePTO(dto, "doc"));
    }

    @Test
    void schedulePTO_shouldThrow_whenAppointmentsExist() {
        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);
        when(doctorService.getDoctorById(1)).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndDate(anyInt(), any(), any()))
                .thenReturn(List.of(new Appointment()));

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.schedulePTO(dto, "doc"));
    }

    @Test
    void schedulePTO_shouldThrow_whenNotEnoughDays() {
        doctor.setNumberOfPTOdays(1);

        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);
        when(doctorService.getDoctorById(1)).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndDate(anyInt(), any(), any()))
                .thenReturn(List.of());

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.schedulePTO(dto, "doc"));
    }

    @Test
    void updatePTO_shouldUpdateSuccessfully() {
        PaidTimeOff existing = new PaidTimeOff();
        existing.setId(1);
        existing.setDoctor(doctor);
        existing.setPtoFrom(OffsetDateTime.parse("2026-07-01T00:00Z"));
        existing.setPtoTo(OffsetDateTime.parse("2026-07-02T00:00Z"));

        when(ptoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);
        when(appointmentRepository.findByDoctorIdAndDate(anyInt(), any(), any()))
                .thenReturn(List.of());

        PtoRequestDTO updateDto = new PtoRequestDTO();
        updateDto.setStartDate(LocalDate.of(2026, 7, 10));
        updateDto.setEndDate(LocalDate.of(2026, 7, 12));

        doctorScheduleService.updatePTO(1, updateDto, "doc");

        verify(ptoRepository).save(existing);
    }

    @Test
    void deletePTO_shouldDeleteSuccessfully() {
        PaidTimeOff existing = new PaidTimeOff();
        existing.setDoctor(doctor);
        existing.setPtoFrom(OffsetDateTime.now().plusDays(1));
        existing.setPtoTo(OffsetDateTime.now().plusDays(2));

        when(ptoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);

        doctorScheduleService.deletePTO(1, "doc");

        verify(ptoRepository).delete(existing);
        verify(doctorRepository).save(doctor);
    }

    @Test
    void deletePTO_shouldThrow_whenPtoInPast() {
        PaidTimeOff existing = new PaidTimeOff();
        existing.setDoctor(doctor);
        existing.setPtoFrom(OffsetDateTime.now().minusDays(2));

        when(ptoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(doctorService.getDoctorByUsername("doc")).thenReturn(doctor);

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.deletePTO(1, "doc"));
    }

    @Test
    void deletePTO_shouldThrow_whenNotOwner() {
        Doctor current = new Doctor();
        current.setId(2);

        PaidTimeOff existing = new PaidTimeOff();
        existing.setDoctor(doctor);

        when(ptoRepository.findById(1)).thenReturn(Optional.of(existing));
        when(doctorService.getDoctorByUsername("doc")).thenReturn(current);

        assertThrows(InvalidActionException.class,
                () -> doctorScheduleService.deletePTO(1, "doc"));
    }
}