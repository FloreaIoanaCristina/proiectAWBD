package com.unibuc.management.services;

import com.unibuc.management.dto.validation.ServiceCoverageRequestDTO;
import com.unibuc.management.entities.InsuranceProvider;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.ServiceCoverage;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.InsuranceProviderRepository;
import com.unibuc.management.repositories.MedicalServiceRepository;
import com.unibuc.management.repositories.ServiceCoverageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class ServiceCoverageService {

    private final ServiceCoverageRepository serviceCoverageRepository;
    private final InsuranceProviderRepository insuranceProviderRepository;
    private final MedicalServiceRepository medicalServiceRepository;

    public ServiceCoverageService(ServiceCoverageRepository serviceCoverageRepository,
                                  InsuranceProviderRepository insuranceProviderRepository,
                                  MedicalServiceRepository medicalServiceRepository) {
        this.serviceCoverageRepository = serviceCoverageRepository;
        this.insuranceProviderRepository = insuranceProviderRepository;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    public List<ServiceCoverage> getAllCoverages() {
        log.debug("Se preia lista completă a acoperirilor de servicii (ServiceCoverages).");
        return serviceCoverageRepository.findAll();
    }

    public ServiceCoverage getCoverageById(Long id) {
        log.debug("Căutare acoperire de serviciu după ID: {}", id);
        return serviceCoverageRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Acoperirea de serviciu cu ID-ul {} nu a fost găsită.", id);
                return new ResourceNotFoundException("Acoperirea de serviciu cu ID-ul " + id + " nu a fost găsită.");
            });
    }

    @Transactional
    public ServiceCoverage save(ServiceCoverageRequestDTO dto) {
        log.info("Se salvează o nouă regulă de acoperire din DTO: Serviciu ID [{}], Asigurator ID [{}], Procent: {}%",
                dto.getMedicalServiceId(), dto.getInsuranceProviderId(), dto.getCoveragePercent());

        MedicalService medicalService = medicalServiceRepository.findById(dto.getMedicalServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviciul medical cu ID-ul " + dto.getMedicalServiceId() + " nu există."));

        InsuranceProvider insuranceProvider = insuranceProviderRepository.findById(dto.getInsuranceProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Asigurătorul cu ID-ul " + dto.getInsuranceProviderId() + " nu există."));

        ServiceCoverage coverage = new ServiceCoverage();
        coverage.setCoveragePercent(dto.getCoveragePercent());
        coverage.setMedicalService(medicalService);
        coverage.setInsuranceProvider(insuranceProvider);

        ServiceCoverage savedCoverage = serviceCoverageRepository.save(coverage);
        log.info("Acoperirea de serviciu a fost salvată cu succes (ID alocat: {})", savedCoverage.getId());
        return savedCoverage;
    }

    @Transactional
    public ServiceCoverage updateCoverage(Long id, ServiceCoverageRequestDTO dto) {
        log.debug("Se solicită actualizarea acoperirii de serviciu cu ID-ul: {}", id);

        ServiceCoverage existingCoverage = getCoverageById(id);

        MedicalService medicalService = medicalServiceRepository.findById(dto.getMedicalServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Serviciul medical cu ID-ul " + dto.getMedicalServiceId() + " nu există."));

        InsuranceProvider insuranceProvider = insuranceProviderRepository.findById(dto.getInsuranceProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Asigurătorul cu ID-ul " + dto.getInsuranceProviderId() + " nu există."));

        existingCoverage.setCoveragePercent(dto.getCoveragePercent());
        existingCoverage.setMedicalService(medicalService);
        existingCoverage.setInsuranceProvider(insuranceProvider);

        ServiceCoverage updatedCoverage = serviceCoverageRepository.save(existingCoverage);
        log.info("Acoperirea de serviciu cu ID-ul {} a fost actualizată cu succes la {}%.", id, updatedCoverage.getCoveragePercent());
        return updatedCoverage;
    }

    @Transactional
    public void deleteCoverage(Long id) {
        log.debug("Se inițiază ștergerea acoperirii de serviciu cu ID-ul: {}", id);
        ServiceCoverage existingCoverage = getCoverageById(id);

        serviceCoverageRepository.delete(existingCoverage);
        log.info("Acoperirea de serviciu cu ID-ul {} a fost ștearsă definitiv din sistem.", id);
    }
}