package com.unibuc.management.services;

import com.unibuc.management.dto.ScheduleEntry;
import com.unibuc.management.entities.Appointment;
import com.unibuc.management.entities.Doctor;
import com.unibuc.management.entities.PaidTimeOff;
import com.unibuc.management.exceptions.InvalidActionException;
import com.unibuc.management.exceptions.ResourceNotFoundException;
import com.unibuc.management.repositories.AppointmentRepository;
import com.unibuc.management.repositories.DoctorRepository;
import com.unibuc.management.repositories.PaidTimeOffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorScheduleService {

    private final AppointmentRepository appointmentRepository;
    private final PaidTimeOffRepository ptoRepository;
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;

    public DoctorScheduleService(AppointmentRepository appointmentRepository,
                                 PaidTimeOffRepository ptoRepository,
                                 DoctorService doctorService,
                                 DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.ptoRepository = ptoRepository;
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
    }

    public List<ScheduleEntry> getDoctorScheduleForDay(Integer doctorId, LocalDate date) {
        log.debug("Se generează programul complet al doctorului ID: {} pentru data: {}", doctorId, date);
        OffsetDateTime startOfDay = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = date.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, startOfDay, endOfDay);
        log.debug("S-au găsit {} programări active pentru doctorul ID: {}", appointments.size(), doctorId);

        List<PaidTimeOff> ptoEntries = ptoRepository.findByDoctorAndDate(doctorId, startOfDay, endOfDay);
        log.debug("S-au găsit {} intrări de concediu (PTO) active pentru doctorul ID: {}", ptoEntries.size(), doctorId);

        List<ScheduleEntry> schedule = new ArrayList<>();
        schedule.addAll(appointments.stream()
                .map(appt -> new ScheduleEntry("Appointment", appt.getAppointmentFrom(),
                        appt.getAppointmentFrom().plusMinutes(30), appt))
                .collect(Collectors.toList()));

        schedule.addAll(ptoEntries.stream()
                .map(pto -> new ScheduleEntry("Paid Time Off", pto.getPtoFrom(), pto.getPtoTo(), pto))
                .collect(Collectors.toList()));

        schedule.sort(Comparator.comparing(ScheduleEntry::getFrom));

        log.debug("Programul combinat și sortat conține un total de {} intrări cronologice.", schedule.size());
        return schedule;
    }

    @Transactional
    public void schedulePTO(Integer doctorId, OffsetDateTime start, OffsetDateTime end) {
        log.debug("Solicitare concediu (PTO) nou pentru doctorul ID: {} în intervalul: {} -> {}", doctorId, start, end);
        Doctor doctor = doctorService.getDoctorById(doctorId);

        if (start.isAfter(end)) {
            log.error("Creare PTO eșuată: Interval invalid. Data de început ({}) este după data de sfârșit ({}).", start, end);
            throw new InvalidActionException("Data de început trebuie să fie înainte de data de sfârșit.");
        }

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctorId, start, end);
        if (!appointments.isEmpty()) {
            log.error("Creare PTO eșuată: Suprapunere detectată. Doctorul ID {} are deja {} programări în intervalul solicitat.",
                    doctorId, appointments.size());
            throw new InvalidActionException("Doctorul are programări în intervalul selectat. PTO refuzat.");
        }

        long daysRequested = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (doctor.getNumberOfPtodays() < daysRequested) {
            log.error("Creare PTO eșuată: Zile insuficiente pentru doctorul ID {}. Solicitate: {} zile, Disponibile: {} zile.",
                    doctorId, daysRequested, doctor.getNumberOfPtodays());
            throw new InvalidActionException("Doctorul nu mai are suficiente zile de concediu (Disponibil: "
                    + doctor.getNumberOfPtodays() + ").");
        }

        PaidTimeOff pto = new PaidTimeOff();
        pto.setDoctor(doctor);
        pto.setPtoFrom(start);
        pto.setPtoTo(end);
        ptoRepository.save(pto);

        doctor.setNumberOfPtodays(doctor.getNumberOfPtodays() - (int) daysRequested);
        doctorRepository.save(doctor);

        log.info("Concediu aprobat cu succes: Doctorul ID {} a primit {} zile de PTO. Zile rămase: {}",
                doctorId, daysRequested, doctor.getNumberOfPtodays());
    }

    @Transactional
    public void updatePTO(Integer ptoId, OffsetDateTime start, OffsetDateTime end) {
        log.debug("Se încearcă modificarea concediului (PTO) cu ID-ul: {} la noul interval: {} -> {}", ptoId, start, end);

        PaidTimeOff pto = ptoRepository.findById(ptoId)
                .orElseThrow(() -> {
                    log.error("Modificare PTO eșuată: Concediul cu ID-ul {} nu există.", ptoId);
                    return new ResourceNotFoundException("Concediul cu ID-ul " + ptoId + " nu a fost găsit.");
                });

        Doctor doctor = pto.getDoctor();

        if (start.isAfter(end)) {
            log.error("Modificare PTO eșuată: Interval invalid pentru doctorul ID {}. Start: {}, End: {}", doctor.getId(), start, end);
            throw new InvalidActionException("Data de început trebuie să fie înainte de data de sfârșit.");
        }

        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), start, end);
        if (!appointments.isEmpty()) {
            log.error("Modificare PTO eșuată: Noul interval se suprapune cu {} programări existente ale doctorului ID {}.",
                    appointments.size(), doctor.getId());
            throw new InvalidActionException("Nu se poate modifica concediul. Doctorul are programări în noul interval selectat.");
        }

        long oldDays = ChronoUnit.DAYS.between(pto.getPtoFrom().toLocalDate(), pto.getPtoTo().toLocalDate()) + 1;
        long newDays = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate()) + 1;
        long difference = newDays - oldDays;

        if (difference > 0 && doctor.getNumberOfPtodays() < difference) {
            log.error("Modificare PTO eșuată: Zile suplimentare insuficiente pentru doctorul ID {}. Necesar adițional: {} zile, Disponibil: {} zile.",
                    doctor.getId(), difference, doctor.getNumberOfPtodays());
            throw new InvalidActionException("Doctorul nu are suficiente zile de concediu disponibile pentru această modificare. (Necesar suplimentar: "
                    + difference + ", Disponibil: " + doctor.getNumberOfPtodays() + ").");
        }

        doctor.setNumberOfPtodays(doctor.getNumberOfPtodays() - (int) difference);
        doctorRepository.save(doctor);

        pto.setPtoFrom(start);
        pto.setPtoTo(end);
        ptoRepository.save(pto);
        log.info("Concediul ID {} a fost modificat cu succes pentru doctorul ID {}. Diferență zile recalculată: {}. Total zile rămase: {}",
                ptoId, doctor.getId(), difference, doctor.getNumberOfPtodays());
    }

    @Transactional
    public void deletePTO(Integer ptoId) {
        log.debug("Se solicită anularea/ștergerea concediului (PTO) cu ID-ul: {}", ptoId);

        PaidTimeOff pto = ptoRepository.findById(ptoId)
                .orElseThrow(() -> {
                    log.error("Ștergere PTO eșuată: Concediul cu ID-ul {} nu a fost găsit.", ptoId);
                    return new ResourceNotFoundException("Concediul cu ID-ul " + ptoId + " nu a fost găsit.");
                });

        if (OffsetDateTime.now().isAfter(pto.getPtoFrom())) {
            log.error("Ștergere PTO eșuată: Încercare de anulare a unui concediu din trecut sau deja început (ID PTO: {}, Start: {}).",
                    ptoId, pto.getPtoFrom());
            throw new InvalidActionException("Nu se pot anula sau șterge concedii din trecut sau care au început deja.");
        }

        Doctor doctor = pto.getDoctor();
        long daysToReturn = ChronoUnit.DAYS.between(pto.getPtoFrom().toLocalDate(), pto.getPtoTo().toLocalDate()) + 1;

        doctor.setNumberOfPtodays(doctor.getNumberOfPtodays() + (int) daysToReturn);
        doctorRepository.save(doctor);
        ptoRepository.delete(pto);

        log.info("Concediul ID {} a fost anulat cu succes. S-au returnat {} zile în contul doctorului ID {}. Total curent zile disponibile: {}",
                ptoId, daysToReturn, doctor.getId(), doctor.getNumberOfPtodays());
    }
}
