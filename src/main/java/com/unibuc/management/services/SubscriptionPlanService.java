package com.unibuc.management.services;

import com.unibuc.management.dto.validation.SubscriptionPlanRequestDTO;
import com.unibuc.management.entities.SubscriptionPlan;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.SubscriptionPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    public SubscriptionPlanService(SubscriptionPlanRepository subscriptionPlanRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public List<SubscriptionPlan> getAllSubscriptionPlans() {
        log.debug("Se preia lista completă a planurilor de abonament.");
        return subscriptionPlanRepository.findAll();
    }

    public SubscriptionPlan getSubscriptionPlanById(Integer id) {
        log.debug("Căutare plan de abonament după ID: {}", id);
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Eroare interogare: Planul de abonament cu ID-ul {} nu a fost găsit.", id);
                    return new ResourceNotFoundException("Planul de abonament cu ID-ul " + id + " nu a fost găsit.");
                });
    }

    @Transactional
    public SubscriptionPlan save(SubscriptionPlanRequestDTO dto) {
        log.info("Se creează un plan de abonament nou din DTO: '{}' (Preț lunar: {} RON)",
                dto.getName(), dto.getMonthlyFee());

        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setName(dto.getName());
        plan.setMonthlyFee(dto.getMonthlyFee());
        plan.setDescription(dto.getDescription());

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);
        log.info("Planul de abonament a fost salvat cu succes (ID alocat: {})", savedPlan.getId());
        return savedPlan;
    }

    @Transactional
    public SubscriptionPlan update(Integer id, SubscriptionPlanRequestDTO dto) {
        log.debug("Se solicită actualizarea planului de abonament cu ID-ul: {}", id);

        SubscriptionPlan existingPlan = getSubscriptionPlanById(id);

        log.debug("Planul existent a fost găsit. Se aplică modificările din DTO (Nume vechi: '{}' -> Nume nou: '{}').",
                existingPlan.getName(), dto.getName());

        existingPlan.setName(dto.getName());
        existingPlan.setMonthlyFee(dto.getMonthlyFee());
        existingPlan.setDescription(dto.getDescription());

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(existingPlan);
        log.info("Planul de abonament cu ID-ul {} ('{}') a fost actualizat cu succes.", id, updatedPlan.getName());
        return updatedPlan;
    }

    @Transactional
    public void delete(Integer id) {
        log.debug("Se inițiază ștergerea planului de abonament cu ID-ul: {}", id);
        SubscriptionPlan existingPlan = getSubscriptionPlanById(id);

        subscriptionPlanRepository.delete(existingPlan);
        log.info("Planul de abonament '{}' (ID: {}) a fost șters definitiv din sistem.", existingPlan.getName(), id);
    }
}
