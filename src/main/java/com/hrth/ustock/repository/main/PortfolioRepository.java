package com.hrth.ustock.repository.main;

import com.hrth.ustock.entity.main.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByUserUserId(Long userId);

    Optional<Portfolio> findByNameAndUserUserId(String name, Long userId);
}
