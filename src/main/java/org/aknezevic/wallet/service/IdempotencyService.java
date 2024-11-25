package org.aknezevic.wallet.service;

import org.aknezevic.wallet.model.Idempotency;
import org.aknezevic.wallet.repository.IdempotencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdempotencyService {

    @Autowired
    private IdempotencyRepository idempotencyRepo;

    public Idempotency getIdempotencyById(UUID idempotencyKey) {
        return idempotencyRepo.findById(idempotencyKey).orElse(null);
    }

    public void addIdempotency(Idempotency idempotency) {
        idempotencyRepo.save(idempotency);
    }

    public void deleteIdempotency(Idempotency idempotency) {
        idempotencyRepo.delete(idempotency);
    }

}
