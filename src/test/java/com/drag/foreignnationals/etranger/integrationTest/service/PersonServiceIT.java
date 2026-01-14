package com.drag.foreignnationals.etranger.integrationTest.service;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.dto.PersonPatchDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.repository.CommuneRepository;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.service.PersonService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
@Transactional
class PersonServiceIT extends AbstractMySqlIT {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private NationalityRepository nationalityRepository;

    @Autowired
    private CommuneRepository communeRepository;

    // ======================================================
    // CREATE PERSON — POSITIVE FLOWS
    // ======================================================
    @Nested
    class CreatePersonPositive {

        @Test
        void shouldCreatePersonSuccessfully() {
            Nationality nat = createNationality();
            PersonCreateDTO dto = basePersonDto(nat.getId());

            PersonDetailDTO result = personService.create(dto);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("Karim");
            assertThat(result.getNationality().getId()).isEqualTo(nat.getId());
            assertThat(personRepository.existsById(result.getId())).isTrue();
        }

        @Test
        void shouldCreatePersonWithCurrentAddress() {
            Nationality nat = createNationality();
            Commune commune = createCommune();

            PersonCreateDTO dto = basePersonDto(nat.getId());
            dto.setCurrentAddress(addressDto(commune.getId()));

            PersonDetailDTO result = personService.create(dto);

            Person person =
                    personRepository.findById(result.getId()).orElseThrow();

            Address current = person.getCurrentAddress();
            assertThat(current).isNotNull();
            assertThat(current.isCurrent()).isTrue();
            assertThat(current.getCommune().getId()).isEqualTo(commune.getId());
        }
    }

    // ======================================================
    // CREATE PERSON — NEGATIVE FLOWS
    // ======================================================
    @Nested
    class CreatePersonNegative {

        @Test
        void shouldFail_whenAddressProvidedWithoutCommune() {
            Nationality nat = createNationality();
            PersonCreateDTO dto = basePersonDto(nat.getId());

            AddressCreateDto address = new AddressCreateDto();
            address.setStreet("Street");
            address.setCity("City");
            dto.setCurrentAddress(address);

            assertThatThrownBy(() -> personService.create(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Commune is required");

            assertThat(personRepository.count()).isZero();
        }

        @Test
        void shouldFail_whenNationalityNotFound() {
            PersonCreateDTO dto = basePersonDto(999L);

            assertThatThrownBy(() -> personService.create(dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Nationality not found");
        }
    }

    // ======================================================
    // UPDATE PERSON — POSITIVE FLOWS
    // ======================================================
    @Nested
    class UpdatePersonPositive {

        @Test
        void shouldUpdatePerson() {
            Nationality nat1 = createNationality();
            Nationality nat2 = nationalityRepository.save(
                    new Nationality(null, "French", "فرنسي", null)
            );

            PersonDetailDTO created =
                    personService.create(basePersonDto(nat1.getId()));

            PersonCreateDTO updateDto = new PersonCreateDTO();
            updateDto.setFirstName("Zinedine");
            updateDto.setLastName("Zidane");
            updateDto.setNationalityId(nat2.getId());

            PersonDetailDTO updated =
                    personService.update(created.getId(), updateDto);

            assertThat(updated.getFirstName()).isEqualTo("Zinedine");
            assertThat(updated.getNationality().getId()).isEqualTo(nat2.getId());
        }

        @Test
        void shouldUpdateCurrentAddress() {
            Nationality nat = createNationality();
            Commune commune1 = createCommune();
            Commune commune2 = communeRepository.save(
                    new Commune(null, "Oran", "وهران", "3100", null)
            );

            PersonCreateDTO dto = basePersonDto(nat.getId());
            dto.setCurrentAddress(addressDto(commune1.getId()));

            PersonDetailDTO created = personService.create(dto);

            PersonCreateDTO update = new PersonCreateDTO();
            update.setCurrentAddress(addressDto(commune2.getId()));

            personService.update(created.getId(), update);

            Person person =
                    personRepository.findById(created.getId()).orElseThrow();

            Assertions.assertThat(person.getAddresses()).hasSize(1);
            assertThat(person.getCurrentAddress().getCommune().getId())
                    .isEqualTo(commune2.getId());
        }
    }

    // ======================================================
    // UPDATE / PATCH — NEGATIVE FLOWS
    // ======================================================
    @Nested
    class UpdatePatchNegative {

        @Test
        void shouldFail_whenUpdatingNonExistingPerson() {
            PersonCreateDTO dto = basePersonDto(createNationality().getId());

            assertThatThrownBy(() -> personService.update(999L, dto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Person not found");
        }

        @Test
        void shouldFail_whenPatchingNonExistingPerson() {
            PersonPatchDTO patch = new PersonPatchDTO();
            patch.setFirstName(Optional.of("Test"));

            assertThatThrownBy(() -> personService.patch(999L, patch))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Person not found");
        }

        @Test
        void shouldFailWhenUpdatingWithInvalidNationality() {
            // GIVEN
            Nationality nat = createNationality();
            PersonDetailDTO created =
                    personService.create(basePersonDto(nat.getId()));

            PersonCreateDTO update = new PersonCreateDTO();
            update.setNationalityId(999L);

            // WHEN + THEN
            assertThatThrownBy(() -> personService.update(created.getId(), update))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Nationality not found");
        }
    }

    // ======================================================
    // PATCH PERSON — POSITIVE FLOWS
    // ======================================================
    @Nested
    class PatchPersonPositive {

        @Test
        void shouldPatchPersonPartially() {
            Nationality nat = createNationality();
            PersonDetailDTO created =
                    personService.create(basePersonDto(nat.getId()));

            PersonPatchDTO patch = new PersonPatchDTO();
            patch.setFirstName(Optional.of("Riyad"));

            PersonDetailDTO patched =
                    personService.patch(created.getId(), patch);

            assertThat(patched.getFirstName()).isEqualTo("Riyad");
            assertThat(patched.getLastName()).isEqualTo("Benzema");
        }
    }

    // ======================================================
    // READ
    // ======================================================
    @Nested
    class ReadPerson {

        @Test
        void shouldGetPersonById() {
            Nationality nat = createNationality();
            PersonDetailDTO created =
                    personService.create(basePersonDto(nat.getId()));

            PersonDetailDTO found =
                    personService.getById(created.getId());

            assertThat(found.getId()).isEqualTo(created.getId());
        }
    }

    // ======================================================
    // HELPERS
    // ======================================================
    private Nationality createNationality() {
        return nationalityRepository.save(
                new Nationality(null, "Algerian", "جزائري", null)
        );
    }

    private Commune createCommune() {
        return communeRepository.save(
                new Commune(null, "Algiers", "الجزائر", "1601", null)
        );
    }

    private PersonCreateDTO basePersonDto(Long nationalityId) {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Karim");
        dto.setLastName("Benzema");
        dto.setDateOfBirth(LocalDate.now());
        dto.setGender(Person.Gender.MALE);
        dto.setNationalityId(nationalityId);
        return dto;
    }

    private AddressCreateDto addressDto(Long communeId) {
        AddressCreateDto dto = new AddressCreateDto();
        dto.setStreet("Didouche Mourad");
        dto.setCity("Algiers");
        dto.setCommuneId(communeId);
        return dto;
    }
}

