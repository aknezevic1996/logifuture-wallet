package org.aknezevic.wallet.service;

import org.aknezevic.wallet.model.Idempotency;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.repository.IdempotencyRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class IdempotencyServiceTest {

    @InjectMocks
    IdempotencyService service;

    @Mock
    IdempotencyRepository repo;

    private static final UUID IDEMPOTENCY_ID = UUID.randomUUID();
    private static final UUID WALLET_ID = UUID.randomUUID();
    private static final BigDecimal BALANCE = new BigDecimal("10.11");
    private static final Wallet WALLET = new Wallet(WALLET_ID, BALANCE);
    private static Idempotency idempotency;

    @BeforeEach
    public void setUp() {
        idempotency = Idempotency.builder()
                .key(IDEMPOTENCY_ID)
                .response(WALLET)
                .expiry(Instant.ofEpochMilli(1732574683L).plus(24, ChronoUnit.HOURS))
                .build();

        lenient().when(repo.findById(IDEMPOTENCY_ID)).thenReturn(Optional.of(idempotency));
        lenient().when(repo.save(idempotency)).thenReturn(idempotency);
    }

    @Test
    public void getIdempotencyHappyPath() {
        Idempotency fetchedIdempotency = service.getIdempotencyById(IDEMPOTENCY_ID);

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

        lenient().doNothing().when(mockedService).addIdempotency(idempotency);
    }

    @Test
    public void deleteIdempotencyHappyPath() {
        IdempotencyService mockedService = mock(IdempotencyService.class);

        lenient().doNothing().when(mockedService).deleteIdempotency(idempotency);
    }
}
