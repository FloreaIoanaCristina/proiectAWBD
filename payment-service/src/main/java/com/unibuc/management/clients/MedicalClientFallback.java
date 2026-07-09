package com.unibuc.management.clients;

import com.unibuc.management.dto.internal.PricingInfoDTO;
import com.unibuc.management.dto.summary.AppointmentSummaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MedicalClientFallback implements MedicalClient {

    @Override
    public PricingInfoDTO getPricingInfo(Integer appointmentId) {
        log.warn("medical-service indisponibil. Nu se pot obține informațiile de preț pentru programarea {}.", appointmentId);
        return null;
    }

    @Override
    public AppointmentSummaryDTO getAppointmentSummary(Integer appointmentId) {
        log.warn("medical-service indisponibil. Nu se poate obține sumarul programării {}.", appointmentId);
        return null;
    }
}
