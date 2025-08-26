package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface CommuneRepository extends JpaRepository<Commune, Long> {
}
