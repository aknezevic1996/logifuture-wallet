package org.aknezevic.wallet.service;

import lombok.extern.slf4j.Slf4j;
import org.aknezevic.wallet.model.Wallet;
import org.aknezevic.wallet.exception.WalletException;
import org.aknezevic.wallet.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepo;

    public Wallet addWallet(Wallet wallet) {
        return walletRepo.save(wallet);
    }

    public Wallet getWalletById(UUID id) {
        return walletRepo.findById(id).orElse(null);
    }

    public Wallet addFundsById(UUID id, BigDecimal amount) {
        Optional<Wallet> existingWallet = walletRepo.findById(id);

        if (existingWallet.isPresent()) {
            Wallet updatedWallet = existingWallet.get();
            BigDecimal currentBalance = updatedWallet.getBalance();

            updatedWallet.setBalance(currentBalance.add(amount));

            return walletRepo.save(updatedWallet);
        }

        return null;
    }

    public Wallet removeFundsById(UUID id, BigDecimal amount) {
        Optional<Wallet> existingWallet = walletRepo.findById(id);

        if (existingWallet.isPresent()) {
            Wallet updatedWallet = existingWallet.get();
            BigDecimal currentBalance = updatedWallet.getBalance();

            if (currentBalance.subtract(amount).signum() < 0) {
                throw new WalletException("Wallet balance for ID " + id + " cannot be negative!");
            }

            updatedWallet.setBalance(currentBalance.subtract(amount));

            return walletRepo.save(updatedWallet);
        }

        return null;
    }

}
