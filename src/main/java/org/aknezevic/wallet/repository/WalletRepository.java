package org.aknezevic.wallet.repository;

import org.aknezevic.wallet.model.Wallet;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, UUID> {

}
