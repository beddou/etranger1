package com.drag.foreignnationals.etranger.integrationTest.service;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.enums.ResidenceType;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.PersonService;
import com.drag.foreignnationals.etranger.service.ResidencePermitService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;


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

    //@Autowired
    //private EntityManager entityManager;

    @BeforeEach
    void cleanup() {
        // Order matters: delete child tables first
        permitRepository.deleteAll();
        personRepository.deleteAll();
        nationalityRepository.deleteAll();
    }

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
            ResidencePermitDTO result = permitService.create(person.getId(), dto);

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

            ResidencePermitDTO firstPermitDTO = permitService.create(person.getId(), permitDto(person.getId()));
            ResidencePermitDTO secondPermitDTO = permitService.create(person.getId(), permitDto(person.getId()));

            List<ResidencePermit> permits = permitRepository.findByPersonId(person.getId());

            ResidencePermit activePermit = permits.stream()
                    .filter(ResidencePermit::isActive)
                            .findFirst().orElseThrow( () ->
                            new BusinessException(ErrorCode.INVALID_DATA, "No valid residence permit was found ")
                    );

            // THEN
            Assertions.assertThat(permits).hasSize(2);

            long activeCount =
                    permits.stream().filter(ResidencePermit::isActive).count();

            assertThat(activeCount).isEqualTo(1);

            // Active permit should be the second one
            assertThat(activePermit.getId())
                    .isEqualTo(secondPermitDTO.getId());

            // Optionally, check the first permit is inactive
            ResidencePermit firstPermit = permits.stream()
                    .filter(p -> !p.isActive())
                    .findFirst()
                    .orElseThrow();

            assertThat(firstPermit.getId()).isEqualTo(firstPermitDTO.getId());
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
            assertThatThrownBy(() -> permitService.create(999L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Person not found with ID 999");
        }

        @Test
        void shouldFailWhenPersonDoesNotExist() {
            // GIVEN
            ResidencePermitDTO dto = permitDto(999L);

            // THEN
            assertThatThrownBy(() -> permitService.create(999L, dto))
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
                permitService.create(person.getId(), permitDto(person.getId()));

        created.setDurationInMonths(36);

        // WHEN
        ResidencePermitDTO updated =
                permitService.update( person.getId(),created.getId(), created);

        // THEN
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getDurationInMonths()).isEqualTo(36);

    }

    @Test
    void shouldDeleteInactivePermit() {
        // GIVEN
        Person person = createPerson();
        ResidencePermitDTO firstPermitDTO =
                permitService.create(person.getId(), permitDto(person.getId()));

        ResidencePermitDTO secondPermitDTO =
                permitService.create(person.getId(), permitDto(person.getId()));

        assertThat(permitRepository.existsById(firstPermitDTO.getId())).isTrue();
        assertThat(permitRepository.existsById(secondPermitDTO.getId())).isTrue();
        permitService.delete(person.getId(),firstPermitDTO.getId());

        // THEN
        assertThat(permitRepository.existsById(firstPermitDTO.getId())).isFalse();
        assertThat(permitRepository.existsById(secondPermitDTO.getId())).isTrue();
        assertThat(personRepository.existsById(person.getId())).isTrue();
    }

    @Test
    void shouldFailWhenDeletingActivePermit() {
        Person person = createPerson();
        ResidencePermitDTO created = permitService.create(person.getId(), permitDto(person.getId()));

        assertThatThrownBy(() -> permitService.delete(person.getId(),created.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot delete active residence permit");
    }

    @Test
    void shouldFailWhenPermitNotFound() {
        Exception ex = assertThrows(Exception.class, () -> permitService.delete(999L,999L));
        assertThat(ex.getMessage()).contains("Residence permit not found for this person with ID 999");
    }

    @Test
    void shouldGetPermitsByPersonId() {
        // GIVEN
        Person person = createPerson();
        permitService.create(person.getId(), permitDto(person.getId()));
        permitService.create(person.getId(), permitDto(person.getId()));

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

