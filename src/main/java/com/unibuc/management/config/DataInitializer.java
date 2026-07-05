package com.unibuc.management.config;

import com.unibuc.management.domain.MedicalService;
import com.unibuc.management.repositories.MedicalServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"!test", "!h2"})
public class DataInitializer implements CommandLineRunner {

    private final MedicalServiceRepository medicalServiceRepository;

    public DataInitializer(MedicalServiceRepository medicalServiceRepository) {
        this.medicalServiceRepository = medicalServiceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (medicalServiceRepository.count() == 0) {

            MedicalService cardiologie = new MedicalService();
            cardiologie.setName("Consult Cardiologie + EKG");
            cardiologie.setSpecialization("Cardiologie");
            cardiologie.setStartHour(8);
            cardiologie.setEndHour(16);
            cardiologie.setPrice(250.0);
            cardiologie.setRating(5.0);
            cardiologie.setNrOfRatings(1);

            MedicalService dermatologie = new MedicalService();
            dermatologie.setName("Consult Dermatologie Generală");
            dermatologie.setSpecialization("Dermatologie");
            dermatologie.setStartHour(9);
            dermatologie.setEndHour(17);
            dermatologie.setPrice(180.0);
            dermatologie.setRating(4.8);
            dermatologie.setNrOfRatings(5);

            MedicalService pediatrie = new MedicalService();
            pediatrie.setName("Consult Pediatrie");
            pediatrie.setSpecialization("Pediatrie");
            pediatrie.setStartHour(8);
            pediatrie.setEndHour(20);
            pediatrie.setPrice(200.0);
            pediatrie.setRating(4.9);
            pediatrie.setNrOfRatings(12);

            medicalServiceRepository.saveAll(List.of(cardiologie, dermatologie, pediatrie));
        }
    }
}