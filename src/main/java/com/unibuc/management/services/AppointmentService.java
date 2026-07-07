package com.unibuc.management.services;

import com.unibuc.management.dto.request.AppointmentRequestDTO;
import com.unibuc.management.domain.*;
import com.unibuc.management.dto.response.AppointmentResponseDTO;
import com.unibuc.management.dto.response.DoctorResponseDTO;
import com.unibuc.management.dto.response.PaymentResponseDTO;
import com.unibuc.management.mappers.AppointmentMapper;
import com.unibuc.management.repositories.*;
import com.unibuc.management.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.unibuc.management.exceptions.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PaidTimeOffRepository ptoRepository;
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final PaymentService paymentService;
    private final MedicalServiceService medicalServiceService;
    private final MedicalServiceRepository medicalServiceRepository;

    @Transactional
    public AppointmentResponseDTO createAppointment(Authentication authentication, AppointmentRequestDTO dto) {
        boolean isPatient = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        Patient patientToRegister;
        if (isPatient) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long patientIdFromSession = userDetails.getId();

            patientToRegister = patientRepository.findByUserId(patientIdFromSession)
                    .orElseThrow(() -> new EntityNotFoundException("Pacientul autentificat nu a fost găsit."));
        } else {
            if (dto.getPatientId() == null) {
                throw new InvalidActionException("ID-ul pacientului este obligatoriu când programarea este creată de personalul medical.");
            }
            patientToRegister = patientRepository.findById(dto.getPatientId())
                    .orElseThrow(() -> new EntityNotFoundException("Pacientul selectat nu există."));
        }
        log.debug("Se inițiază crearea unei programări din DTO pentru pacientul ID: {}", patientToRegister.getId());

        MedicalService medicalService = medicalServiceRepository.findById(dto.getMedicalServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Serviciul medical nu a fost găsit."));

        List<OffsetDateTime> availableSlots = getAvailableTimeSlots(dto.getMedicalServiceId(), dto.getDoctorId(), dto.getAppointmentFrom().toLocalDate().toString());
        boolean slotAvailable = availableSlots.stream().anyMatch(slot ->
                slot.truncatedTo(ChronoUnit.MINUTES).equals(dto.getAppointmentFrom().truncatedTo(ChronoUnit.MINUTES))
        );

        if (!slotAvailable) {
            log.error("Eroare creare programare: Slotul orar {} solicitat de pacientul ID {} nu este disponibil.",
                    dto.getAppointmentFrom(), patientToRegister.getId());
            throw new InvalidActionException("Slotul orar " + dto.getAppointmentFrom() + " nu este disponibil.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patientToRegister);
        appointment.setMedicalService(medicalService);
        appointment.setAppointmentFrom(dto.getAppointmentFrom());
        appointment.setStatus("Appointed");

        if (dto.getDoctorId() != null) {
            Doctor doctor = doctorRepository.findById((dto.getDoctorId()))
                    .orElseThrow(() -> new EntityNotFoundException("Medicul nu a fost găsit."));
            appointment.setDoctor(doctor);
        } else {
            appointment.setDoctor(null);
        }

        log.debug("Generare plată pentru programarea curentă...");
        Payment payment = paymentService.createPaymentForPatient(patientToRegister, medicalService, appointment);
        appointment.setPayment(payment);
        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Programarea cu ID-ul {} a fost salvată cu succes în sistem.", savedAppointment.getId());

        return AppointmentMapper.toDto(savedAppointment);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointmentsByPatientId(Integer patientId,
                                                        Pageable pageable,
                                                        Authentication authentication) {

        log.debug("Se solicită programările pacientului ID {}", patientId);

        String currentUsername = authentication.getName();

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"))) {

            Patient currentPatient = patientService.getPatientByUsername(currentUsername);

            if (!currentPatient.getId().equals(patientId)) {
                log.error("Pacientul '{}' a încercat să acceseze programările altui pacient.",
                        currentUsername);
                throw new UnauthorizedAccessException(
                        "Nu aveți dreptul să vizualizați aceste programări.");
            }
            return appointmentRepository.findByPatientId(patientId, pageable).map(AppointmentMapper::toDto);
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))) {

            Doctor currentDoctor = doctorService.getDoctorByUsername(currentUsername);

            Page<Appointment> appointments =
                    appointmentRepository.findByPatientId(patientId, pageable);

            List<Appointment> filtered = appointments.getContent().stream()
                    .filter(a -> a.getMedicalService()
                            .getMedicalServiceDoctors()
                            .stream()
                            .anyMatch(d -> d.getId().equals(currentDoctor.getId())))
                    .toList();

            return new PageImpl<>(
                    filtered.stream()
                            .map(AppointmentMapper::toDto)
                            .toList(),
                    pageable,
                    filtered.size()
            );
        }

        throw new UnauthorizedAccessException("Acces interzis.");
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAppointmentsByDoctorId(Integer doctorId,
                                                       Pageable pageable,
                                                       Authentication authentication) {

        log.debug("Se solicită programările medicului ID {}", doctorId);

        Doctor currentDoctor =
                doctorService.getDoctorByUsername(authentication.getName());

        if (!currentDoctor.getId().equals(doctorId)) {

            log.error("Doctorul '{}' a încercat să acceseze programările altui doctor.",
                    authentication.getName());

            throw new UnauthorizedAccessException(
                    "Nu aveți dreptul să vizualizați aceste programări.");
        }

        return appointmentRepository.findByDoctorId(doctorId, pageable).map(AppointmentMapper::toDto);
    }

    @Transactional
    public AppointmentResponseDTO updateAppointment(
            Integer appointmentId,
            OffsetDateTime appointmentFrom,
            Authentication authentication) {

        log.info("Se actualizează programarea ID {}", appointmentId);

        if (appointmentFrom.isBefore(OffsetDateTime.now())) {
            throw new InvalidActionException(
                    "Nu puteți muta o programare în trecut.");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Programarea cu ID-ul " + appointmentId + " nu a fost găsită."));

        validateAppointmentOwnership(appointment, authentication);

        MedicalService medicalService = appointment.getMedicalService();

        Integer doctorId =
                appointment.getDoctor() != null
                        ? appointment.getDoctor().getId()
                        : null;

        List<OffsetDateTime> availableSlots =
                getAvailableTimeSlots(
                        medicalService.getId(),
                        doctorId,
                        appointmentFrom.toLocalDate().toString());

        boolean slotAvailable =
                availableSlots.stream().anyMatch(slot ->
                        slot.truncatedTo(ChronoUnit.MINUTES)
                                .equals(appointmentFrom.truncatedTo(ChronoUnit.MINUTES)));

        if (!slotAvailable) {
            throw new InvalidActionException(
                    "Slotul selectat nu mai este disponibil.");
        }

        appointment.setAppointmentFrom(appointmentFrom);

        log.info("Se modifică/salvează direct entitatea Appointment ID: {}", appointment.getId());
        Appointment updatedAppointment =
                appointmentRepository.save(appointment);

        log.info("Programarea ID {} a fost modificată cu succes.", appointmentId);

        return AppointmentMapper.toDto(updatedAppointment);
    }

    @Transactional
    public void submitFeedback(Integer appointmentId, float rating, String username) {

        log.debug("Se procesează trimiterea de feedback pentru programarea ID: {} de către utilizatorul {}",
                appointmentId, username);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Feedback eșuat: Programarea cu ID-ul {} nu există.", appointmentId);
                    return new ResourceNotFoundException("Programarea nu a fost găsită.");
                });

        Patient currentPatient = patientRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    log.error("Pacientul asociat utilizatorului {} nu a fost găsit.", username);
                    return new ResourceNotFoundException("Pacientul nu a fost găsit.");
                });

        if (!appointment.getPatient().getId().equals(currentPatient.getId())) {
            log.error("Securitate: Pacientul ID {} a încercat să lase feedback pentru programarea ID {} care aparține pacientului ID {}",
                    currentPatient.getId(), appointmentId, appointment.getPatient().getId());

            throw new UnauthorizedAccessException(
                    "Nu puteți lăsa feedback pentru o programare care nu vă aparține.");
        }

        if (OffsetDateTime.now().isBefore(appointment.getAppointmentFrom().plusMinutes(30))) {
            log.error("Feedback prea devreme pentru programarea ID {}", appointmentId);

            throw new InvalidActionException(
                    "Feedback-ul poate fi trimis doar după 30 de minute de la începerea consultației.");
        }

        MedicalService service = appointment.getMedicalService();

        double newRating =
                (service.getRating() * service.getNrOfRatings() + rating)
                        / (service.getNrOfRatings() + 1);

        service.setRating(newRating);
        service.setNrOfRatings(service.getNrOfRatings() + 1);

        medicalServiceService.save(service);

        appointment.setStatus("Completed");
        appointmentRepository.save(appointment);

        log.info("Feedback salvat cu succes pentru programarea ID {}", appointmentId);
    }

    public List<OffsetDateTime> getAvailableTimeSlots(Integer medicalServiceId, Integer doctorId, String date) {
        MedicalService medicalService =
                medicalServiceService.getMedicalServiceById(medicalServiceId);
        log.debug("Calculare sloturi pentru serviciul ID: {}, Medic ID: {}, Data: {}", medicalService.getId(), doctorId, date);

        LocalDate selectedDate = LocalDate.parse(date);
        LocalDateTime startTime = LocalDateTime.of(selectedDate, LocalTime.of(medicalService.getStartHour(), 0));
        LocalDateTime endTime = LocalDateTime.of(selectedDate, LocalTime.of(medicalService.getEndHour(), 0));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        ZoneOffset zoneOffset = ZoneOffset.UTC;
        OffsetDateTime startOfDay = selectedDate.atStartOfDay().atOffset(zoneOffset);
        OffsetDateTime endOfDay = selectedDate.atTime(LocalTime.MAX).atOffset(zoneOffset);

        List<Appointment> existingAppointments = appointmentRepository.findByMedicalServiceAndDate(medicalService.getId(), startOfDay, endOfDay);

        List<PaidTimeOff> doctorPTOs = new ArrayList<>();
        if (doctorId != null) {
            doctorPTOs = ptoRepository.findActivePtoForDoctorInDay(doctorId, startOfDay, endOfDay);
            log.debug("S-au găsit {} perioade PTO active care acoperă data selectată pentru doctorul ID: {}", doctorPTOs.size(), doctorId);
        }

        List<OffsetDateTime> availableSlots = new ArrayList<>();

        while (startTime.isBefore(endTime)) {
            OffsetDateTime slot = startTime.atOffset(ZoneOffset.UTC);

            boolean isPastTime = slot.isBefore(now);

            boolean isBlockedByPTO = false;
            if (doctorId != null) {
                isBlockedByPTO = doctorPTOs.stream().anyMatch(pto ->
                        (slot.isEqual(pto.getPtoFrom()) || slot.isAfter(pto.getPtoFrom())) && slot.isBefore(pto.getPtoTo())
                );
            }

            boolean hasAppointment = false;
            if (doctorId != null) {
                hasAppointment = existingAppointments.stream().anyMatch(appt ->
                        appt.getDoctor() != null &&
                                appt.getDoctor().getId().equals(doctorId) &&
                                appt.getAppointmentFrom().equals(slot)
                );
            } else {
                hasAppointment = existingAppointments.stream().anyMatch(appt -> appt.getAppointmentFrom().equals(slot));
            }

            if (!isPastTime && !isBlockedByPTO && !hasAppointment) {
                availableSlots.add(slot);
            }

            startTime = startTime.plusMinutes(30);
        }

        log.debug("Total sloturi orare libere identificate: {}", availableSlots.size());
        return availableSlots;
    }

    public Optional<AppointmentResponseDTO> findById(Integer appointmentId) {
        log.debug("Căutare programare cu ID: {}", appointmentId);
        return appointmentRepository.findById(appointmentId).map(AppointmentMapper::toDto);
    }

    public Page<AppointmentResponseDTO> getAppointmentsByPatientIdPaged(Integer patientId, Pageable pageable) {
        log.debug("Preluare paginată a programărilor pentru pacientul ID: {}", patientId);
        return appointmentRepository.findByPatientId(patientId, pageable).map(AppointmentMapper::toDto);
    }

    public Page<AppointmentResponseDTO> getAppointmentsByDoctorIdPaged(Integer doctorId, Pageable pageable) {
        log.debug("Preluare paginată a programărilor pentru doctorul cu ID: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(AppointmentMapper::toDto);
    }

    @Transactional
    public void deleteAppointment(Integer appointmentId,
                                  Authentication authentication) {

        log.info("Se încearcă ștergerea programării ID {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Programarea cu ID-ul {} nu există.", appointmentId);
                    return new ResourceNotFoundException(
                            "Programarea cu ID-ul " + appointmentId + " nu a fost găsită.");
                });

        validateAppointmentOwnership(appointment, authentication);

        appointmentRepository.delete(appointment);

        log.info("Programarea cu ID-ul {} a fost ștearsă.", appointmentId);
    }

    private void validateAppointmentOwnership(
            Appointment appointment,
            Authentication authentication) {

        String username = authentication.getName();

        boolean isPatient = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT"));

        boolean isDoctor = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (isPatient) {

            Patient currentPatient =
                    patientRepository.findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Pacientul nu a fost găsit."));

            if (!currentPatient.getId().equals(appointment.getPatient().getId())) {
                throw new UnauthorizedAccessException(
                        "Nu puteți modifica programarea altui pacient.");
            }

            return;
        }

        if (isDoctor) {

            Doctor currentDoctor =
                    doctorRepository.findByUserUsername(username)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Doctorul nu a fost găsit."));

            boolean ownsAppointment =
                    appointment.getMedicalService()
                            .getMedicalServiceDoctors()
                            .stream()
                            .anyMatch(d -> d.getId().equals(currentDoctor.getId()));

            if (!ownsAppointment) {
                throw new UnauthorizedAccessException(
                        "Nu puteți modifica programarea altui doctor.");
            }
        }
    }
}