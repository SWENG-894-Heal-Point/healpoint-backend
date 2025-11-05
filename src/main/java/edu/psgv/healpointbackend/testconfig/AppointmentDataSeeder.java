package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.Appointment;
import edu.psgv.healpointbackend.model.AppointmentStatus;
import edu.psgv.healpointbackend.repository.AppointmentRepository;
import edu.psgv.healpointbackend.repository.DoctorRepository;
import edu.psgv.healpointbackend.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Data seeder for loading appointments into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute appointment loading logic
 * when the application starts in the "test" profile. It reads appointment data
 * from a JSON file and saves it to the database.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(45)
public class AppointmentDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    private final static int APPOINTMENT_DURATION_MINUTES = 30;

    /**
     * Constructs a new AppointmentDataSeeder with the specified ObjectMapper,
     * DoctorRepository, PatientRepository, and AppointmentRepository.
     *
     * @param objectMapper          the ObjectMapper for JSON processing
     * @param doctorRepository      the repository for Doctor entities
     * @param patientRepository     the repository for Patient entities
     * @param appointmentRepository the repository for Appointment entities
     */
    public AppointmentDataSeeder(ObjectMapper objectMapper, DoctorRepository doctorRepository,
                                 PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.objectMapper = objectMapper;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Runs the appointment data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during appointment loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load appointments from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Appointments.json").getInputStream());
        for (JsonNode node : root) {
            LocalTime startTime = LocalTime.parse(node.get("appointmentTime").asText());

            Appointment appointment = new Appointment(
                    doctorRepository.findById(node.get("doctorId").asInt()).orElseThrow(),
                    patientRepository.findById(node.get("patientId").asInt()).orElseThrow(),
                    LocalDate.parse(node.get("appointmentDate").asText()),
                    startTime,
                    startTime.plusMinutes(APPOINTMENT_DURATION_MINUTES),
                    node.get("reason").asText()
            );

            appointment.setStatus(node.get("status").asText());
            appointmentRepository.save(appointment);
        }

        // Log numbers of appointments by users
        LOGGER.info("*** Loaded Appointments Count By User ***");

        List<Appointment> appointments = appointmentRepository.findAll();
        List<Integer> allUserIDs = appointments.stream()
                .flatMap(a -> Stream.of(a.getDoctor().getId(), a.getPatient().getId()))
                .distinct().sorted().toList();

        for (Integer userId : allUserIDs) {
            long upcomingCount = appointments.stream()
                    .filter(a -> (a.getDoctor().getId().equals(userId) || a.getPatient().getId().equals(userId))
                            && a.getStatus().equals(AppointmentStatus.SCHEDULED)).count();

            long pastCount = appointments.stream()
                    .filter(a -> (a.getDoctor().getId().equals(userId) || a.getPatient().getId().equals(userId))
                            && !a.getStatus().equals(AppointmentStatus.SCHEDULED)).count();

            LOGGER.info("User ID {} has {} past appointments and {} upcoming appointments", userId, pastCount, upcomingCount);
        }
    }
}
