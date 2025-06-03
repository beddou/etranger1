package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Situation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SituationRepository extends JpaRepository<Situation, Long> {
}