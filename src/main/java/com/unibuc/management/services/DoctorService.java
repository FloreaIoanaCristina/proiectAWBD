package com.unibuc.management.services;

import com.unibuc.management.dto.request.DoctorRequestDTO;
import com.unibuc.management.domain.Doctor;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.domain.User;
import com.unibuc.management.dto.response.DoctorResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.mappers.AppointmentMapper;
import com.unibuc.management.mappers.DoctorMapper;
import com.unibuc.management.mappers.MedicalServiceMapper;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final MedicalServiceRepository medicalServiceRepository;
    private final AppointmentRepository appointmentRepository;

    public DoctorResponseDTO createDoctor(DoctorRequestDTO doctorRequestDTO) {
        log.info("Se salvează/actualizează direct entitatea Doctor cu numele: {}", doctorRequestDTO.getName());

        Doctor doctor = new Doctor();
        doctor.setName(doctorRequestDTO.getName());
        doctor.setOffice(doctorRequestDTO.getOffice());
        doctor.setNumberOfPTOdays(doctorRequestDTO.getNumberOfPtodays());

        Integer serviceId = doctorRequestDTO.getMedicalServiceId();

        MedicalService medicalService = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.error("Eroare: Serviciul medical cu ID-ul {} nu a fost găsit în sistem.", serviceId);
                    return new ResourceNotFoundException("Serviciul medical cu ID-ul " + serviceId + " nu a fost găsit.");
                });
        doctor.setMedicalService(medicalService);

        if (doctorRequestDTO.getUserId() != null) {
            User user = userRepository.findById(doctorRequestDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul cu ID-ul " + doctorRequestDTO.getUserId() + " nu există."));
            doctor.setUser(user);
            log.debug("Relația cu User ID-ul '{}' a fost stabilită cu succes.", doctorRequestDTO.getUserId());
        }

        Doctor savedDoctor = doctorRepository.save(doctor);
        return DoctorMapper.toResponseDTO(savedDoctor);
    }

    public Doctor getDoctorById(Integer doctorId) {
        log.debug("Căutare doctor după ID: {}", doctorId);
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> {
                    log.error("Eroare interogare: Doctorul cu ID-ul {} nu a fost găsit în sistem.", doctorId);
                    return new ResourceNotFoundException("Doctorul cu ID-ul " + doctorId + " nu a fost găsit.");
                });
        return doctor;
    }

    public List<DoctorResponseDTO> getAllDoctors() {
        log.debug("Se preia lista completă a doctorilor.");
        List<Doctor> doctors = doctorRepository.findAll();
        log.debug("S-au găsit {} doctori în sistem.", doctors.size());
        return doctors.stream()
                .map(DoctorMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public DoctorResponseDTO updateDoctor(Integer id, DoctorRequestDTO doctorRequestDTO) {
        log.debug("Se solicită actualizarea detaliilor pentru doctorul cu ID-ul: {}", id);
        Doctor existingDoctor = getDoctorById(id);

        existingDoctor.setName(doctorRequestDTO.getName());
        existingDoctor.setOffice(doctorRequestDTO.getOffice());
        existingDoctor.setNumberOfPTOdays(doctorRequestDTO.getNumberOfPtodays());
        Integer serviceId = doctorRequestDTO.getMedicalServiceId();

        MedicalService medicalService = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.error("Eroare la actualizare: Serviciul medical cu ID-ul {} nu a fost găsit în sistem.", serviceId);
                    return new ResourceNotFoundException("Serviciul medical cu ID-ul " + serviceId + " nu a fost găsit.");
                });

        existingDoctor.setMedicalService(medicalService);
        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        log.info("Doctorul cu ID-ul {} a fost actualizat cu succes (Nume nou: {}).", id, updatedDoctor.getName());
        return DoctorMapper.toResponseDTO(updatedDoctor);
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        log.debug("Se inițiază procedura de ștergere pentru doctorul cu ID-ul: {}", id);
        Doctor doctor = getDoctorById(id);
        User user = doctor.getUser();

        long appointmentCount = appointmentRepository.countByDoctorId(id);
        if (appointmentCount > 0) {
            throw new InvalidActionException("Nu se poate șterge medicul deoarece are " + appointmentCount + " programări înregistrate în sistem.");
        }

        if (user != null) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

            boolean isDoctor = authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

            if (isDoctor && !user.getUsername().equals(currentUsername)) {
                throw new InvalidActionException(
                        "Nu aveți permisiunea să ștergeți profilul unui alt medic care are cont activ.");
            }

            log.info("Se șterge utilizatorul asociat '{}' (ID: {}) pentru a elimina doctorul în cascadă.",
                    user.getUsername(), user.getId());
            userRepository.delete(user);
        } else {
            log.warn("Doctorul cu ID-ul {} nu are un cont de User asociat. Se șterge doar entitatea Doctor.", id);
            doctorRepository.delete(doctor);
        }
    }

    public Doctor getDoctorByUsername(String username) {
        log.debug("Căutare doctor după username-ul de autentificare: {}", username);
        return doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Eroare interogare: Nu există niciun doctor asociat username-ului '{}'.", username);
                    return new ResourceNotFoundException("Doctorul cu username-ul " + username + " nu a fost găsit.");
                });
    }

    public Page<DoctorResponseDTO> getDoctorsPaged(Pageable pageable) {
        log.debug("Se solicită lista paginată de doctori folosind structura Pageable.");
        return doctorRepository.findAllWithServicesPaged(pageable).map(DoctorMapper::toResponseDTO);
    }
}