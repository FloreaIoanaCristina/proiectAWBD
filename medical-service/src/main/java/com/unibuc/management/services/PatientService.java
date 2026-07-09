package com.unibuc.management.services;

import com.unibuc.management.clients.UserClient;
import com.unibuc.management.dto.request.PatientRequestDTO;
import com.unibuc.management.domain.InsuranceProvider;
import com.unibuc.management.domain.Patient;
import com.unibuc.management.dto.response.PatientResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.mappers.PatientMapper;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import com.unibuc.management.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final InsuranceProviderRepository insuranceProviderRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserClient userClient;

    public Page<PatientResponseDTO> getAllPatientsPaged(Pageable pageable) {
        log.debug("Se preia lista paginată a pacienților.");
        return patientRepository.findAll(pageable).map(PatientMapper::toResponseDTO);
    }
    public List<PatientResponseDTO> getAllPatients() {
        log.debug("Se preia lista completă a pacienților.");
        return patientRepository.findAll()
                .stream().map(PatientMapper::toResponseDTO)
                .toList();
    }

    public Patient getPatientById(Integer id) {
        log.debug("Căutare pacient după ID: {}", id);
        return patientRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Pacientul cu ID-ul {} nu a fost găsit în sistem.", id);
                return new ResourceNotFoundException("Pacientul cu ID-ul " + id + " nu a fost găsit.");
            });
    }

    public Patient getPatientByUsername(String username) {
        log.debug("Căutare pacient după username-ul de autentificare: {}", username);
        return patientRepository.findByUsername(username)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Nu există niciun pacient asociat username-ului '{}'.", username);
                return new ResourceNotFoundException("Pacientul cu username-ul " + username + " nu a fost găsit.");
            });
    }

    @Transactional
    public PatientResponseDTO createPatient(PatientRequestDTO patientDto) {
        log.info("Se înregistrează un pacient nou cu numele: {}", patientDto.getName());
        validateAdultAge(patientDto.getBirthDate());

        Patient patient = new Patient();
        patient.setName(patientDto.getName());
        patient.setAge(patientDto.getBirthDate());
        patient.setSex(patientDto.getSex());
        patient.setSubscription(patientDto.getSubscription());
        patient.setMedicalRecord(patientDto.getMedicalRecord());

        if (patientDto.getInsuranceProviderId() != null) {
            InsuranceProvider provider = insuranceProviderRepository.findById(patientDto.getInsuranceProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asigurătorul cu ID-ul " + patientDto.getInsuranceProviderId() + " nu există."));
            patient.setInsuranceProvider(provider);
        }

        if (patientDto.getUserId() != null) {
            patient.setUserId(patientDto.getUserId());
            patient.setUsername(patientDto.getUsername());
            log.debug("Profilul de pacient a fost legat structural de User ID: {}", patientDto.getUserId());
        }

        Patient savedPatient = patientRepository.save(patient);
        log.info("Pacientul a fost creat cu succes cu ID-ul: {}", savedPatient.getId());
        return PatientMapper.toResponseDTO(savedPatient);
    }

    @Transactional
    public PatientResponseDTO updatePatient(Integer id, PatientRequestDTO patientDto) {
        log.debug("Se solicită actualizarea datelor pentru pacientul cu ID-ul: {}", id);

        Patient existingPatient = getPatientById(id);

        validateAdultAge(patientDto.getBirthDate());

        existingPatient.setName(patientDto.getName());
        existingPatient.setAge(patientDto.getBirthDate());
        existingPatient.setSex(patientDto.getSex());
        existingPatient.setSubscription(patientDto.getSubscription());
        existingPatient.setMedicalRecord(patientDto.getMedicalRecord());

        if (patientDto.getInsuranceProviderId() != null) {
            InsuranceProvider provider = insuranceProviderRepository.findById(patientDto.getInsuranceProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Asigurătorul cu ID-ul " + patientDto.getInsuranceProviderId() + " nu există."));
            existingPatient.setInsuranceProvider(provider);
        } else {
            existingPatient.setInsuranceProvider(null);
        }

        Patient updatedPatient = patientRepository.save(existingPatient);
        log.info("Datele pacientului cu ID-ul {} au fost actualizate cu succes.", id);
        return PatientMapper.toResponseDTO(updatedPatient);
    }

    @Transactional
    public void deletePatient(Integer id) {
        log.debug("Se inițiază procedura de eliminare pentru pacientul cu ID-ul: {}", id);
        Patient patient = getPatientById(id);
        Long userId = patient.getUserId();

        long appointmentCount = appointmentRepository.countByPatientId(id);
        if (appointmentCount > 0) {
            throw new InvalidActionException("Nu se poate șterge pacientul deoarece are " + appointmentCount + " programări");
        }

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        boolean isDoctor = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));
        boolean isPatient = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        if (isPatient) {
            if (userId == null || !currentUsername.equals(patient.getUsername())) {
                throw new InvalidActionException("Nu aveți permisiunea să ștergeți profilul altui pacient.");
            }
        } else if (isDoctor) {
            if (userId != null) {
                throw new InvalidActionException("Doctorii pot șterge doar pacienții fără cont de utilizator. Pacienții cu cont activ trebuie să își șteargă singuri contul.");
            }
        }

        patientRepository.delete(patient);
        log.info("Entitatea Patient cu ID-ul {} a fost ștearsă.", id);

        if (userId != null) {
            log.info("Se elimină și contul de User asociat (ID: {}) via user-service pentru a evita înregistrările orfane.", userId);
            userClient.deleteUser(userId);
        } else {
            log.warn("Atenție: Pacientul cu ID-ul {} nu avea un cont de User asociat.", id);
        }

        log.info("Procedura de ștergere completă s-a finalizat cu succes pentru pacientul ID {}.", id);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        patientRepository.findByUserId(userId).ifPresent(patient -> {
            long appointmentCount = appointmentRepository.countByPatientId(patient.getId());
            if (appointmentCount > 0) {
                throw new InvalidActionException("Nu se poate șterge pacientul deoarece are " + appointmentCount + " programări");
            }
            patientRepository.delete(patient);
            log.info("Profilul de pacient asociat User ID {} a fost șters.", userId);
        });
    }

    private void validateAdultAge(LocalDate birthDate) {
        if (birthDate == null) return;

        int calculatedAge = Period.between(birthDate, LocalDate.now()).getYears();
        if (calculatedAge < 18) {
            log.error("Validare eșuată: Pacientul are doar {} ani. Serviciul acceptă doar pacienți majori.", calculatedAge);
            throw new InvalidActionException("Înregistrarea a eșuat. Pacientul trebuie să aibă vârsta minimă de 18 ani.");
        }
    }
}