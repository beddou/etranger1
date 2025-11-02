package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidencePermitRepository extends JpaRepository<ResidencePermit, Long> {
    List<ResidencePermit> findByPersonId(Long personId);
}
