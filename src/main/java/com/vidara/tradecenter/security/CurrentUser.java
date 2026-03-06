package com.vidara.tradecenter.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;


// CUSTOM ANNOTATION
// Use this in controllers to get current logged-in user
//
// usage:
//   @GetMapping("/profile")
//   public ResponseEntity<?> getProfile(@CurrentUser CustomUserDetails user) {
//       Long userId = user.getId();
//       // ...
//   }


@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}