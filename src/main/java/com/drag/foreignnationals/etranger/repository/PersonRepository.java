package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
}