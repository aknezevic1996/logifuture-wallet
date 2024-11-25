package org.aknezevic.wallet.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
