package org.aknezevic.wallet.repository;

import org.aknezevic.wallet.model.Idempotency;
import org.aknezevic.wallet.model.Wallet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SpringBootTest(classes = RedisConfigTest.class)
public class RepositoryTest {

    @Autowired
    WalletRepository walletRepo;
    @Autowired
    IdempotencyRepository idemRepo;

    @Test
    public void walletCrudWorks() {
        UUID id = UUID.randomUUID();
        Wallet wallet = new Wallet(id, new BigDecimal("10.11"));

        walletRepo.save(wallet);

        Wallet foundWallet = walletRepo.findById(id).orElse(null);

        Assertions.assertNotNull(foundWallet);
        Assertions.assertEquals(wallet, foundWallet);

        walletRepo.deleteById(id);
        Assertions.assertNull(walletRepo.findById(id).orElse(null));
    }

    @Test
    public void idempotencyCrudWorks() {
        UUID id = UUID.randomUUID();
        Idempotency idempotency = Idempotency.builder()
                .key(id)
                .response(new Wallet(UUID.randomUUID(), new BigDecimal("10.11")))
                .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        idemRepo.save(idempotency);

        Idempotency foundIdempotency = idemRepo.findById(id).orElse(null);

        Assertions.assertNotNull(foundIdempotency);
        Assertions.assertEquals(idempotency, foundIdempotency);

        idemRepo.deleteById(id);
        Assertions.assertNull(idemRepo.findById(id).orElse(null));
    }
}
