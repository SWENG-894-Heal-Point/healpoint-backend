package edu.psgv.healpointbackend.testconfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.psgv.healpointbackend.model.WorkDay;
import edu.psgv.healpointbackend.repository.WorkDayRepository;
import edu.psgv.healpointbackend.service.ScheduleManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static edu.psgv.healpointbackend.HealpointBackendApplication.LOGGER;


/**
 * Data seeder for loading work days into the database during testing.
 * <p>
 * This class implements CommandLineRunner to execute work day loading logic
 * when the application starts in the "test" profile. It reads work day data
 * from a JSON file and saves it to the database using the ScheduleManager.
 * </p>
 *
 * @author Mahfuzur Rahman
 */
@Component
@Profile("test")
@Order(40)
public class WorkDayDataSeeder implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    private final ScheduleManager scheduleManager;
    private final WorkDayRepository workDayRepository;

    /**
     * Constructs a new WorkDayDataSeeder with the specified ObjectMapper,
     * ScheduleManager, and WorkDayRepository.
     *
     * @param objectMapper      the ObjectMapper for JSON processing
     * @param scheduleManager   the service for managing schedules
     * @param workDayRepository the repository for WorkDay entities
     */
    public WorkDayDataSeeder(ObjectMapper objectMapper, ScheduleManager scheduleManager, WorkDayRepository workDayRepository) {
        this.objectMapper = objectMapper;
        this.scheduleManager = scheduleManager;
        this.workDayRepository = workDayRepository;
    }

    /**
     * Runs the work day data seeding process.
     *
     * @param args command-line arguments (not used)
     * @throws Exception if an error occurs during work day loading
     */
    @Override
    public void run(String... args) throws Exception {
        // Load work days from JSON file and save to database
        JsonNode root = objectMapper.readTree(new ClassPathResource("test-data/Schedules.json").getInputStream());
        for (JsonNode node : root) {
            int doctorId = node.get("doctorId").asInt();
            JsonNode itemsNode = node.get("availability");
            List<WorkDay> workDayList = new ArrayList<>();

            for (JsonNode item : itemsNode) {
                WorkDay workDay = WorkDay.builder()
                        .dayName(item.get("dayName").asText())
                        .startTime(objectMapper.convertValue(item.get("startTime"), java.time.LocalTime.class))
                        .endTime(objectMapper.convertValue(item.get("endTime"), java.time.LocalTime.class))
                        .build();

                workDayList.add(workDay);
            }

            scheduleManager.upsertWorkDays(doctorId, workDayList);
        }

        // Log all loaded work days
        LOGGER.info("*** Loaded Work Days ***");
        workDayRepository.findAll().forEach(wd -> LOGGER.info("DoctorID: {}, {} {} to {}, SlotCount: {}",
                wd.getDoctorId(), wd.getDayName(), wd.getStartTime(), wd.getEndTime(), wd.getSlotCount()));
    }
}
