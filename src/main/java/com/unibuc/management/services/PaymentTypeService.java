package com.unibuc.management.services;

import com.unibuc.management.dto.validation.PaymentTypeRequestDTO;
import com.unibuc.management.entities.MedicalService;
import com.unibuc.management.entities.Patient;
import com.unibuc.management.entities.PaymentType;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.PaymentTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class PaymentTypeService {

    private final PaymentTypeRepository paymentTypeRepository;
    private final MedicalServiceService medicalServiceService;
    private final PatientService patientService;

    @Autowired
    public PaymentTypeService(PaymentTypeRepository paymentTypeRepository,
                              MedicalServiceService medicalServiceService,
                              PatientService patientService) {
        this.paymentTypeRepository = paymentTypeRepository;
        this.medicalServiceService = medicalServiceService;
        this.patientService = patientService;
    }


    public PaymentType getPaymentTypeById(Integer id) {
        log.debug("Căutare tip de plată după ID: {}", id);
        return paymentTypeRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Eroare interogare: Tipul de plată cu ID-ul {} nu există în sistem.", id);
                return new ResourceNotFoundException("Tipul de plată cu ID-ul " + id + " nu există.");
            });
    }

    public PaymentType getPriceForPatient(Integer medicalServiceId, Integer patientId) {
        log.debug("Se evaluează tipul de preț configurat pentru serviciul medical ID: {} și pacientul ID: {}",
            medicalServiceId, patientId);
        MedicalService medicalService = medicalServiceService.getMedicalServiceById(medicalServiceId);
        Patient patient = patientService.getPatientById(patientId);

        if (patient.getInsuranceProvider() != null) {
            log.debug("Pacientul ID {} are asigurare activă ({}). Se caută tariful redus prin asigurare.",
                patientId, patient.getInsuranceProvider().getName());
            Optional<PaymentType> withInsurance = paymentTypeRepository
                .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(medicalService.getId(), true, false);
            if (withInsurance.isPresent()) {
                log.info("S-a selectat tariful cu Asigurare pentru serviciul '{}' (Pacient ID: {})",
                    medicalService.getName(), patientId);
                return withInsurance.get();
            }
            log.warn("Nu s-a găsit o configurație specifică pentru asigurare.");
        }

        if (patient.getSubscription()) {
            log.debug("Pacientul ID {} are abonament activ. Se caută tariful dedicat abonaților.", patientId);
            Optional<PaymentType> withSubscription = paymentTypeRepository
                .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(medicalService.getId(), false, true);
            if (withSubscription.isPresent()) {
                log.info("S-a selectat tariful cu Abonament pentru serviciul '{}' (Pacient ID: {})",
                    medicalService.getName(), patientId);
                return withSubscription.get();
            }
            log.warn("Nu s-a găsit o configurație specifică pentru abonament. Se va trece la tariful standard.");
        }
        log.debug("Se aplică tariful standard implicit (fără asigurare, fără abonament) pentru serviciul ID {}",
            medicalService.getId());

        return paymentTypeRepository
            .findByMedicalServiceIdAndWithInsuranceAndWithSubscription(medicalService.getId(), false, false)
            .orElseThrow(() -> {
                log.error("Eroare critică: Nu a fost definit niciun preț/tarif standard pentru serviciul medical ID: {}",
                        medicalService.getId());
                return new ResourceNotFoundException("Nu a fost definit un preț pentru acest serviciu medical.");
            });
    }

    public List<PaymentType> getAllPaymentTypes() {
        log.debug("Se preia lista tuturor tipurilor de plată configurate.");
        return paymentTypeRepository.findAll();
    }

    @Transactional
    public PaymentType save(PaymentTypeRequestDTO dto) {
        log.info("Se salvează o nouă configurație de plată din DTO pentru serviciul medical ID: {}", dto.getMedicalServiceId());

        MedicalService medicalService = medicalServiceService.getMedicalServiceById(dto.getMedicalServiceId());

        PaymentType paymentType = new PaymentType();
        paymentType.setPrice(dto.getPrice());
        paymentType.setWithInsurance(dto.getWithInsurance());
        paymentType.setWithSubscription(dto.getWithSubscription());
        paymentType.setMedicalService(medicalService);

        PaymentType savedType = paymentTypeRepository.save(paymentType);
        log.info("Configurația de plată a fost salvată cu succes (ID alocat: {})", savedType.getId());
        return savedType;
    }

    @Transactional
    public PaymentType updatePaymentType(Integer id, PaymentTypeRequestDTO dto) {
        log.debug("Se solicită actualizarea configurației de plată cu ID-ul: {}", id);

        PaymentType existingType = getPaymentTypeById(id);

        MedicalService medicalService = medicalServiceService.getMedicalServiceById(dto.getMedicalServiceId());

        log.debug("Configurația existentă găsită. Schimbare preț și flag-uri.");

        existingType.setPrice(dto.getPrice());
        existingType.setWithInsurance(dto.getWithInsurance());
        existingType.setWithSubscription(dto.getWithSubscription());
        existingType.setMedicalService(medicalService);

        PaymentType updatedType = paymentTypeRepository.save(existingType);
        log.info("Tipul de plată cu ID-ul {} a fost actualizat cu succes.", id);
        return updatedType;
    }

    @Transactional
    public void deletePaymentType(Integer id) {
        log.debug("Se inițiază ștergerea tipului de plată cu ID-ul: {}", id);
        PaymentType existingType = getPaymentTypeById(id);

        paymentTypeRepository.delete(existingType);
        log.info("Tipul de plată cu ID-ul {} a fost șters definitiv din sistem.", id);
    }
}