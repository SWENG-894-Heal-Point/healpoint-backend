package edu.psgv.healpointbackend.model;

/**
 * Defines user roles within the application.
 * <ul>
 *   <li>ADMIN - Administrator role with elevated privileges.</li>
 *   <li>DOCTOR - Doctor role for medical professionals.</li>
 *   <li>PATIENT - Patient role for users receiving care.</li>
 * </ul>
 */
public class Roles {
    public static final String ADMIN = "ADMIN";
    public static final String SUPPORT_STAFF = "SUPPORT_STAFF";
    public static final String DOCTOR = "DOCTOR";
    public static final String PATIENT = "PATIENT";
}
