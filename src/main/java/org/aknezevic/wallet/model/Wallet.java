package org.aknezevic.wallet.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Wallet")
public class Wallet {
    @Id
    private UUID id;
    @NotNull(message = "Balance cannot be null")
    @Min(value = 0, message = "Balance cannot be less than 0")
    private BigDecimal balance;
}
