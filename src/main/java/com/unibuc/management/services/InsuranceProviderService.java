package com.unibuc.management.services;

import com.unibuc.management.dto.validation.InsuranceProviderRequestDTO;
import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class InsuranceProviderService {

    private final InsuranceProviderRepository insuranceProviderRepository;

    @Autowired
    public InsuranceProviderService(InsuranceProviderRepository insuranceProviderRepository) {
        this.insuranceProviderRepository = insuranceProviderRepository;
    }

    public Page<InsuranceProvider> getAllInsuranceProvidersPaged(Pageable pageable) {
        log.debug("Se preia lista paginată a asiguratorilor.");
        return insuranceProviderRepository.findAll(pageable);
    }
    public List<InsuranceProvider> getAllInsuranceProviders() {
        log.debug("Se preia lista completă a asiguratorilor.");
        return insuranceProviderRepository.findAll();
    }

    public InsuranceProvider getInsuranceProviderById(Integer id) {
        log.debug("Căutare asigurator după ID: {}", id);
        return insuranceProviderRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Asiguratorul cu ID-ul {} nu a fost găsit.", id);
                return new ResourceNotFoundException("Asiguratorul cu ID-ul " + id + " nu a fost găsit.");
            });
    }

    @Transactional
    public InsuranceProvider createInsuranceProvider(InsuranceProviderRequestDTO dto) {
        log.info("Se creează un asigurator nou cu numele: {}", dto.getName());

        InsuranceProvider provider = new InsuranceProvider();
        provider.setName(dto.getName());
        provider.setContactNumber(dto.getContactNumber());

        InsuranceProvider savedProvider = insuranceProviderRepository.save(provider);
        log.info("Asiguratorul '{}' a fost salvat cu succes (ID alocat: {}).", savedProvider.getName(), savedProvider.getId());
        return savedProvider;
    }

    @Transactional
    public InsuranceProvider updateInsuranceProvider(Integer id, InsuranceProviderRequestDTO dto) {
        log.debug("Se solicită actualizarea asiguratorului cu ID-ul: {}", id);

        InsuranceProvider existingProvider = getInsuranceProviderById(id);

        log.debug("Asiguratorul cu ID-ul {} a fost găsit. Se aplică modificările din DTO (Nume vechi: '{}' -> Nume nou: '{}').",
                id, existingProvider.getName(), dto.getName());

        existingProvider.setName(dto.getName());
        existingProvider.setContactNumber(dto.getContactNumber());

        InsuranceProvider updatedProvider = insuranceProviderRepository.save(existingProvider);

        log.info("Asiguratorul cu ID-ul {} a fost actualizat cu succes.", id);
        return updatedProvider;
    }

    public void deleteInsuranceProvider(Integer id) {
        log.debug("Se inițiază ștergerea asiguratorului cu ID-ul: {}", id);
        InsuranceProvider provider = getInsuranceProviderById(id);
        insuranceProviderRepository.delete(provider);
        log.info("Asiguratorul '{}' (ID: {}) a fost șters definitiv din sistem.", provider.getName(), id);
    }
}

