package com.unibuc.management.clients;

import com.unibuc.management.dto.internal.PricingInfoDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medical-service", path = "/api/internal", fallback = MedicalClientFallback.class)
public interface MedicalClient {

    @GetMapping("/appointments/{id}/pricing-info")
    PricingInfoDTO getPricingInfo(@PathVariable("id") Integer appointmentId);

    @GetMapping("/appointments/{id}/summary")
    AppointmentSummaryDTO getAppointmentSummary(@PathVariable("id") Integer appointmentId);
}
