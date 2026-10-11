package com.mychance.backend_services.repository;

import com.mychance.backend_services.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

	Optional<Account> findByEmailIgnoreCase(String email);

	Optional<Account> findByIdpSubject(String idpSubject);

	boolean existsByEmailIgnoreCase(String email);
}
