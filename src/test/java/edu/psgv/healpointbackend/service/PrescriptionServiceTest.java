package edu.psgv.healpointbackend.service;

import edu.psgv.healpointbackend.dto.PrescriptionDto;
import edu.psgv.healpointbackend.model.Patient;
import edu.psgv.healpointbackend.model.Prescription;
import edu.psgv.healpointbackend.model.PrescriptionItem;
import edu.psgv.healpointbackend.repository.PatientRepository;
import edu.psgv.healpointbackend.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PrescriptionServiceTest {
    private PrescriptionRepository prescriptionRepository;
    private PatientRepository patientRepository;
    private PrescriptionService prescriptionService;

    @BeforeEach
    void setUp() {
        prescriptionRepository = mock(PrescriptionRepository.class);
        patientRepository = mock(PatientRepository.class);
        prescriptionService = new PrescriptionService(prescriptionRepository, patientRepository);
    }

    @Test
    void getPrescription_patientExists_returnsExistingPrescription() {
        Patient mockPatient = Patient.builder().id(1).build();

        Prescription existing = new Prescription();
        existing.setPatient(mockPatient);

        PrescriptionItem item = new PrescriptionItem();
        item.setMedication("TestMed");
        existing.getPrescriptionItems().add(item);

        when(patientRepository.findById(1)).thenReturn(Optional.of(mockPatient));
        when(prescriptionRepository.findByPatientId(1)).thenReturn(Optional.of(existing));

        Prescription result = prescriptionService.getPrescription(1);

        assertSame(existing, result);
        verify(patientRepository).findById(1);
        verify(prescriptionRepository).findByPatientId(1);

        assertEquals(1, result.getPrescriptionItems().size());
        assertEquals("TestMed", result.getPrescriptionItems().get(0).getMedication());
    }

    @Test
    void getPrescription_patientExists_noExistingPrescription_returnsNewPrescription() {
        Patient mockPatient = Patient.builder().id(2).build();

        when(patientRepository.findById(2)).thenReturn(Optional.of(mockPatient));
        when(prescriptionRepository.findByPatientId(2)).thenReturn(Optional.empty());

        Prescription result = prescriptionService.getPrescription(2);

        assertNotNull(result);
        assertEquals(mockPatient, result.getPatient());
        verify(prescriptionRepository).findByPatientId(2);
        assertTrue(result.getPrescriptionItems().isEmpty());
    }

    @Test
    void getPrescription_invalidPatient_throwsException() {
        when(patientRepository.findById(999)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.getPrescription(999));

        assertTrue(ex.getMessage().contains("Patient with ID 999 not found"));
        verify(prescriptionRepository, never()).findByPatientId(anyInt());
    }

    @Test
    void upsertPrescription_validInputNoExistingPrescription_createsNewPrescription() {
        Patient mockPatient = Patient.builder().id(1).build();
        mockPatient.setId(1);

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setMedication("Aspirin");

        PrescriptionDto dto = new PrescriptionDto();
        dto.setPatientId(1);
        dto.setInstruction("Take daily");
        dto.setPrescriptionItems(List.of(item1));

        when(patientRepository.findById(1)).thenReturn(Optional.of(mockPatient));
        when(prescriptionRepository.findByPatientId(1)).thenReturn(Optional.empty());

        prescriptionService.upsertPrescription(dto);

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionRepository).save(captor.capture());
        Prescription saved = captor.getValue();

        assertEquals("Take daily", saved.getInstruction());
        assertEquals(1, saved.getPrescriptionItems().size());
        assertEquals(mockPatient, saved.getPatient());
    }

    @Test
    void upsertPrescription_invalidPatient_throwsException() {
        PrescriptionDto dto = new PrescriptionDto();
        dto.setPatientId(99);
        dto.setPrescriptionItems(List.of());

        when(patientRepository.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.upsertPrescription(dto));

        assertTrue(ex.getMessage().contains("Patient with ID 99 not found"));
        verify(prescriptionRepository, never()).save(any());
    }
}