package com.itau.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itau.api.model.Contrato;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, UUID> {
    Optional<Contrato> findByAccountId(String accountId);
}
