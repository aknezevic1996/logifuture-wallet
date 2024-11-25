package org.aknezevic.wallet.service;

import org.aknezevic.wallet.model.Idempotency;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.repository.IdempotencyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class IdempotencyServiceTest {

    @Autowired
    IdempotencyService service;

    @MockBean
    IdempotencyRepository repo;

    @BeforeEach
    public void setUp() {
        UUID id = UUID.fromString("8f873de5-8f8d-4c7c-84ad-b9b477c7d229");
        UUID walletId = UUID.fromString("310c4614-c9b3-45da-bdfd-2b1bc3151309");
        Idempotency idempotency = Idempotency.builder()
                .key(id)
                .response(new Wallet(walletId, new BigDecimal("10.11")))
                .expiry(Instant.ofEpochMilli(1732574683L).plus(24, ChronoUnit.HOURS))
                .build();

        given(repo.findById(id)).willReturn(Optional.of(idempotency));
        given(repo.save(idempotency)).willReturn(idempotency);
    }

    @Test
    public void getIdempotencyHappyPath() {
        UUID id = UUID.fromString("8f873de5-8f8d-4c7c-84ad-b9b477c7d229");
        Idempotency idempotency = Idempotency.builder()
                .key(id)
                .response(new Wallet(UUID.fromString("310c4614-c9b3-45da-bdfd-2b1bc3151309"), new BigDecimal("10.11")))
                .expiry(Instant.ofEpochMilli(1732574683L).plus(24, ChronoUnit.HOURS))
                .build();

        Idempotency fetchedIdempotency = service.getIdempotencyById(id);

        Assertions.assertEquals(fetchedIdempotency, idempotency);
    }

    @Test
    public void getIdempotencyNotFoundReturnsNull() {
        Idempotency idempotency = service.getIdempotencyById(UUID.randomUUID());

        Assertions.assertNull(idempotency);
    }

    @Test
    public void saveIdempotencyHappyPath() {
        IdempotencyService mockedService = mock(IdempotencyService.class);
        Idempotency idempotency = Idempotency.builder()
                .key(UUID.fromString("8f873de5-8f8d-4c7c-84ad-b9b477c7d229"))
                .response(new Wallet(UUID.fromString("310c4614-c9b3-45da-bdfd-2b1bc3151309"), new BigDecimal("10.11")))
                .expiry(Instant.ofEpochMilli(1732574683L).plus(24, ChronoUnit.HOURS))
                .build();

        willDoNothing().given(mockedService).addIdempotency(idempotency);
    }

    @Test
    public void deleteIdempotencyHappyPath() {
        IdempotencyService mockedService = mock(IdempotencyService.class);
        Idempotency idempotency = Idempotency.builder()
                .key(UUID.fromString("8f873de5-8f8d-4c7c-84ad-b9b477c7d229"))
                .response(new Wallet(UUID.fromString("310c4614-c9b3-45da-bdfd-2b1bc3151309"), new BigDecimal("10.11")))
                .expiry(Instant.ofEpochMilli(1732574683L).plus(24, ChronoUnit.HOURS))
                .build();

        willDoNothing().given(mockedService).deleteIdempotency(idempotency);
    }
}
