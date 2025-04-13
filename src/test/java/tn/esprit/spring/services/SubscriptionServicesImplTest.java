package tn.esprit.spring.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.entities.TypeSubscription;
import tn.esprit.spring.repositories.ISkierRepository;
import tn.esprit.spring.repositories.ISubscriptionRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServicesImplTest {

    @Mock
    private ISubscriptionRepository subscriptionRepository;

    @Mock
    private ISkierRepository skierRepository;

    @InjectMocks
    private SubscriptionServicesImpl subscriptionServices;

    private Subscription annualSubscription;
    private Subscription semestrialSubscription;
    private Subscription monthlySubscription;
    private List<Subscription> subscriptionList;
    private Set<Subscription> subscriptionSet;
    private Skier skier;

    @BeforeEach
    void setUp() {
        // Initialize test data
        LocalDate today = LocalDate.now();

        annualSubscription = new Subscription();
        annualSubscription.setNumSub(1L);
        annualSubscription.setTypeSub(TypeSubscription.ANNUAL);
        annualSubscription.setStartDate(today);
        annualSubscription.setPrice(1000F);

        semestrialSubscription = new Subscription();
        semestrialSubscription.setNumSub(2L);
        semestrialSubscription.setTypeSub(TypeSubscription.SEMESTRIEL);
        semestrialSubscription.setStartDate(today);
        semestrialSubscription.setPrice(600F);

        monthlySubscription = new Subscription();
        monthlySubscription.setNumSub(3L);
        monthlySubscription.setTypeSub(TypeSubscription.MONTHLY);
        monthlySubscription.setStartDate(today);
        monthlySubscription.setPrice(100F);

        subscriptionList = new ArrayList<>();
        subscriptionList.add(annualSubscription);
        subscriptionList.add(semestrialSubscription);
        subscriptionList.add(monthlySubscription);

        subscriptionSet = new HashSet<>(subscriptionList);

        skier = new Skier();
        skier.setNumSkier(1L);
        skier.setFirstName("John");
        skier.setLastName("Doe");
        skier.setSubscription(annualSubscription);
    }

    @Test
    void testAddSubscription_Annual() {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate expectedEndDate = startDate.plusYears(1);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(annualSubscription);

        // When
        Subscription result = subscriptionServices.addSubscription(annualSubscription);

        // Then
        assertEquals(expectedEndDate, result.getEndDate());
        verify(subscriptionRepository, times(1)).save(annualSubscription);
    }

    @Test
    void testAddSubscription_Semestriel() {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate expectedEndDate = startDate.plusMonths(6);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(semestrialSubscription);

        // When
        Subscription result = subscriptionServices.addSubscription(semestrialSubscription);

        // Then
        assertEquals(expectedEndDate, result.getEndDate());
        verify(subscriptionRepository, times(1)).save(semestrialSubscription);
    }

    @Test
    void testAddSubscription_Monthly() {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate expectedEndDate = startDate.plusMonths(1);

        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(monthlySubscription);

        // When
        Subscription result = subscriptionServices.addSubscription(monthlySubscription);

        // Then
        assertEquals(expectedEndDate, result.getEndDate());
        verify(subscriptionRepository, times(1)).save(monthlySubscription);
    }

    @Test
    void testUpdateSubscription() {
        // Given
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(annualSubscription);

        // When
        Subscription result = subscriptionServices.updateSubscription(annualSubscription);

        // Then
        assertNotNull(result);
        assertEquals(annualSubscription, result);
        verify(subscriptionRepository, times(1)).save(annualSubscription);
    }

    @Test
    void testRetrieveSubscriptionById_Found() {
        // Given
        Long subscriptionId = 1L;
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(annualSubscription));

        // When
        Subscription result = subscriptionServices.retrieveSubscriptionById(subscriptionId);

        // Then
        assertNotNull(result);
        assertEquals(annualSubscription, result);
    }

    @Test
    void testRetrieveSubscriptionById_NotFound() {
        // Given
        Long subscriptionId = 999L;
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        // When
        Subscription result = subscriptionServices.retrieveSubscriptionById(subscriptionId);

        // Then
        assertNull(result);
    }

    @Test
    void testGetSubscriptionByType() {
        // Given
        when(subscriptionRepository.findByTypeSubOrderByStartDateAsc(TypeSubscription.ANNUAL)).thenReturn(new HashSet<>(List.of(annualSubscription)));

        // When
        Set<Subscription> result = subscriptionServices.getSubscriptionByType(TypeSubscription.ANNUAL);

        // Then
        assertEquals(1, result.size());
        assertEquals(annualSubscription, result.iterator().next());
    }

    @Test
    void testRetrieveSubscriptionsByDates() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now().plusDays(10);
        when(subscriptionRepository.getSubscriptionsByStartDateBetween(startDate, endDate)).thenReturn(subscriptionList);

        // When
        List<Subscription> result = subscriptionServices.retrieveSubscriptionsByDates(startDate, endDate);

        // Then
        assertEquals(3, result.size());
        assertEquals(subscriptionList, result);
    }

    @Test
    void testRetrieveSubscriptions() {
        // Given
        List<Subscription> orderedSubscriptions = List.of(annualSubscription);
        when(subscriptionRepository.findDistinctOrderByEndDateAsc()).thenReturn(orderedSubscriptions);
        when(skierRepository.findBySubscription(annualSubscription)).thenReturn(skier);

        // When
        List<Subscription> result = subscriptionServices.retrieveSubscriptions();

        // Then
        assertNull(result); // The method returns null
        verify(subscriptionRepository, times(1)).findDistinctOrderByEndDateAsc();
        verify(skierRepository, times(1)).findBySubscription(annualSubscription);
    }

    @Test
    void testShowMonthlyRecurringRevenue() {
        // Given
        float monthlyRevenue = 100F;
        float semestrialRevenue = 600F;
        float annualRevenue = 1200F;

        when(subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.MONTHLY)).thenReturn(monthlyRevenue);
        when(subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.SEMESTRIEL)).thenReturn(semestrialRevenue);
        when(subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.ANNUAL)).thenReturn(annualRevenue);

        // When
        subscriptionServices.showMonthlyRecurringRevenue();

        // Then
        float expectedRevenue = monthlyRevenue + (semestrialRevenue / 6) + (annualRevenue / 12);
        verify(subscriptionRepository, times(1)).recurringRevenueByTypeSubEquals(TypeSubscription.MONTHLY);
        verify(subscriptionRepository, times(1)).recurringRevenueByTypeSubEquals(TypeSubscription.SEMESTRIEL);
        verify(subscriptionRepository, times(1)).recurringRevenueByTypeSubEquals(TypeSubscription.ANNUAL);
    }
}