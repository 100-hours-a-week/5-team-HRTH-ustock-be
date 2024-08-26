package com.hrth.ustock.repository;

import com.hrth.ustock.entity.portfolio.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findAllByUserUserId(Long userId);

    Optional<Portfolio> findByNameAndUserUserId(String name, Long userId);
}
