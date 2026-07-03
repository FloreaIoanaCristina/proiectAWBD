package com.unibuc.management.services;

import com.unibuc.management.dto.validation.AppointmentRequestDTO;
import com.unibuc.management.entities.*;
import com.unibuc.management.repositories.*;
import com.unibuc.management.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.unibuc.management.exceptions.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PaidTimeOffRepository ptoRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PaymentService paymentService;
    private final MedicalServiceService medicalServiceService;

    private final MedicalServiceRepository medicalServiceRepository;
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PaidTimeOffRepository ptoRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              PaymentService paymentService,
                              MedicalServiceService medicalServiceService,
                              MedicalServiceRepository medicalServiceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.ptoRepository = ptoRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.paymentService = paymentService;
        this.medicalServiceService = medicalServiceService;
        this.medicalServiceRepository = medicalServiceRepository;
    }

    @Transactional
    public Appointment createAppointment(Authentication authentication, AppointmentRequestDTO dto) {
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

        List<OffsetDateTime> availableSlots = getAvailableTimeSlots(medicalService, dto.getDoctorId(), dto.getAppointmentFrom().toLocalDate().toString());
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

        return savedAppointment;
    }

    @Transactional
    public void submitFeedback(Integer appointmentId, float rating, Integer currentPatientId) {
        log.debug("Se procesează trimiterea de feedback pentru programarea ID: {} de către pacientul ID: {}",
                appointmentId, currentPatientId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Feedback eșuat: Programarea cu ID-ul {} nu există.", appointmentId);
                    return new ResourceNotFoundException("Programarea nu a fost găsită.");
                });

        if (!appointment.getPatient().getId().equals(currentPatientId)) {
            log.error("Securitate: Pacientul ID {} a încercat ilegal să lase feedback pentru programarea ID {} a pacientului ID {}",
                    currentPatientId, appointmentId, appointment.getPatient().getId());
            throw new UnauthorizedAccessException("Nu puteți lăsa feedback pentru o programare care nu vă aparține.");
        }

        if (OffsetDateTime.now().isBefore(appointment.getAppointmentFrom().plusMinutes(30))) {
            log.error("Validare eșuată: Încercare timpurie de feedback la programarea ID {} înainte de expirarea celor 30 min.", appointmentId);
            throw new InvalidActionException("Feedback-ul poate fi trimis doar după 30 de minute de la începerea consultației.");
        }

        MedicalService service = appointment.getMedicalService();
        double newRating = (service.getRating() * service.getNrOfRatings() + rating) / (service.getNrOfRatings() + 1);
        service.setRating(newRating);
        service.setNrOfRatings(service.getNrOfRatings() + 1);
        medicalServiceService.save(service);

        log.debug("Rating-ul serviciului ID {} a fost actualizat la {}", service.getId(), newRating);

        appointment.setStatus("Completed");
        appointmentRepository.save(appointment);

        log.info("Feedback salvat cu succes (rating: {}) pentru programarea ID {}. Status completat.", rating, appointmentId);
    }
    public List<OffsetDateTime> getAvailableTimeSlots(MedicalService medicalService, Integer doctorId, String date) {
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

    public Optional<Appointment> findById(Integer appointmentId) {
        log.debug("Căutare programare cu ID: {}", appointmentId);
        return appointmentRepository.findById(appointmentId);
    }

    public Page<Appointment> getAppointmentsByPatientIdPaged(Integer patientId, Pageable pageable) {
        log.debug("Preluare paginată a programărilor pentru pacientul ID: {}", patientId);
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    public Page<Appointment> getAppointmentsByDoctorIdPaged(Integer doctorId, Pageable pageable) {
        log.debug("Preluare paginată a programărilor pentru doctorul cu ID: {}", doctorId);
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }

    public Appointment save(Appointment appointment) {
        log.info("Se modifică/salvează direct entitatea Appointment ID: {}", appointment.getId());
        return appointmentRepository.save(appointment);
    }

    public boolean deleteAppointment(Integer id) {
        if (appointmentRepository.existsById(id)) {
            appointmentRepository.deleteById(id);
            log.info("Programarea cu ID-ul {} a fost ștearsă din sistem.", id);
            return true;
        }
        log.error("Ștergere eșuată: Programarea cu ID-ul {} nu a putut fi găsită pentru eliminare.", id);
        return false;
    }
}