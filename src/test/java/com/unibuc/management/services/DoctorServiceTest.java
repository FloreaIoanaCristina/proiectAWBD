package com.unibuc.management.services;

import com.unibuc.management.dto.validation.DoctorRequestDTO;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.User;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private MedicalServiceRepository medicalServiceRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doctorService = new DoctorService(doctorRepository, userRepository,medicalServiceRepository);

        doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("Dr. Smith");
        doctor.setOffice("Room 101");
        doctor.setUser(new User());
    }

    @Test
    void testGetDoctorById_NotFound() {
        when(doctorRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> doctorService.getDoctorById(1));
    }

    @Test
    void testUpdateDoctor_Success() {
        MedicalService mockService = new MedicalService();
        mockService.setId(5);
        DoctorRequestDTO dto = new DoctorRequestDTO();
        dto.setName("New Name");
        dto.setOffice("Cabinet 102");
        dto.setNumberOfPtodays(25);
        dto.setMedicalServiceId(5);

        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));
        when(medicalServiceRepository.findById(5)).thenReturn(Optional.of(mockService));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

        Doctor result = doctorService.updateDoctor(1, dto);

        assertNotNull(result);
        assertEquals("New Name", doctor.getName(), "Numele doctorului existent ar trebui să fie actualizat cu cel din DTO.");
        assertEquals("Cabinet 102", doctor.getOffice(), "Cabinetul ar trebui să fie actualizat.");
        assertEquals(25, doctor.getNumberOfPtodays(), "Zilele de concediu ar trebui să fie actualizate.");
        assertEquals(mockService, doctor.getMedicalService(), "Relația cu Serviciul Medical ar trebui să fie mapată corect.");

        verify(doctorRepository).save(doctor);
        verify(medicalServiceRepository).findById(5);
    }

    @Test
    void testDeleteDoctor_Success() {
        when(doctorRepository.findById(1)).thenReturn(Optional.of(doctor));

        assertDoesNotThrow(() -> doctorService.deleteDoctor(1));

        verify(userRepository, times(1)).delete(any(User.class));
    }

    @Test
    void testGetDoctorsPaged() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Doctor> doctorPage = new PageImpl<>(List.of(doctor));

        when(doctorRepository.findAll(any(Pageable.class))).thenReturn(doctorPage);

        Page<Doctor> result = doctorService.getDoctorsPaged(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Dr. Smith", result.getContent().get(0).getName());
        verify(doctorRepository).findAll(any(Pageable.class));
    }
}
