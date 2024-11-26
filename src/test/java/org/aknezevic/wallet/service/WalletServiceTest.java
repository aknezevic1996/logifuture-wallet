package org.aknezevic.wallet.service;

import org.aknezevic.wallet.exception.WalletException;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.repository.WalletRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @InjectMocks
    WalletService service;

    @Mock
    WalletRepository repo;

    private static final UUID ID = UUID.randomUUID();
    private static final BigDecimal BALANCE = new BigDecimal("10.11");
    private static final Wallet WALLET = new Wallet(ID, BALANCE);

    @BeforeEach
    public void setUp() {
        lenient().when(repo.findById(ID)).thenReturn(Optional.of(WALLET));
        lenient().when(repo.save(WALLET)).thenReturn(WALLET);
    }

    @Test
    public void getWalletHappyPath() {
        Wallet wallet = service.getWalletById(ID);

        Assertions.assertEquals(ID, wallet.getId());
        Assertions.assertEquals(new BigDecimal("10.11"), wallet.getBalance());
    }

    @Test
    public void getWalletNotFoundReturnsNull() {
        Wallet wallet = service.getWalletById(UUID.randomUUID());

        Assertions.assertNull(wallet);
    }

    @Test
    public void saveWalletHappyPath() {
        Wallet savedWallet = service.addWallet(WALLET);

        Assertions.assertEquals(WALLET, savedWallet);
    }

    @Test
    public void addFundsHappyPath() {
        lenient().when(repo.save(WALLET)).thenReturn(new Wallet(ID, new BigDecimal("10.12")));
        Wallet updatedWallet = service.addFundsById(ID, new BigDecimal("0.01"));

        Assertions.assertEquals(new BigDecimal("10.12"), updatedWallet.getBalance());
    }

    @Test
    public void addFundsNotFoundReturnsNull() {
        Wallet wallet = service.addFundsById(UUID.randomUUID(), new BigDecimal("11.11"));

        Assertions.assertNull(wallet);
    }

    @Test
    public void removeFundsHappyPath() {
        lenient().when(repo.save(WALLET)).thenReturn(new Wallet(ID, new BigDecimal("10.10")));
        Wallet updatedWallet = service.removeFundsById(ID, new BigDecimal("0.01"));

        Assertions.assertEquals(new BigDecimal("10.10"), updatedWallet.getBalance());
    }

    @Test
    public void removeFundsNotFoundReturnsNull() {
        Wallet wallet = service.removeFundsById(UUID.randomUUID(), new BigDecimal("11.11"));

        Assertions.assertNull(wallet);
    }

    @Test
    public void removeFundsNegativeValue() {
        Assertions.assertThrows(WalletException.class, () -> service.removeFundsById(ID, new BigDecimal("20.20")));
    }

    @Test
    public void updateFundsNegativeAmount() {
        Assertions.assertThrows(WalletException.class, () -> service.addFundsById(ID, new BigDecimal("-22.22")));
    }
}
