package edu.psgv.healpointbackend.model;

/**
 * Defines appointment statuses within the application.
 * <ul>
 *   <li>CANCELED - Appointment has been canceled.</li>
 *   <li>COMPLETED - Appointment has been completed.</li>
 *   <li>MISSED - Appointment was missed by the patient.</li>
 *   <li>SCHEDULED - Appointment is scheduled and upcoming.</li>
 * </ul>
 */
public class AppointmentStatus {
    public static final String CANCELED = "CANCELED";
    public static final String COMPLETED = "COMPLETED";
    public static final String MISSED = "MISSED";
    public static final String SCHEDULED = "SCHEDULED";

    /**
     * Private constructor to prevent instantiation.
     */
    private AppointmentStatus() {
    }
}
