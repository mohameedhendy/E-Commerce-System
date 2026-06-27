package com.ecommerce.ecommerce_backend.dao;

import com.ecommerce.ecommerce_backend.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressDAO extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);
}
