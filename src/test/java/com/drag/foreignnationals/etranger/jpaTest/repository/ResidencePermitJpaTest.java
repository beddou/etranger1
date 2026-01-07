package com.drag.foreignnationals.etranger.jpaTest.repository;

import com.drag.foreignnationals.etranger.entity.*;
import com.drag.foreignnationals.etranger.enums.ResidenceType;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ResidencePermitJpaTest {

    @Autowired
    private ResidencePermitRepository permitRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private NationalityRepository nationalityRepository;

    @Autowired
    private EntityManager em;

    @Test
    void shouldSaveResidencePermitWithPerson() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "Algerian", "جزائري", null));

        Person person = Person.builder()
                .firstName("Karim")
                .lastName("Benzema")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        person = personRepository.save(person);

        ResidencePermit permit = ResidencePermit.builder()
                .dateOfIssue(LocalDate.of(2024, 1, 1))
                .durationInMonths(12)
                .person(person)
                .type(ResidenceType.Etudiant)
                .build();

        permitRepository.save(permit);

        em.flush();
        em.clear();

        ResidencePermit found =
                permitRepository.findById(permit.getId()).orElseThrow();

        assertThat(found.getPerson().getId()).isEqualTo(person.getId());
        assertThat(found.getDurationInMonths()).isEqualTo(12);
        assertThat(found.getType()).isEqualTo(ResidenceType.Etudiant);
    }

    @Test
    void shouldReturnActivePermit() {

        Person person = Person.builder()
                        .firstName("Yacine")
                        .lastName("B.")
                        .gender(Person.Gender.MALE)
                        .dateOfBirth(LocalDate.of(1990, 1, 1))
                        .nationality(
                                nationalityRepository.save(
                                        new Nationality(null,"Algerian","جزائري",null)
                                )
                        )
                        .build();


        ResidencePermit oldPermit = ResidencePermit.builder()
                        .dateOfIssue(LocalDate.of(2022, 1, 1))
                        .durationInMonths(12)
                        .active(false)
                        .person(person)
                        .type(ResidenceType.Etudiant)
                        .build();



        ResidencePermit activePermit = ResidencePermit.builder()
                        .dateOfIssue(LocalDate.of(2023, 1, 1))
                        .durationInMonths(12)
                        .active(true) //
                        .person(person)
                        .type(ResidenceType.Commerçant)
                        .build();

        person.setResidencePermits(List.of(oldPermit, activePermit));

        personRepository.save(person);

        em.flush();
        em.clear();

        Person found = personRepository.findById(person.getId()).orElseThrow();

        ResidencePermit active = found.getActiveResidencePermit();

        assertThat(active).isNotNull();
        assertThat(active.getType()).isEqualTo(ResidenceType.Commerçant);

    }

    @Test
    void shouldFindResidencePermitsByPersonId() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "French", "فرنسي", null));

        Person person = Person.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1985, 5, 5))
                .nationality(nat)
                .build();

        person = personRepository.save(person);

        ResidencePermit p1 = ResidencePermit.builder()
                .dateOfIssue(LocalDate.of(2023, 1, 1))
                .durationInMonths(12)
                .person(person)
                .type(ResidenceType.Commerçant)
                .build();

        ResidencePermit p2 = ResidencePermit.builder()
                .dateOfIssue(LocalDate.of(2024, 1, 1))
                .durationInMonths(24)
                .person(person)
                .type(ResidenceType.Etudiant)
                .build();

        permitRepository.saveAll(List.of(p1, p2));

        em.flush();
        em.clear();

        List<ResidencePermit> permits =
                permitRepository.findByPersonId(person.getId());

        assertThat(permits).hasSize(2);
    }

    @Test
    void shouldDeleteResidencePermit() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "Italian", "إيطالي", null));

        Person person = Person.builder()
                .firstName("Luca")
                .lastName("Rossi")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .nationality(nat)
                .build();

        person = personRepository.save(person);

        ResidencePermit permit = ResidencePermit.builder()
                .dateOfIssue(LocalDate.of(2024, 6, 1))
                .durationInMonths(6)
                .person(person)
                .type(ResidenceType.Salarié)
                .build();

        permit = permitRepository.save(permit);

        em.flush();
        em.clear();

        permitRepository.deleteById(permit.getId());

        em.flush();
        em.clear();

        assertThat(permitRepository.findById(permit.getId())).isEmpty();
    }

    @Test
    void deletingPermitShouldNotDeletePerson() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "Spanish", "اسباني", null));

        Person person = Person.builder()
                .firstName("Carlos")
                .lastName("Mendez")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        person = personRepository.save(person);

        ResidencePermit permit = ResidencePermit.builder()
                .dateOfIssue(LocalDate.of(2024, 3, 1))
                .durationInMonths(12)
                .person(person)
                .type(ResidenceType.Etudiant)
                .build();

        permit = permitRepository.save(permit);

        em.flush();
        em.clear();

        permitRepository.delete(permit);

        em.flush();
        em.clear();

        assertThat(personRepository.findById(person.getId())).isPresent();
    }
}