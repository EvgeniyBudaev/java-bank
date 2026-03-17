package com.transferservice.controller;

import com.transferservice.aspect.LogMethodExecutionTime;
import com.transferservice.controller.request.RequestTransferDto;
import com.transferservice.controller.response.ResponseAccountDto;
import com.transferservice.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST-контроллер для переводов между счетами.
 */
@RestController
@Validated
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/transfer")
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    @LogMethodExecutionTime
    public ResponseEntity<ResponseAccountDto> transfer(
            @Valid @RequestBody RequestTransferDto dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("TransferController transfer: dto={}", dto);
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(transferService.transfer(authenticatedUserId, dto));
    }
}
