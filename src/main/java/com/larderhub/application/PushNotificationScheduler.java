package com.larderhub.application;

import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.model.PantryItem;
import com.larderhub.domain.model.PushSubscription;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.pantry.PantryItemPersistencePort;
import com.larderhub.domain.ports.out.push.PushSubscriptionPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushNotificationScheduler {

  private final PushSubscriptionPersistencePort subscriptionPort;
  private final HouseholdMembersPersistencePort memberPort;
  private final PantryItemPersistencePort pantryPort;
  private final PushSenderService pushSender;

  @Scheduled(cron = "0 0 9 * * *")
  public void sendExpirationAlerts() {
    log.info("Running expiration push notification job");
    LocalDate today = LocalDate.now();
    LocalDate in3days = today.plusDays(3);

    for (PushSubscription sub : subscriptionPort.findAll()) {
      List<Long> householdIds = memberPort.findAllByUserId(sub.getUserId())
          .stream().map(HouseholdMember::getHouseholdId).toList();

      for (Long householdId : householdIds) {
        List<PantryItem> expiring = pantryPort.findExpiringItems(householdId, today, in3days);
        if (!expiring.isEmpty()) {
          int count = expiring.size();
          String body = count == 1
              ? "1 producto caduca en menos de 3 días"
              : count + " productos caducan en menos de 3 días";
          pushSender.sendToUser(sub, "LarderHub — Caducidades", body);
          break; // one notification per user per run, even across multiple households
        }
      }
    }
  }

  // Manual trigger for testing — call GET /api/v1/push/test-notify
  public void triggerManually() {
    sendExpirationAlerts();
  }
}
