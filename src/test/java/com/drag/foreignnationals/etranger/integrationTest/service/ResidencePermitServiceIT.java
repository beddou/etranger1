package com.drag.foreignnationals.etranger.integrationTest.service;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.enums.ResidenceType;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.PersonService;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
class ResidencePermitServiceIT extends AbstractMySqlIT {

    @Autowired
    private ResidencePermitService permitService;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ResidencePermitRepository permitRepository;

    @Autowired
    private NationalityRepository nationalityRepository;

    @Autowired
    private EntityManager entityManager;

    // ============================================================
    // POSITIVE BUSINESS FLOWS
    // ============================================================

    @Nested
    class CreatePermitPositive {

        @Test
        void shouldCreateResidencePermit() {
            // GIVEN
            Person person = createPerson();
            ResidencePermitDTO dto = permitDto(person.getId());

            // WHEN
            ResidencePermitDTO result = permitService.create(dto);

            // THEN
            assertThat(result.getId()).isNotNull();
            assertThat(result.isActive()).isTrue();

            ResidencePermit saved =
                    permitRepository.findById(result.getId()).orElseThrow();

            assertThat(saved.getPerson().getId()).isEqualTo(person.getId());
        }

        @Test
        void shouldDeactivatePreviousActivePermitAndCreateNewOne() {
            // GIVEN
            Person person = createPerson();

            ResidencePermitDTO first = permitService.create(permitDto(person.getId()));
            ResidencePermitDTO second = permitService.create(permitDto(person.getId()));

            // WHEN
            Person reloaded =
                    personRepository.findById(person.getId()).orElseThrow();

            List<ResidencePermit> permits = reloaded.getResidencePermits();

            // THEN
            Assertions.assertThat(permits).hasSize(2);

            long activeCount =
                    permits.stream().filter(ResidencePermit::isActive).count();

            assertThat(activeCount).isEqualTo(1);
            assertThat(reloaded.getActiveResidencePermit().getId())
                    .isEqualTo(second.getId());
        }
    }

    // ============================================================
    // NEGATIVE BUSINESS RULES
    // ============================================================

    @Nested
    class CreatePermitNegative {

        @Test
        void shouldFailWhenPersonIdIsMissing() {
            // GIVEN
            ResidencePermitDTO dto = permitDto(null);

            // THEN
            assertThatThrownBy(() -> permitService.create(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Person information is required");
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            // GIVEN
            ResidencePermitDTO dto = permitDto(999L);

            // THEN
            assertThatThrownBy(() -> permitService.create(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Person not found");
        }
    }

    // ============================================================
    // UPDATE / DELETE / READ
    // ============================================================

    @Test
    void shouldUpdateResidencePermit() {
        // GIVEN
        Person person = createPerson();
        ResidencePermitDTO created =
                permitService.create(permitDto(person.getId()));

        created.setDurationInMonths(36);

        // WHEN
        ResidencePermitDTO updated =
                permitService.update(created.getId(), created);

        // THEN
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getDurationInMonths()).isEqualTo(36);

    }

    @Test
    void shouldDeleteResidencePermit() {
        // GIVEN
        Person person = createPerson();
        ResidencePermitDTO created =
                permitService.create(permitDto(person.getId()));

        // WHEN

        entityManager.flush();
        entityManager.clear();

        permitService.delete(created.getId());

        entityManager.flush();
        entityManager.clear();

        // THEN
        assertThat(permitRepository.existsById(created.getId())).isFalse();
        assertThat(personRepository.existsById(person.getId())).isTrue();
    }

    @Test
    void shouldGetPermitsByPersonId() {
        // GIVEN
        Person person = createPerson();
        permitService.create(permitDto(person.getId()));
        permitService.create(permitDto(person.getId()));

        // WHEN
        List<ResidencePermitDTO> permits =
                permitService.getByPersonId(person.getId());

        // THEN
        assertThat(permits.size()).isEqualTo(2);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private Person createPerson() {
        Nationality nat = nationalityRepository.save(
                new Nationality(null, "Algerian", "جزائري", null)
        );

        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Karim");
        dto.setLastName("Benzema");
        dto.setDateOfBirth(LocalDate.now());
        dto.setGender(Person.Gender.MALE);
        dto.setNationalityId(nat.getId());

        return personRepository.findById(
                personService.create(dto).getId()
        ).orElseThrow();
    }

    private ResidencePermitDTO permitDto(Long personId) {
        ResidencePermitDTO dto = new ResidencePermitDTO();
        dto.setPersonId(personId);
        dto.setDateOfIssue(LocalDate.now());
        dto.setDurationInMonths(24);
        dto.setType(ResidenceType.Commerçant);
        return dto;
    }
}

