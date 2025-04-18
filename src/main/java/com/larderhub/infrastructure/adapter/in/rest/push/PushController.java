package com.larderhub.infrastructure.adapter.in.rest.push;

import com.larderhub.application.PushNotificationScheduler;
import com.larderhub.domain.ports.in.push.PushNotificationUseCase;
import com.larderhub.infrastructure.adapter.in.rest.push.dto.PushSubscribeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/push")
@RequiredArgsConstructor
public class PushController {

  private final PushNotificationUseCase pushUseCase;
  private final PushNotificationScheduler scheduler;

  @PostMapping("/subscribe")
  public ResponseEntity<Void> subscribe(
      @Valid @RequestBody PushSubscribeRequest req,
      @AuthenticationPrincipal UserDetails userDetails) {
    pushUseCase.subscribe(req.getEndpoint(), req.getP256dh(), req.getAuth(), userDetails.getUsername());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/subscribe")
  public ResponseEntity<Void> unsubscribe(
      @AuthenticationPrincipal UserDetails userDetails) {
    pushUseCase.unsubscribe(userDetails.getUsername());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/test-notify")
  public ResponseEntity<Void> testNotify() {
    scheduler.triggerManually();
    return ResponseEntity.ok().build();
  }
}
