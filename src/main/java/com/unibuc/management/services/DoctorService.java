package com.unibuc.management.services;

import com.unibuc.management.dto.validation.DoctorRequestDTO;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.User;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         MedicalServiceRepository medicalServiceRepository) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    public Doctor saveDoctor(DoctorRequestDTO doctorRequestDTO) {
        log.info("Se salvează/actualizează direct entitatea Doctor cu numele: {}", doctorRequestDTO.getName());

        Doctor doctor = new Doctor();
        doctor.setName(doctorRequestDTO.getName());
        doctor.setOffice(doctorRequestDTO.getOffice());
        doctor.setNumberOfPtodays(doctorRequestDTO.getNumberOfPtodays());

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

        return doctorRepository.save(doctor);
    }

    public Doctor getDoctorById(Integer doctorId) {
        log.debug("Căutare doctor după ID: {}", doctorId);
        return doctorRepository.findById(doctorId)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Doctorul cu ID-ul {} nu a fost găsit în sistem.", doctorId);
                return new ResourceNotFoundException("Doctorul cu ID-ul " + doctorId + " nu a fost găsit.");
            });
    }

    public List<Doctor> getAllDoctors() {
        log.debug("Se preia lista completă a doctorilor.");
        List<Doctor> doctors = doctorRepository.findAll();
        log.debug("S-au găsit {} doctori în sistem.", doctors.size());
        return doctors;
    }

    @Transactional
    public Doctor updateDoctor(Integer id, DoctorRequestDTO doctorRequestDTO) {
        log.debug("Se solicită actualizarea detaliilor pentru doctorul cu ID-ul: {}", id);
        Doctor existingDoctor = getDoctorById(id);

        existingDoctor.setName(doctorRequestDTO.getName());
        existingDoctor.setOffice(doctorRequestDTO.getOffice());
        existingDoctor.setNumberOfPtodays(doctorRequestDTO.getNumberOfPtodays());
        Integer serviceId = doctorRequestDTO.getMedicalServiceId();

        MedicalService medicalService = medicalServiceRepository.findById(serviceId)
                .orElseThrow(() -> {
                    log.error("Eroare la actualizare: Serviciul medical cu ID-ul {} nu a fost găsit în sistem.", serviceId);
                    return new ResourceNotFoundException("Serviciul medical cu ID-ul " + serviceId + " nu a fost găsit.");
                });

        existingDoctor.setMedicalService(medicalService);
        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        log.info("Doctorul cu ID-ul {} a fost actualizat cu succes (Nume nou: {}).", id, updatedDoctor.getName());
        return updatedDoctor;
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        log.debug("Se inițiază procedura de ștergere pentru doctorul cu ID-ul: {}", id);
        Doctor doctor = getDoctorById(id);
        User user = doctor.getUser();

        if (user != null) {
            log.info("Se șterge utilizatorul asociat '{}' (ID: {}) pentru a elimina doctorul în cascadă.",
                    user.getUsername(), user.getId());
            userRepository.delete(user);
        } else {
            log.warn("Doctorul cu ID-ul {} nu are un cont de User asociat. Se șterge doar entitatea Doctor.", id);
            doctorRepository.delete(doctor);
        }
        log.info("Doctorul cu ID-ul {} a fost eliminat complet din sistem.", id);
    }

    public Optional<Doctor> getDoctorByMedicalService(Integer medicalServiceId) {
        log.debug("Căutare doctor asociat serviciului medical cu ID: {}", medicalServiceId);
        return doctorRepository.findByMedicalServiceId(medicalServiceId);
    }

    public Doctor getDoctorByUsername(String username) {
        log.debug("Căutare doctor după username-ul de autentificare: {}", username);
        return doctorRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Eroare interogare: Nu există niciun doctor asociat username-ului '{}'.", username);
                    return new ResourceNotFoundException("Doctorul cu username-ul " + username + " nu a fost găsit.");
                });
    }

    public Page<Doctor> getDoctorsPaged(Pageable pageable) {
        log.debug("Se solicită lista paginată de doctori folosind structura Pageable.");
        return doctorRepository.findAll(pageable);
    }
}