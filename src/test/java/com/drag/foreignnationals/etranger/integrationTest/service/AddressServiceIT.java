package com.drag.foreignnationals.etranger.integrationTest.service;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.repository.AddressRepository;
import com.drag.foreignnationals.etranger.repository.CommuneRepository;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.service.AddressService;
import com.drag.foreignnationals.etranger.service.PersonService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Transactional
class AddressServiceIT extends AbstractMySqlIT {

    @Autowired
    private AddressService addressService;

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CommuneRepository communeRepository;

    @Autowired
    private NationalityRepository nationalityRepository;

    // ======================================================
    // ADD ADDRESS — POSITIVE FLOWS
    // ======================================================
    @Nested
    class AddAddressPositive {

        @Test
        void shouldAddFirstAddressAsCurrent() {
            // GIVEN
            Person person = createPerson();
            Commune commune = createCommune();

            AddressCreateDto dto = addressDto(commune.getId());

            // WHEN
            AddressDTO result =
                    addressService.add(person.getId(), dto);

            // THEN
            assertThat(result.getId()).isNotNull();
            assertThat(result.isCurrent()).isTrue();

            Person reloaded =
                    personRepository.findById(person.getId()).orElseThrow();

            Assertions.assertThat(reloaded.getAddresses()).hasSize(1);
            assertThat(reloaded.getCurrentAddress()).isNotNull();
        }

        @Test
        void shouldDeactivateOldCurrentAddressAndCreateNewOne() {
            // GIVEN
            Person person = createPerson();
            Commune commune1 = createCommune();
            Commune commune2 = communeRepository.save(
                    new Commune(null, "Oran", "وهران", "3100", null)
            );

            addressService.add(person.getId(), addressDto(commune1.getId()));

            // WHEN
            addressService.add(person.getId(), addressDto(commune2.getId()));

            // THEN
            Person reloaded =
                    personRepository.findById(person.getId()).orElseThrow();

            Assertions.assertThat(reloaded.getAddresses()).hasSize(2);
            assertThat(reloaded.getCurrentAddress().getCommune().getId())
                    .isEqualTo(commune2.getId());

            long currentCount =
                    reloaded.getAddresses().stream().filter(Address::isCurrent).count();

            assertThat(currentCount).isEqualTo(1);
        }
    }

    // ======================================================
    // ADD ADDRESS — NEGATIVE FLOWS
    // ======================================================
    @Nested
    class AddAddressNegative {

        @Test
        void shouldFail_whenPersonDoesNotExist() {
            AddressCreateDto dto = addressDto(1L);

            assertThatThrownBy(() ->
                    addressService.add(999L, dto)
            )
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Person not found");
        }

        @Test
        void shouldFail_whenCommuneIdIsMissing() {
            Person person = createPerson();

            AddressCreateDto dto = new AddressCreateDto();
            dto.setStreet("Street");
            dto.setCity("City");

            assertThatThrownBy(() ->
                    addressService.add(person.getId(), dto)
            )
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Commune information is required");

            assertThat(addressRepository.count()).isZero();
        }

        @Test
        void shouldFail_whenCommuneDoesNotExist() {
            Person person = createPerson();

            AddressCreateDto dto = addressDto(999L);

            assertThatThrownBy(() ->
                    addressService.add(person.getId(), dto)
            )
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Commune not found");
        }
    }

    // ======================================================
    // GET ADDRESSES
    // ======================================================
    @Nested
    class GetAddresses {

        @Test
        void shouldReturnAllAddressesByPerson() {
            Person person = createPerson();
            Commune commune = createCommune();
            AddressCreateDto address = addressDto(commune.getId());

            addressService.add(person.getId(), address);
            addressService.add(person.getId(), address);

            List<AddressDTO> addresses =
                    addressService.getAllByPerson(person.getId());

            Assertions.assertThat(addresses).hasSize(2);
        }

        @Test
        void shouldReturnEmptyList_whenPersonHasNoAddresses() {
            Person person = createPerson();

            List<AddressDTO> addresses =
                    addressService.getAllByPerson(person.getId());

            Assertions.assertThat(addresses).isEmpty();
        }
    }

    // ======================================================
    // HELPERS
    // ======================================================
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

    private Commune createCommune() {
        return communeRepository.save(
                new Commune(null, "Algiers", "الجزائر", "1601", null)
        );
    }

    private AddressCreateDto addressDto(Long communeId) {
        AddressCreateDto dto = new AddressCreateDto();
        dto.setStreet("Didouche Mourad");
        dto.setCity("Algiers");
        dto.setCommuneId(communeId);
        return dto;
    }
}

