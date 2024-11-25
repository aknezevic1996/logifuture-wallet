package org.aknezevic.wallet.repository;

import org.aknezevic.wallet.model.Idempotency;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IdempotencyRepository extends CrudRepository<Idempotency, UUID> {

}
