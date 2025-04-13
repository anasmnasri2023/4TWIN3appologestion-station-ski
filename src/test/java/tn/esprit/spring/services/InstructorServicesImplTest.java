package tn.esprit.spring.services;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.entities.Course;
import tn.esprit.spring.entities.Instructor;
import tn.esprit.spring.repositories.ICourseRepository;
import tn.esprit.spring.repositories.IInstructorRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InstructorServicesImplTest {

    @Mock
    private IInstructorRepository instructorRepository;

    @Mock
    private ICourseRepository courseRepository;

    @InjectMocks
    private InstructorServicesImpl instructorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddInstructor() {
        Instructor instructor = new Instructor(1L, "John", "Doe", LocalDate.now(), null);

        when(instructorRepository.save(instructor)).thenReturn(instructor);

        Instructor savedInstructor = instructorService.addInstructor(instructor);

        assertNotNull(savedInstructor);
        assertEquals("John", savedInstructor.getFirstName());
        verify(instructorRepository, times(1)).save(instructor);
    }

    @Test
    void testRetrieveAllInstructors() {
        Instructor instructor1 = new Instructor(1L, "John", "Doe", LocalDate.now(), null);
        Instructor instructor2 = new Instructor(2L, "Jane", "Smith", LocalDate.now(), null);

        when(instructorRepository.findAll()).thenReturn(Arrays.asList(instructor1, instructor2));

        List<Instructor> instructors = instructorService.retrieveAllInstructors();

        assertEquals(2, instructors.size());
        verify(instructorRepository, times(1)).findAll();
    }

    @Test
    void testUpdateInstructor() {
        Instructor instructor = new Instructor(1L, "John", "Doe", LocalDate.now(), null);

        when(instructorRepository.save(instructor)).thenReturn(instructor);

        Instructor updatedInstructor = instructorService.updateInstructor(instructor);

        assertNotNull(updatedInstructor);
        assertEquals("John", updatedInstructor.getFirstName());
        verify(instructorRepository, times(1)).save(instructor);
    }

    @Test
    void testRetrieveInstructorFound() {
        Instructor instructor = new Instructor(1L, "John", "Doe", LocalDate.now(), null);

        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));

        Instructor foundInstructor = instructorService.retrieveInstructor(1L);

        assertNotNull(foundInstructor);
        assertEquals(1L, foundInstructor.getNumInstructor());
        verify(instructorRepository, times(1)).findById(1L);
    }

    @Test
    void testRetrieveInstructorNotFound() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.empty());

        Instructor foundInstructor = instructorService.retrieveInstructor(1L);

        assertNull(foundInstructor);
        verify(instructorRepository, times(1)).findById(1L);
    }

    @Test
    void testAddInstructorAndAssignToCourse() {
        Course course = new Course(1L, 3, null, null, 120.0f, 5);
        Instructor instructor = new Instructor(null, "John", "Doe", LocalDate.now(), null);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(instructorRepository.save(any(Instructor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instructor savedInstructor = instructorService.addInstructorAndAssignToCourse(instructor, 1L);

        assertNotNull(savedInstructor);
        assertNotNull(savedInstructor.getCourses());
        assertEquals(1, savedInstructor.getCourses().size());
        verify(courseRepository, times(1)).findById(1L);
        verify(instructorRepository, times(1)).save(instructor);
    }
}
