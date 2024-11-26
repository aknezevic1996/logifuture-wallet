package org.aknezevic.wallet.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    WalletService walletService;
    @Autowired
    IdempotencyService idempotencyService;

    /**
     * Health endpoint to verify server is responding to requests.
     *
     * @return String "Pong"
     */
    @GetMapping("/health")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok("Pong");
    }

    /**
     * GET endpoint for retrieving a wallet
     *
     * @param id UUID of the wallet to be retrieved
     *
     * @return the wallet object
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        log.debug("Retrieving wallet of ID {}", id);
        Wallet wallet = walletService.getWalletById(id);
        if (wallet != null) {
            return ResponseEntity.ok(walletService.getWalletById(id));
        }

        log.warn("Wallet of ID {} was not found.", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * POST endpoint for creating a wallet
     *
     * @param idempotencyKey header with a UUID value
     * @param wallet wallet object to be created
     *
     * @return the created wallet object
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("Idempotency-Key") UUID idempotencyKey, @Valid @RequestBody Wallet wallet) {
        log.debug("Checking for existing idempotency key of ID {}", idempotencyKey);
        Idempotency currentKey = idempotencyService.getIdempotencyById(idempotencyKey);

        if (currentKey != null) {
            if (currentKey.getExpiry().isBefore(Instant.now())) {
                log.warn("Found idempotency of ID {} has passed expiry, deleting hash.", idempotencyKey);
                idempotencyService.deleteIdempotency(currentKey);
            }
            else {
                log.info("Returning cached wallet from idempotency hash.");
                return ResponseEntity.ok(currentKey.getResponse());
            }
        }

        log.debug("Creating new wallet of ID {}", wallet.getId());
        Wallet createdWallet = walletService.addWallet(wallet);
        Idempotency newIdempotency = Idempotency.builder()
                .key(idempotencyKey)
                .response(createdWallet)
                .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();
        log.debug("Creating new idempotency of ID {}", idempotencyKey);
        idempotencyService.addIdempotency(newIdempotency);

        return new ResponseEntity<>(createdWallet, HttpStatus.CREATED);
    }

    /**
     * POST endpoint for creating a wallet
     *
     * @param idempotencyKey header with a UUID value
     * @param id UUID of the wallet to be updated
     * @param isAddingFunds boolean value indicating addition (true) or subtraction (false) of wallet balance
     * @param amount decimal amount to be added or subtracted from current wallet balance
     *
     * @return the updated wallet object
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateBalance(@RequestHeader("Idempotency-Key") UUID idempotencyKey, @PathVariable UUID id,
                                           @RequestParam Boolean isAddingFunds, @RequestParam BigDecimal amount) {
        log.debug("Checking for existing idempotency key of ID {}", idempotencyKey);
        Idempotency currentKey = idempotencyService.getIdempotencyById(idempotencyKey);

        if (currentKey != null) {
            if (currentKey.getExpiry().isBefore(Instant.now())) {
                log.warn("Found idempotency of ID {} has passed expiry, deleting hash.", idempotencyKey);
                idempotencyService.deleteIdempotency(currentKey);
            }
            else {
                log.info("Returning cached wallet from idempotency hash.");
                return ResponseEntity.ok(currentKey.getResponse());
            }
        }

        log.debug("Updating balance for wallet of ID {}", id);
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
            log.debug("Creating new idempotency of ID {}", idempotencyKey);
            idempotencyService.addIdempotency(newIdempotency);

            return ResponseEntity.ok(updatedWallet);
        }

        log.warn("Wallet of ID {} was not found.", id);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
