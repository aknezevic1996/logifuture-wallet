package org.aknezevic.wallet.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@RedisHash("Idempotency")
public class Idempotency {

    @Id
    private UUID key;
    @Valid
    private Wallet response;
    @NotNull
    private Instant expiry;
}
