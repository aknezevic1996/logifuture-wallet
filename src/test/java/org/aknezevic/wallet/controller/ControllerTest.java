package org.aknezevic.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aknezevic.wallet.model.Idempotency;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.service.IdempotencyService;
import org.aknezevic.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    WalletService walletService;
    @MockBean
    IdempotencyService idempotencyService;

    @Value("${auth.token}")
    private String AUTH_TOKEN;

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void getWalletHappyPath() throws Exception {
        UUID id = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        Wallet wallet = new Wallet(id, balance);

        given(walletService.getWalletById(id)).willReturn(wallet);

        mvc.perform(get("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    public void getWalletNotFoundReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(walletService.getWalletById(id)).willReturn(null);

        mvc.perform(get("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createWalletHappyPath() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        Wallet wallet = new Wallet(id, balance);
        Idempotency idempotency = Idempotency.builder()
                .key(idem)
                .response(wallet)
                .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        given(idempotencyService.getIdempotencyById(UUID.randomUUID())).willReturn(null);
        given(walletService.addWallet(wallet)).willReturn(wallet);
        willDoNothing().given(idempotencyService).addIdempotency(idempotency);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(post("/api/wallet").header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    public void createWalletIdempotencyExpired() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        Wallet wallet = new Wallet(id, balance);
        Idempotency idempotency = Idempotency.builder()
                .key(idem)
                .response(wallet)
                .expiry(Instant.now().minus(48, ChronoUnit.HOURS))
                .build();
        Idempotency newIdempotency = Idempotency.builder()
                .key(idem)
                .response(wallet)
                .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        given(idempotencyService.getIdempotencyById(idem)).willReturn(idempotency);
        willDoNothing().given(idempotencyService).deleteIdempotency(idempotency);
        given(walletService.addWallet(wallet)).willReturn(wallet);
        willDoNothing().given(idempotencyService).addIdempotency(newIdempotency);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(post("/api/wallet").header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    public void createWalletIdempotencyMatch() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        Wallet wallet = new Wallet(id, balance);
        Idempotency idempotency = Idempotency.builder()
                .key(idem)
                .response(wallet)
                .expiry(Instant.now().plus(10, ChronoUnit.HOURS))
                .build();

        given(idempotencyService.getIdempotencyById(idem)).willReturn(idempotency);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(post("/api/wallet").header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    public void addFundsHappyPath() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("20.22");
        Wallet wallet = new Wallet(id, balance);
        Wallet updatedWallet = new Wallet(id, balance.add(amount));

        given(idempotencyService.getIdempotencyById(UUID.randomUUID())).willReturn(null);
        given(walletService.addFundsById(id, amount)).willReturn(updatedWallet);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "true")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(updatedWallet.getBalance()));
    }

    @Test
    public void addFundsNotFoundReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("20.22");
        Wallet wallet = new Wallet(id, balance);

        given(idempotencyService.getIdempotencyById(UUID.randomUUID())).willReturn(null);
        given(walletService.addFundsById(id, amount)).willReturn(null);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "true")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeFundsHappyPath() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("1.02");
        Wallet wallet = new Wallet(id, balance);
        Wallet updatedWallet = new Wallet(id, balance.subtract(amount));

        given(idempotencyService.getIdempotencyById(UUID.randomUUID())).willReturn(null);
        given(walletService.removeFundsById(id, amount)).willReturn(updatedWallet);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "false")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(updatedWallet.getBalance()));
    }

    @Test
    public void removeFundsNotFoundReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("20.22");
        Wallet wallet = new Wallet(id, balance);

        given(idempotencyService.getIdempotencyById(UUID.randomUUID())).willReturn(null);
        given(walletService.removeFundsById(id, amount)).willReturn(null);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "false")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateFundsIdempotencyExpired() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("20.22");
        Wallet wallet = new Wallet(id, balance);
        Wallet updatedWallet = new Wallet(id, balance.add(amount));
        Idempotency idempotency = Idempotency.builder()
                .key(idem)
                .response(wallet)
                .expiry(Instant.now().minus(48, ChronoUnit.HOURS))
                .build();
        Idempotency newIdempotency = Idempotency.builder()
                .key(idem)
                .response(wallet)
                .expiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .build();

        given(idempotencyService.getIdempotencyById(idem)).willReturn(idempotency);
        willDoNothing().given(idempotencyService).deleteIdempotency(idempotency);
        given(walletService.addFundsById(id, amount)).willReturn(updatedWallet);
        willDoNothing().given(idempotencyService).addIdempotency(newIdempotency);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "true")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(updatedWallet.getBalance()));
    }

    @Test
    public void updateFundsIdempotencyMatches() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("1.02");
        Wallet wallet = new Wallet(id, balance);
        Wallet updatedWallet = new Wallet(id, balance.subtract(amount));
        Idempotency idempotency = Idempotency.builder()
                .key(idem)
                .response(updatedWallet)
                .expiry(Instant.now().plus(10, ChronoUnit.HOURS))
                .build();

        given(idempotencyService.getIdempotencyById(idem)).willReturn(idempotency);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/{id}", id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "false")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(updatedWallet.getBalance()));
    }

    @Test
    public void noBalanceReturnsBadRequest() throws Exception {
        Wallet wallet = new Wallet(UUID.randomUUID(), null);
        String content = mapper.writeValueAsString(wallet);

        mvc.perform(post("/api/wallet").header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void negativeBalanceReturnsBadRequest() throws Exception {
        Wallet wallet = new Wallet(UUID.randomUUID(), new BigDecimal("-22.22"));
        String content = mapper.writeValueAsString(wallet);

        mvc.perform(post("/api/wallet").header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }
}
