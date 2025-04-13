package tn.esprit.spring.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.entities.Color;
import tn.esprit.spring.entities.Piste;
import tn.esprit.spring.repositories.IPisteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for PisteServicesImpl
 *
 * Test Types:
 * 1. testRetrieveAllPistes: Verifies retrieval of all pistes
 *    - Type: Repository Interaction Test
 *    - Mocks repository findAll() method
 *    - Checks correct list size and repository method invocation
 *
 * 2. testAddPiste: Validates piste addition
 *    - Type: Repository Save Operation Test
 *    - Mocks repository save() method
 *    - Checks return object and repository interaction
 *
 * 3. testRetrievePiste: Tests retrieving a specific piste
 *    - Type: Repository Lookup Test
 *    - Mocks repository findById() method
 *    - Verifies correct piste retrieval
 *
 * 4. testRemovePiste: Checks piste removal
 *    - Type: Repository Delete Operation Test
 *    - Verifies deleteById() method is called
 *
 * 5. testRetrievePiste_NotFound: Handles non-existent piste scenario
 *    - Type: Edge Case Test
 *    - Checks behavior when piste is not found
 *
 * 6. testAddPiste_InvalidSlope: Validates input validation
 *    - Type: Input Validation Test
 *    - Ensures negative slope is rejected
 *
 * 7. testAddPiste_NullPiste: Checks null input handling
 *    - Type: Null Input Validation Test
 *    - Verifies null piste is not accepted
 */
@ExtendWith(MockitoExtension.class)
public class PisteServicesImplTest {

    @Mock
    private IPisteRepository pisteRepository;

    @InjectMocks
    private PisteServicesImpl pisteServices;

    private Piste piste;
    private List<Piste> pisteList;

    @BeforeEach
    public void init() {
        piste = new Piste();
        piste.setNumPiste(1L);
        piste.setNamePiste("Test Piste");
        piste.setColor(Color.RED);
        piste.setLength(1500);
        piste.setSlope(30);

        pisteList = new ArrayList<>();
        pisteList.add(piste);
    }

    @Test
    public void testRetrieveAllPistes() {
        when(pisteRepository.findAll()).thenReturn(pisteList);

        List<Piste> result = pisteServices.retrieveAllPistes();

        assertEquals(1, result.size());
        verify(pisteRepository, times(1)).findAll();
    }

    @Test
    public void testAddPiste() {
        when(pisteRepository.save(any(Piste.class))).thenReturn(piste);

        Piste result = pisteServices.addPiste(piste);

        assertNotNull(result);
        assertEquals("Test Piste", result.getNamePiste());
        verify(pisteRepository, times(1)).save(any(Piste.class));
    }

    @Test
    public void testRetrievePiste() {
        when(pisteRepository.findById(1L)).thenReturn(Optional.of(piste));

        Piste result = pisteServices.retrievePiste(1L);

        assertNotNull(result);
        assertEquals(1L, result.getNumPiste());
        verify(pisteRepository, times(1)).findById(1L);
    }

    @Test
    public void testRemovePiste() {
        doNothing().when(pisteRepository).deleteById(1L);

        pisteServices.removePiste(1L);

        verify(pisteRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testRetrievePiste_NotFound() {
        when(pisteRepository.findById(99L)).thenReturn(Optional.empty());
        Piste result = pisteServices.retrievePiste(99L);
        assertNull(result);
    }

    @Test
    public void testAddPiste_InvalidSlope() {
        Piste invalidPiste = new Piste();
        invalidPiste.setSlope(-5);

        assertThrows(IllegalArgumentException.class, () -> {
            pisteServices.addPiste(invalidPiste);
        });
    }

    @Test
    public void testAddPiste_NullPiste() {
        assertThrows(IllegalArgumentException.class, () -> {
            pisteServices.addPiste(null);
        });
    }
}