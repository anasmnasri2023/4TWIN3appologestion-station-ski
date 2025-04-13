package tn.esprit.spring.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.spring.entities.Skier;
import tn.esprit.spring.entities.Subscription;
import tn.esprit.spring.entities.TypeSubscription;
import tn.esprit.spring.repositories.ISkierRepository;
import tn.esprit.spring.repositories.ISubscriptionRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class SubscriptionServicesImpl implements ISubscriptionServices {

    private final ISubscriptionRepository subscriptionRepository;
    private final ISkierRepository skierRepository;

    @Override
    public Subscription addSubscription(Subscription subscription) {
        switch (subscription.getTypeSub()) {
            case ANNUAL:
                subscription.setEndDate(subscription.getStartDate().plusYears(1));
                break;
            case SEMESTRIEL:
                subscription.setEndDate(subscription.getStartDate().plusMonths(6));
                break;
            case MONTHLY:
                subscription.setEndDate(subscription.getStartDate().plusMonths(1));
                break;
            default:
                log.error("Unknown subscription type: " + subscription.getTypeSub());
        }
        log.info("Subscription added: " + subscription);
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription updateSubscription(Subscription subscription) {
        log.info("Subscription updated: " + subscription);
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription retrieveSubscriptionById(Long numSubscription) {
        Subscription subscription = subscriptionRepository.findById(numSubscription).orElse(null);
        if (subscription != null) {
            log.info("Subscription retrieved: " + subscription);
        } else {
            log.warn("Subscription not found with ID: " + numSubscription);
        }
        return subscription;
    }

    @Override
    public Set<Subscription> getSubscriptionByType(TypeSubscription type) {
        log.info("Fetching subscriptions of type: " + type);
        return subscriptionRepository.findByTypeSubOrderByStartDateAsc(type);
    }

    @Override
    public List<Subscription> retrieveSubscriptionsByDates(LocalDate startDate, LocalDate endDate) {
        log.info("Fetching subscriptions between " + startDate + " and " + endDate);
        return subscriptionRepository.getSubscriptionsByStartDateBetween(startDate, endDate);
    }

    @Override
    @Scheduled(cron = "*/30 * * * * *")  // Runs every 30 seconds
    public List<Subscription> retrieveSubscriptions() {
        log.info("Starting subscription retrieval process");
        for (Subscription sub : subscriptionRepository.findDistinctOrderByEndDateAsc()) {
            Skier skier = skierRepository.findBySubscription(sub);
            log.info("Subscription: " + sub.getNumSub() + " | End date: " + sub.getEndDate()
                    + " | Skier: " + skier.getFirstName() + " " + skier.getLastName());
        }
        log.info("Finished subscription retrieval process");
        return null;
    }

    @Scheduled(cron = "*/30 * * * * *") // Example: Runs every 30 seconds
    public void showMonthlyRecurringRevenue() {
        log.info("Calculating monthly recurring revenue");
        float revenue = subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.MONTHLY)
                + subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.SEMESTRIEL) / 6
                + subscriptionRepository.recurringRevenueByTypeSubEquals(TypeSubscription.ANNUAL) / 12;
        log.info("Monthly Revenue = " + revenue);
    }
}
