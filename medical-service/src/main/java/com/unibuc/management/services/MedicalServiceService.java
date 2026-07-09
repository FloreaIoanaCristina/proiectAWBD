package com.unibuc.management.services;

import com.unibuc.management.dto.request.MedicalServiceRequestDTO;
import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.dto.response.MedicalServiceResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.mappers.MedicalServiceMapper;
import com.unibuc.management.repositories.MedicalServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalServiceService {

    private final MedicalServiceRepository medicalServiceRepository;

    public List<MedicalServiceResponseDTO> getAllServices() {
        log.debug("Se preia lista completă a serviciilor medicale.");
        return medicalServiceRepository.findAll()
                .stream().map(MedicalServiceMapper::toResponseDTO)
                .toList();
    }

    public List<MedicalServiceResponseDTO> getServicesBySpecialization(String specialization) {
        log.debug("Se filtrează serviciile medicale pentru specializarea: '{}'", specialization);
        return medicalServiceRepository.findBySpecialization(specialization)
                .stream().map(MedicalServiceMapper::toResponseDTO)
                .toList();
    }

    public MedicalService getMedicalServiceById(Integer id) {
        log.debug("Căutare serviciu medical după ID: {}", id);
        return medicalServiceRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Serviciul medical cu ID-ul {} nu există.", id);
                return new ResourceNotFoundException("Serviciul medical cu ID-ul " + id + " nu există.");
            });
    }

    @Transactional
    public MedicalServiceResponseDTO save(MedicalService service) {
        log.info("Se salvează/actualizează serviciul medical: '{}' (Preț: {}))",
                service.getName(), service.getPrice());
        MedicalService savedService = medicalServiceRepository.save(service);
        log.info("Serviciul medical a fost salvat cu succes cu ID-ul: {}", savedService.getId());
        return MedicalServiceMapper.toResponseDTO(savedService);
    }

    @Transactional
    public void delete(Integer id) {
        log.debug("Se inițiază ștergerea serviciului medical cu ID-ul: {}", id);
        MedicalService service = getMedicalServiceById(id);
        medicalServiceRepository.delete(service);
        log.info("Serviciul medical '{}' (ID: {}) a fost șters definitiv din sistem.", service.getName(), id);
    }

    @Transactional
    public MedicalServiceResponseDTO saveFromDto(MedicalServiceRequestDTO dto) {
        log.info("Se inițiază crearea unui serviciu medical nou din DTO: '{}' (Preț: {})",
                dto.getName(), dto.getPrice());

        if (dto.getStartHour() >= dto.getEndHour()) {
            throw new InvalidActionException(
                    "Ora de început trebuie să fie strict înaintea orei de sfârșit."
            );
        }

        MedicalService service = new MedicalService();

        service.setName(dto.getName());
        service.setSpecialization(dto.getSpecialization());
        service.setStartHour(dto.getStartHour());
        service.setEndHour(dto.getEndHour());
        service.setPrice(dto.getPrice());

        service.setRating(0.0);
        service.setNrOfRatings(0);

        MedicalServiceResponseDTO savedService = save(service);

        log.info("Serviciul medical a fost salvat cu succes în baza de date cu ID-ul: {}", savedService.getId());
        return savedService;
    }

    @Transactional
    public MedicalServiceResponseDTO update(Integer id, MedicalServiceRequestDTO dto) {
        log.info("Se solicită actualizarea serviciului medical cu ID-ul: {}", id);

        if (dto.getStartHour() >= dto.getEndHour()) {
            throw new InvalidActionException(
                    "Ora de început trebuie să fie strict înaintea orei de sfârșit."
            );
        }

        MedicalService existingService = medicalServiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Serviciul medical cu ID-ul " + id + " nu a fost găsit."
                ));

        existingService.setName(dto.getName());
        existingService.setSpecialization(dto.getSpecialization());
        existingService.setStartHour(dto.getStartHour());
        existingService.setEndHour(dto.getEndHour());
        existingService.setPrice(dto.getPrice());

        MedicalServiceResponseDTO updatedService = save(existingService);
        log.info("Serviciul medical cu ID-ul {} a fost actualizat cu succes.", id);

        return updatedService;
    }
}
