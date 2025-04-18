package com.larderhub.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.larderhub.domain.model.HouseholdMember;
import com.larderhub.domain.model.PushSubscription;
import com.larderhub.domain.ports.out.householdMembers.HouseholdMembersPersistencePort;
import com.larderhub.domain.ports.out.push.PushSubscriptionPersistencePort;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushSenderService {

  private final PushService pushService;
  private final PushSubscriptionPersistencePort subscriptionPort;
  private final HouseholdMembersPersistencePort memberPort;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public PushSenderService(
      @Value("${push.vapid.public-key}") String vapidPublicKey,
      @Value("${push.vapid.private-key}") String vapidPrivateKey,
      @Value("${push.vapid.subject}") String vapidSubject,
      PushSubscriptionPersistencePort subscriptionPort,
      HouseholdMembersPersistencePort memberPort) throws Exception {
    Security.addProvider(new BouncyCastleProvider());
    this.pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
    this.subscriptionPort = subscriptionPort;
    this.memberPort = memberPort;
  }

  public void sendToUser(PushSubscription sub, String title, String body) {
    try {
      String payload = objectMapper.writeValueAsString(Map.of("title", title, "body", body));
      Notification notification = new Notification(sub.getEndpoint(), sub.getP256dh(), sub.getAuth(), payload);
      pushService.send(notification);
    } catch (Exception e) {
      log.warn("Failed to send push to userId={}: {}", sub.getUserId(), e.getMessage());
    }
  }

  public void sendToHouseholdMembers(Long householdId, Long excludeUserId, String title, String body) {
    List<Long> memberIds = memberPort.findByHouseholdId(householdId)
        .stream().map(HouseholdMember::getUserId)
        .filter(id -> !id.equals(excludeUserId))
        .toList();
    if (memberIds.isEmpty()) return;
    List<PushSubscription> subscriptions = subscriptionPort.findByUserIds(memberIds);
    for (PushSubscription sub : subscriptions) {
      sendToUser(sub, title, body);
    }
  }
}
