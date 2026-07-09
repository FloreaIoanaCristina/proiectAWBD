package com.unibuc.management.services;

import com.unibuc.management.dto.request.DoctorRequestDTO;
import com.unibuc.management.domain.Doctor;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.domain.User;
import com.unibuc.management.dto.response.DoctorResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @InjectMocks
    private DoctorService doctorService;

    @Mock private DoctorRepository doctorRepository;
    @Mock private UserRepository userRepository;
    @Mock private MedicalServiceRepository medicalServiceRepository;
    @Mock private AppointmentRepository appointmentRepository;

    private Doctor doctor;
    private DoctorRequestDTO dto;
    private MedicalService medicalService;
    private User user;

    @BeforeEach
    void setUp() {

        medicalService = new MedicalService();
        medicalService.setId(100);

        user = new User();
        user.setId(1L);
        user.setUsername("doc");

        doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr. House");
        doctor.setOffice("A1");
        doctor.setNumberOfPTOdays(10);
        doctor.setMedicalService(medicalService);
        doctor.setUser(user);

        dto = new DoctorRequestDTO();
        dto.setName("Dr. House");
        dto.setOffice("A1");
        dto.setNumberOfPtodays(10);
        dto.setMedicalServiceId(100);
    }
    @Test
    void createDoctor_shouldCreateSuccessfully() {

        dto.setUserId(1L);

        when(medicalServiceRepository.findById(100))
                .thenReturn(Optional.of(medicalService));

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(doctorRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        DoctorResponseDTO result = doctorService.createDoctor(dto);

        assertEquals("Dr. House", result.getName());
        assertEquals(user.getId(), result.getUserId());
        assertEquals(medicalService.getId(), result.getMedicalService().getId());
    }

    @Test
    void createDoctor_shouldThrow_whenMedicalServiceNotFound() {

        when(medicalServiceRepository.findById(100))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.createDoctor(dto));
    }

    @Test
    void getDoctorById_shouldReturnDoctor() {

        when(doctorRepository.findById(1))
                .thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getDoctorById(1);

        assertEquals(1, result.getId());
    }

    @Test
    void getDoctorById_shouldThrow_whenNotFound() {

        when(doctorRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.getDoctorById(1));
    }

    @Test
    void getAllDoctors_shouldReturnList() {

        when(doctorRepository.findAll())
                .thenReturn(List.of(doctor));

        List<DoctorResponseDTO> result = doctorService.getAllDoctors();

        assertEquals(1, result.size());
    }

    @Test
    void getDoctorByUsername_shouldReturnDoctor() {

        when(doctorRepository.findByUserUsername("doc"))
                .thenReturn(Optional.of(doctor));

        Doctor result = doctorService.getDoctorByUsername("doc");

        assertEquals(1, result.getId());
    }

    @Test
    void getDoctorsPaged_shouldReturnPage() {

        Page<Doctor> page = new PageImpl<>(List.of(doctor));

        when(doctorRepository.findAllWithServicesPaged(any()))
                .thenReturn(page);

        Page<DoctorResponseDTO> result = doctorService.getDoctorsPaged(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateDoctor_shouldUpdateSuccessfully() {

        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(medicalServiceRepository.findById(100))
                .thenReturn(Optional.of(medicalService));

        when(doctorRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        DoctorResponseDTO result = doctorService.updateDoctor(1, dto);

        assertEquals("Dr. House", result.getName());
        assertEquals(medicalService.getId(), result.getMedicalService().getId());
    }

    @Test
    void updateDoctor_shouldThrow_whenNotFound() {

        when(doctorRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.updateDoctor(1, dto));
    }
    @Test
    void deleteDoctor_shouldThrow_whenHasAppointments() {

        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.countByDoctorId(1)).thenReturn(2L);

        assertThrows(InvalidActionException.class,
                () -> doctorService.deleteDoctor(1));
    }

    @Test
    void deleteDoctor_shouldDeleteUser_whenUserExists() {

        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.countByDoctorId(1)).thenReturn(0L);

        mockAuth("doc", "ROLE_DOCTOR");

        doctorService.deleteDoctor(1);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteDoctor_shouldDeleteDoctor_whenNoUser() {

        Doctor noUserDoctor = new Doctor();
        noUserDoctor.setId(2);
        noUserDoctor.setUser(null);

        when(doctorRepository.findById(2)).thenReturn(Optional.of(noUserDoctor));
        when(appointmentRepository.countByDoctorId(2)).thenReturn(0L);

        doctorService.deleteDoctor(2);

        verify(doctorRepository).delete(noUserDoctor);
    }


    private void mockAuth(String username, String role) {
        var auth = mock(org.springframework.security.core.Authentication.class);

        when(auth.getName()).thenReturn(username);
        when(auth.getAuthorities()).thenAnswer(inv -> List.of(
                (org.springframework.security.core.GrantedAuthority)
                        () -> role
        ));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}