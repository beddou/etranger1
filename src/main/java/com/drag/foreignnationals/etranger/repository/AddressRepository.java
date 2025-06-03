package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByPersonId(Long personId);
}
