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

import java.util.ArrayList;
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

    @Test // FR-12.5 UT-28
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

    @Test // FR-12.3 UT-19
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

    @Test // FR-12.6 UT-29
    void getPrescription_invalidPatient_throwsException() {
        when(patientRepository.findById(999)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.getPrescription(999));

        assertTrue(ex.getMessage().contains("Patient with ID 999 not found"));
        verify(prescriptionRepository, never()).findByPatientId(anyInt());
    }

    @Test // FR-9.3 UT-15
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

    @Test // FR-9.5 UT-24
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

    @Test // FR-10.4 UT-25
    void upsertPrescription_validInputExistingPrescription_updatePrescription() {
        Patient mockPatient = Patient.builder().id(1).build();
        mockPatient.setId(1);

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setMedication("TestMed");

        Prescription existing = new Prescription();
        existing.setPatient(mockPatient);
        existing.getPrescriptionItems().add(item1);

        PrescriptionItem item2 = new PrescriptionItem();
        item2.setMedication("NewTestMed");

        PrescriptionDto dto = new PrescriptionDto();
        dto.setPatientId(1);
        dto.setInstruction("Take daily");
        dto.setPrescriptionItems(List.of(item2));

        when(patientRepository.findById(1)).thenReturn(Optional.of(mockPatient));
        when(prescriptionRepository.findByPatientId(1)).thenReturn(Optional.of(existing));

        prescriptionService.upsertPrescription(dto);

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionRepository).save(captor.capture());
        Prescription saved = captor.getValue();

        assertEquals("Take daily", saved.getInstruction());
        assertEquals(1, saved.getPrescriptionItems().size());
        assertEquals("NewTestMed", saved.getPrescriptionItems().get(0).getMedication());
        assertEquals(mockPatient, saved.getPatient());
    }

    @Test // FR-10.3 UT-17
    void upsertPrescription_duplicateMedications_throwsException() {
        Patient mockPatient = Patient.builder().id(1).build();
        PrescriptionItem oldItem = new PrescriptionItem();
        oldItem.setMedication("OldTestMed");

        Prescription existing = new Prescription();
        existing.setPatient(mockPatient);
        existing.setInstruction("Old instruction");
        existing.getPrescriptionItems().add(oldItem);

        PrescriptionItem item1 = new PrescriptionItem();
        item1.setMedication("TestMed");
        PrescriptionItem item2 = new PrescriptionItem();
        item2.setMedication("Testmed ");

        PrescriptionDto dto = new PrescriptionDto();
        dto.setPatientId(1);
        dto.setPrescriptionItems(List.of(item1, item2));

        when(patientRepository.findById(1)).thenReturn(Optional.of(mockPatient));
        when(prescriptionRepository.findByPatientId(1)).thenReturn(Optional.of(existing));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> prescriptionService.upsertPrescription(dto));

        assertTrue(ex.getMessage().contains("Duplicate medications"));
        verify(prescriptionRepository, never()).save(any());

        assertEquals("Old instruction", existing.getInstruction());
        assertEquals(1, existing.getPrescriptionItems().size());
        assertEquals("OldTestMed", existing.getPrescriptionItems().get(0).getMedication());
    }

    @Test // FR-10.5 UT-26
    void upsertPrescription_nullItemsList_savesWithoutError() {
        Patient mockPatient = Patient.builder().id(1).build();
        Prescription existing = new Prescription();

        PrescriptionDto dto = new PrescriptionDto();
        dto.setPatientId(1);
        dto.setInstruction("No items");
        dto.setPrescriptionItems(new ArrayList<>());

        when(patientRepository.findById(1)).thenReturn(Optional.of(mockPatient));
        when(prescriptionRepository.findByPatientId(1)).thenReturn(Optional.of(existing));

        assertDoesNotThrow(() -> prescriptionService.upsertPrescription(dto));
        verify(prescriptionRepository).save(existing);
    }
}