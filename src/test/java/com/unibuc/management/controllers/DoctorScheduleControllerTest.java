package com.unibuc.management.controllers;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.services.DoctorScheduleService;
import com.unibuc.management.services.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DoctorScheduleControllerTest {

    @Mock
    private DoctorScheduleService doctorScheduleService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DoctorScheduleController doctorScheduleController;

    private MockMvc mockMvc;
    private Doctor doctor;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(doctorScheduleController).build();
        doctor = new Doctor();
        doctor.setId(1);
        doctor.setOffice("Cardiology");
    }

    @Test
    public void getDoctorScheduleForDay_ReturnsSchedule() throws Exception {
        OffsetDateTime fromTime1 = OffsetDateTime.parse("2025-01-14T09:00:00+00:00");
        OffsetDateTime toTime1 = OffsetDateTime.parse("2025-01-14T09:30:00+00:00");

        OffsetDateTime fromTime2 = OffsetDateTime.parse("2025-01-14T10:00:00+00:00");
        OffsetDateTime toTime2 = OffsetDateTime.parse("2025-01-14T10:30:00+00:00");

        ScheduleEntry entry1 = new ScheduleEntry("Doctor's Appointment", fromTime1, toTime1, doctor);
        ScheduleEntry entry2 = new ScheduleEntry("Doctor's Appointment", fromTime2, toTime2, doctor);
        List<ScheduleEntry> schedule = Arrays.asList(entry1, entry2);

        when(doctorScheduleService.getDoctorScheduleForDay(1, LocalDate.of(2025, 1, 14)))
                .thenReturn(schedule);

        mockMvc.perform(get("/api/doctor-schedule/day")
                        .param("doctorId", "1")
                        .param("date", "2025-01-14"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2025-01-14T09:00:00")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2025-01-14T10:00:00")));
    }

    @Test
    public void schedulePTO_Success_ReturnsSuccessMessage() throws Exception {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("doctor_user");
        SecurityContextHolder.setContext(securityContext);

        when(doctorService.getDoctorByUsername("doctor_user")).thenReturn(doctor);
        Mockito.doNothing().when(doctorScheduleService).schedulePTO(eq(1), any(), any());

        mockMvc.perform(post("/api/doctor-schedule/schedulePTO")
                        .param("doctorId", "1")
                        .param("startDate", "2025-01-14T10:00:00+00:00")
                        .param("endDate", "2025-01-14T12:00:00+00:00"))
                .andExpect(status().isOk())
                .andExpect(content().string("Concediu programat cu succes!"));
    }

    @Test
    public void schedulePTO_Failure_ReturnsBadRequest() throws Exception {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("doctor_user");
        SecurityContextHolder.setContext(securityContext);

        when(doctorService.getDoctorByUsername("doctor_user")).thenReturn(doctor);

        Mockito.doThrow(new InvalidActionException("Doctor has appointments"))
                .when(doctorScheduleService).schedulePTO(eq(1), any(), any());

        jakarta.servlet.ServletException exception = org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.servlet.ServletException.class,
                () -> {
                    mockMvc.perform(post("/api/doctor-schedule/schedulePTO")
                            .param("doctorId", "1")
                            .param("startDate", "2025-01-14T10:00:00+00:00")
                            .param("endDate", "2025-01-14T12:00:00+00:00"));
                }
        );

        org.junit.jupiter.api.Assertions.assertTrue(exception.getCause() instanceof InvalidActionException);
        org.junit.jupiter.api.Assertions.assertEquals("Doctor has appointments", exception.getCause().getMessage());
    }
}