package com.hrth.ustock.repository;

import com.hrth.ustock.entity.main.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByProviderAndProviderId(String provider, String providerId);
}
