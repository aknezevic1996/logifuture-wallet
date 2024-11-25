package org.aknezevic.wallet.service;

import org.aknezevic.wallet.exception.WalletException;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;

@SpringBootTest
public class WalletServiceTest {

    @Autowired
    WalletService service;

    @MockBean
    WalletRepository repo;

    @BeforeEach
    public void setUp() {
        UUID id = UUID.fromString("5010471b-dc72-4fdd-95bf-820162c4a2d1");
        Wallet wallet = new Wallet(id, new BigDecimal("10.11"));

        given(repo.findById(id)).willReturn(Optional.of(wallet));
        given(repo.save(wallet)).willReturn(wallet);
    }

    @Test
    public void getWalletHappyPath() {
        UUID id = UUID.fromString("5010471b-dc72-4fdd-95bf-820162c4a2d1");
        Wallet wallet = service.getWalletById(id);

        Assertions.assertEquals(id, wallet.getId());
        Assertions.assertEquals(new BigDecimal("10.11"), wallet.getBalance());
    }

    @Test
    public void getWalletNotFoundReturnsNull() {
        Wallet wallet = service.getWalletById(UUID.randomUUID());

        Assertions.assertNull(wallet);
    }

    @Test
    public void saveWalletHappyPath() {
        Wallet wallet = new Wallet(UUID.fromString("5010471b-dc72-4fdd-95bf-820162c4a2d1"), new BigDecimal("10.11"));

        Wallet savedWallet = service.addWallet(wallet);

        Assertions.assertEquals(wallet, savedWallet);
    }

    @Test
    public void addFundsHappyPath() {
        UUID id = UUID.fromString("5010471b-dc72-4fdd-95bf-820162c4a2d1");
        Wallet wallet = new Wallet(id, new BigDecimal("10.11"));

        given(repo.save(wallet)).willReturn(new Wallet(id, new BigDecimal("10.12")));
        Wallet updatedWallet = service.addFundsById(id, new BigDecimal("0.01"));

        Assertions.assertEquals(new BigDecimal("10.12"), updatedWallet.getBalance());
    }

    @Test
    public void addFundsNotFoundReturnsNull() {
        Wallet wallet = service.addFundsById(UUID.randomUUID(), new BigDecimal("11.11"));

        Assertions.assertNull(wallet);
    }

    @Test
    public void removeFundsHappyPath() {
        UUID id = UUID.fromString("5010471b-dc72-4fdd-95bf-820162c4a2d1");
        Wallet wallet = new Wallet(id, new BigDecimal("10.11"));

        given(repo.save(wallet)).willReturn(new Wallet(id, new BigDecimal("10.10")));
        Wallet updatedWallet = service.removeFundsById(id, new BigDecimal("0.01"));

        Assertions.assertEquals(new BigDecimal("10.10"), updatedWallet.getBalance());
    }

    @Test
    public void removeFundsNotFoundReturnsNull() {
        Wallet wallet = service.removeFundsById(UUID.randomUUID(), new BigDecimal("11.11"));

        Assertions.assertNull(wallet);
    }

    @Test
    public void removeFundsNegativeValue() {
        UUID id = UUID.fromString("5010471b-dc72-4fdd-95bf-820162c4a2d1");

        Assertions.assertThrows(WalletException.class, () -> service.removeFundsById(id, new BigDecimal("20.20")));
    }
}
