package com.unibuc.management.services;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.domain.Appointment;
import com.unibuc.management.domain.Doctor;
import com.unibuc.management.domain.PaidTimeOff;
import com.unibuc.management.dto.request.PtoRequestDTO;
import com.unibuc.management.dto.response.DoctorResponseDTO;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorScheduleService {

    private final AppointmentRepository appointmentRepository;
    private final PaidTimeOffRepository ptoRepository;
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;

    public List<ScheduleEntry> getDoctorScheduleForDay(Integer doctorId, String date) {
        log.debug("Se generează programul complet al doctorului ID: {} pentru data: {}", doctorId, date);

        Doctor doctor = doctorService.getDoctorById(doctorId);

        LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new InvalidActionException("Formatul datei este invalid. Folosiți YYYY-MM-DD.");
        }

        OffsetDateTime startOfDay = localDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = localDate.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndDate(
                        doctor.getId(),
                        startOfDay,
                        endOfDay
                );

        log.debug("S-au găsit {} programări active pentru doctorul ID: {}",
                appointments.size(), doctorId);

        List<PaidTimeOff> ptoEntries =
                ptoRepository.findActivePtoForDoctorInDay(
                        doctor.getId(),
                        startOfDay,
                        endOfDay
                );

        log.debug("S-au găsit {} intrări PTO pentru doctorul ID: {}",
                ptoEntries.size(), doctorId);

        List<ScheduleEntry> schedule = new ArrayList<>();

        schedule.addAll(
                appointments.stream()
                        .map(appt -> new ScheduleEntry(
                                "Appointment",
                                appt.getAppointmentFrom(),
                                appt.getAppointmentFrom().plusMinutes(30),
                                appt))
                        .collect(Collectors.toList())
        );

        schedule.addAll(
                ptoEntries.stream()
                        .map(pto -> new ScheduleEntry(
                                "Paid Time Off",
                                pto.getPtoFrom(),
                                pto.getPtoTo(),
                                pto))
                        .collect(Collectors.toList())
        );

        schedule.sort(Comparator.comparing(ScheduleEntry::getFrom));

        log.debug("Programul conține {} intrări.", schedule.size());

        return schedule;
    }

    public List<ScheduleEntry> getDoctorLeaves(Integer doctorId) {
        log.debug("Se preia istoricul complet de concedii (PTO) pentru doctorul ID: {}", doctorId);

        OffsetDateTime startRange = OffsetDateTime.now().minusYears(2);
        OffsetDateTime endRange = OffsetDateTime.now().plusYears(2);

        List<PaidTimeOff> ptoEntries = ptoRepository.findActivePtoForDoctorInDay(doctorId, startRange, endRange);

        return ptoEntries.stream()
                .map(pto -> new ScheduleEntry(
                        "Paid Time Off",
                        pto.getPtoFrom(),
                        pto.getPtoTo(),
                        pto
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void schedulePTO(PtoRequestDTO dto, String currentUsername) {

        log.debug("Solicitare concediu pentru doctorul ID {} de către utilizatorul {}.",
                dto.getDoctorId(), currentUsername);

        Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);

        if (!currentDoctor.getId().equals(dto.getDoctorId())) {
            log.error("Doctorul {} a încercat să creeze PTO pentru doctorul {}.",
                    currentDoctor.getId(), dto.getDoctorId());
            throw new InvalidActionException(
                    "Puteți programa concediu doar pentru propria persoană.");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidActionException(
                    "Data de sfârșit nu poate fi înainte de data de început.");
        }

        OffsetDateTime start = dto.getStartDate()
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime end = dto.getEndDate()
                .atTime(LocalTime.MAX)
                .atOffset(ZoneOffset.UTC);

        Doctor doctor = doctorService.getDoctorById(dto.getDoctorId());

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndDate(
                        dto.getDoctorId(), start, end);

        if (!appointments.isEmpty()) {
            log.error("Doctorul ID {} are {} programări în intervalul selectat.",
                    dto.getDoctorId(), appointments.size());

            throw new InvalidActionException(
                    "Doctorul are programări în intervalul selectat. PTO refuzat.");
        }

        long daysRequested =
                ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;

        if (doctor.getNumberOfPTOdays() < daysRequested) {
            throw new InvalidActionException(
                    "Doctorul nu mai are suficiente zile de concediu (Disponibil: "
                            + doctor.getNumberOfPTOdays() + ").");
        }

        PaidTimeOff pto = new PaidTimeOff();
        pto.setDoctor(doctor);
        pto.setPtoFrom(start);
        pto.setPtoTo(end);

        ptoRepository.save(pto);

        doctor.setNumberOfPTOdays(
                doctor.getNumberOfPTOdays() - (int) daysRequested);

        doctorRepository.save(doctor);

        log.info("Concediu creat pentru doctorul ID {}. PTO rămas: {} zile.",
                doctor.getId(), doctor.getNumberOfPTOdays());
    }

    @Transactional
    public void updatePTO(Integer ptoId,
                          PtoRequestDTO dto,
                          String currentUsername) {

        log.debug("Se încearcă modificarea concediului PTO {} de către utilizatorul {}.",
                ptoId, currentUsername);

        PaidTimeOff pto = ptoRepository.findById(ptoId)
                .orElseThrow(() -> {
                    log.error("Concediul cu ID {} nu există.", ptoId);
                    return new ResourceNotFoundException(
                            "Concediul cu ID-ul " + ptoId + " nu a fost găsit.");
                });

        Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);

        if (!currentDoctor.getId().equals(pto.getDoctor().getId())) {
            throw new InvalidActionException(
                    "Puteți modifica doar propriile concedii.");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidActionException(
                    "Data de sfârșit nu poate fi înainte de data de început.");
        }

        OffsetDateTime start = dto.getStartDate()
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);

        OffsetDateTime end = dto.getEndDate()
                .atTime(LocalTime.MAX)
                .atOffset(ZoneOffset.UTC);

        Doctor doctor = pto.getDoctor();

        List<Appointment> appointments =
                appointmentRepository.findByDoctorIdAndDate(
                        doctor.getId(), start, end);

        if (!appointments.isEmpty()) {
            log.error("Noul interval se suprapune cu {} programări.",
                    appointments.size());

            throw new InvalidActionException(
                    "Nu se poate modifica concediul. Doctorul are programări în noul interval selectat.");
        }

        long oldDays =
                ChronoUnit.DAYS.between(
                        pto.getPtoFrom().toLocalDate(),
                        pto.getPtoTo().toLocalDate()) + 1;

        long newDays =
                ChronoUnit.DAYS.between(
                        start.toLocalDate(),
                        end.toLocalDate()) + 1;

        long difference = newDays - oldDays;

        if (difference > 0 &&
                doctor.getNumberOfPTOdays() < difference) {

            throw new InvalidActionException(
                    "Doctorul nu are suficiente zile de concediu disponibile pentru această modificare.");
        }

        doctor.setNumberOfPTOdays(
                doctor.getNumberOfPTOdays() - (int) difference);

        doctorRepository.save(doctor);

        pto.setPtoFrom(start);
        pto.setPtoTo(end);

        ptoRepository.save(pto);

        log.info("Concediul {} a fost modificat cu succes.", ptoId);
    }

    @Transactional
    public void deletePTO(Integer ptoId, String currentUsername) {
        log.debug("Se solicită anularea/ștergerea concediului (PTO) cu ID-ul: {}", ptoId);

        Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);

        PaidTimeOff pto = ptoRepository.findById(ptoId)
                .orElseThrow(() -> {
                    log.error("Ștergere PTO eșuată: Concediul cu ID-ul {} nu a fost găsit.", ptoId);
                    return new ResourceNotFoundException(
                            "Concediul cu ID-ul " + ptoId + " nu a fost găsit.");
                });

        if (!pto.getDoctor().getId().equals(currentDoctor.getId())) {
            log.error("Doctorul ID {} a încercat să șteargă PTO-ul doctorului ID {}.",
                    currentDoctor.getId(), pto.getDoctor().getId());

            throw new InvalidActionException(
                    "Puteți șterge doar propriile perioade de concediu.");
        }

        if (OffsetDateTime.now().isAfter(pto.getPtoFrom())) {
            log.error("Ștergere PTO eșuată: Încercare de anulare a unui concediu din trecut sau deja început (ID PTO: {}, Start: {}).",
                    ptoId, pto.getPtoFrom());
            throw new InvalidActionException(
                    "Nu se pot anula sau șterge concedii din trecut sau care au început deja.");
        }

        Doctor doctor = pto.getDoctor();
        long daysToReturn = ChronoUnit.DAYS.between(
                pto.getPtoFrom().toLocalDate(),
                pto.getPtoTo().toLocalDate()) + 1;

        doctor.setNumberOfPTOdays(doctor.getNumberOfPTOdays() + (int) daysToReturn);
        doctorRepository.save(doctor);
        ptoRepository.delete(pto);

        log.info("Concediul ID {} a fost anulat cu succes. S-au returnat {} zile în contul doctorului ID {}. Total curent zile disponibile: {}",
                ptoId, daysToReturn, doctor.getId(), doctor.getNumberOfPTOdays());
    }
}
