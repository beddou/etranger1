package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    List<Person> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query("""
        SELECT p FROM Person p
        LEFT JOIN p.nationality n
        LEFT JOIN p.situation s
        WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(n.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(s.type) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<Person> search(@Param("keyword") String keyword, Pageable pageable);
}