package org.aknezevic.wallet.service;

import lombok.extern.slf4j.Slf4j;
import org.aknezevic.wallet.exception.WalletException;
import org.aknezevic.wallet.model.Wallet;
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

    /**
     * Method for saving a wallet to Redis
     *
     * @param wallet wallet object to be saved
     *
     * @return the saved wallet object
     */
    public Wallet addWallet(Wallet wallet) {
        return walletRepo.save(wallet);
    }

    /**
     * Method for retrieving a wallet from Redis
     *
     * @param id UUID of the wallet to be retrieved
     *
     * @return the retrieved wallet object
     */
    public Wallet getWalletById(UUID id) {
        return walletRepo.findById(id).orElse(null);
    }

    /**
     * Method for adding funds to a wallet's balance
     *
     * @param id UUID of the wallet to be retrieved
     * @param amount decimal amount to add to wallet balance
     *
     * @return the updated wallet object
     * @throws WalletException if amount is negative
     */
    public Wallet addFundsById(UUID id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Provided amount is greater than 0.");
            throw new WalletException("Amount must be greater than 0!");
        }

        Optional<Wallet> existingWallet = walletRepo.findById(id);

        if (existingWallet.isPresent()) {
            Wallet updatedWallet = existingWallet.get();
            BigDecimal currentBalance = updatedWallet.getBalance();

            updatedWallet.setBalance(currentBalance.add(amount));

            return walletRepo.save(updatedWallet);
        }

        return null;
    }

    /**
     * Method for adding funds to a wallet's balance
     *
     * @param id UUID of the wallet to be retrieved
     * @param amount decimal amount to add to wallet balance
     *
     * @return the updated wallet object
     * @throws WalletException if amount is negative or amount is greater than wallet's current balance
     */
    public Wallet removeFundsById(UUID id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Provided amount is greater than 0.");
            throw new WalletException("Amount must be greater than 0!");
        }

        Optional<Wallet> existingWallet = walletRepo.findById(id);

        if (existingWallet.isPresent()) {
            Wallet updatedWallet = existingWallet.get();
            BigDecimal currentBalance = updatedWallet.getBalance();

            if (amount.compareTo(currentBalance) > 0) {
                log.error("Provided amount is greater than current wallet balance.");
                throw new WalletException("Amount to subtract from Wallet ID " + id + " is greater than wallet balance!");
            }

            updatedWallet.setBalance(currentBalance.subtract(amount));

            return walletRepo.save(updatedWallet);
        }

        return null;
    }

}
