package org.aknezevic.wallet;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.mockito.BDDMockito.given;
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

        mvc.perform(get("/api/wallet/" + id).header("X-API-KEY", AUTH_TOKEN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(balance));
    }

    @Test
    public void createWalletHappyPath() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        Wallet wallet = new Wallet(id, balance);

        given(idempotencyService.getIdempotencyById(idem)).willReturn(null);
        given(walletService.addWallet(wallet)).willReturn(wallet);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(post("/api/wallet").header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isCreated())
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

        given(idempotencyService.getIdempotencyById(idem)).willReturn(null);
        given(walletService.addFundsById(id, amount)).willReturn(updatedWallet);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/" + id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "true")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(updatedWallet.getBalance()));
    }

    @Test
    public void removeFundsHappyPath() throws Exception {
        UUID id = UUID.randomUUID();
        UUID idem = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("10.11");
        BigDecimal amount = new BigDecimal("1.02");
        Wallet wallet = new Wallet(id, balance);
        Wallet updatedWallet = new Wallet(id, balance.subtract(amount));

        given(idempotencyService.getIdempotencyById(idem)).willReturn(null);
        given(walletService.removeFundsById(id, amount)).willReturn(updatedWallet);

        String content = mapper.writeValueAsString(wallet);

        mvc.perform(patch("/api/wallet/" + id).header("X-API-KEY", AUTH_TOKEN).header("Idempotency-Key", idem)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isAddingFunds", "false")
                        .param("amount", amount.toString())
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.balance").value(updatedWallet.getBalance()));
    }
}
