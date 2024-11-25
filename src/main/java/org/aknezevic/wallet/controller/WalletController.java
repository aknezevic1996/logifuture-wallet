package org.aknezevic.wallet.controller;

import jakarta.validation.Valid;
import org.aknezevic.wallet.model.Idempotency;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.service.IdempotencyService;
import org.aknezevic.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    WalletService walletService;
    @Autowired
    IdempotencyService idempotencyService;

    @GetMapping("/health")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("Pong");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        Wallet wallet = walletService.getWalletById(id);
        if (wallet != null) {
            return ResponseEntity.ok(walletService.getWalletById(id));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Idempotency-Key") UUID idempotencyKey, @Valid @RequestBody Wallet wallet) {
        Idempotency currentKey = idempotencyService.getIdempotencyById(idempotencyKey);

        if (currentKey != null) {
            if (currentKey.getExpiry().isBefore(Instant.now())) {
                idempotencyService.deleteIdempotency(currentKey);
            }
            else {
                return ResponseEntity.ok(currentKey.getResponse());
            }
        }

        Wallet createdWallet = walletService.addWallet(wallet);
        Idempotency newIdempotency = Idempotency.builder()
                .key(idempotencyKey)
                .response(createdWallet)
                .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        idempotencyService.addIdempotency(newIdempotency);

        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateBalance(@RequestHeader("Idempotency-Key") UUID idempotencyKey, @PathVariable UUID id,
                                           @RequestParam Boolean isAddingFunds, @RequestParam BigDecimal amount) {
        Idempotency currentKey = idempotencyService.getIdempotencyById(idempotencyKey);

        if (currentKey != null) {
            if (currentKey.getExpiry().isBefore(Instant.now())) {
                idempotencyService.deleteIdempotency(currentKey);
            }
            else {
                return ResponseEntity.ok(currentKey.getResponse());
            }
        }

        Wallet updatedWallet;
        if (isAddingFunds) {
            updatedWallet = walletService.addFundsById(id, amount);
        }
        else {
            updatedWallet = walletService.removeFundsById(id, amount);
        }

        if (updatedWallet != null) {
            Idempotency newIdempotency = Idempotency.builder()
                    .key(idempotencyKey)
                    .response(updatedWallet)
                    .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                    .build();
            idempotencyService.addIdempotency(newIdempotency);

            return ResponseEntity.ok(updatedWallet);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
