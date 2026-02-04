package com.example.finalproject.user.repository;

import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserOrderByIsDefaultDesc(User user);
}
