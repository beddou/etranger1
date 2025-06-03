package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Nationality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NationalityRepository extends JpaRepository<Nationality, Long> {
}