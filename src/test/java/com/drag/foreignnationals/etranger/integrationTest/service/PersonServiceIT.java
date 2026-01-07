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
import com.drag.foreignnationals.etranger.repository.SituationRepository;
import com.drag.foreignnationals.etranger.service.PersonService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Autowired
    private SituationRepository situationRepository;


/*   TEST 1 — Create person (basic flow)
Business rule tested :
✔ Nationality is mandatory
✔ Person is persisted    */
@Test
void shouldCreatePersonSuccessfully() {
    // GIVEN
    Nationality nat = createNationality();
    PersonCreateDTO dto = basePersonDto(nat.getId());

    // WHEN
    PersonDetailDTO result = personService.create(dto);

    // THEN
    assertThat(result.getId()).isNotNull();
    assertThat(result.getFirstName()).isEqualTo("Karim");
    assertThat(result.getNationality().getId()).isEqualTo(nat.getId());

    assertThat(personRepository.existsById(result.getId())).isTrue();
}

/*   TEST 2 — Create person WITH current address
Business rule tested

✔ Address is created
✔ Address is current
✔ Commune is mandatory  */
@Test
void shouldCreatePersonWithCurrentAddress() {
    // GIVEN
    Nationality nat = createNationality();
    Commune commune = createCommune();

    PersonCreateDTO dto = basePersonDto(nat.getId());
    dto.setCurrentAddress(addressDto(commune.getId()));

    // WHEN
    PersonDetailDTO result = personService.create(dto);

    // THEN
    Person person = personRepository.findById(result.getId()).orElseThrow();

    Address current = person.getCurrentAddress();
    assertThat(current).isNotNull();
    assertThat(current.isCurrent()).isTrue();
    assertThat(current.getCommune().getId()).isEqualTo(commune.getId());
}

/*   TEST 3 — Create person with address but NO commune → error
Business rule tested

✔ Commune is mandatory when address exists   */
@Test
void shouldFailWhenAddressProvidedWithoutCommune() {
    // GIVEN
    Nationality nat = createNationality();

    PersonCreateDTO dto = basePersonDto(nat.getId());
    AddressCreateDto address = new AddressCreateDto();
    address.setStreet("Street");
    address.setCity("City");
    dto.setCurrentAddress(address);

    // THEN
    assertThatThrownBy(() -> personService.create(dto))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Commune is required");
}

/*   TEST 4 — Update person (full update)
Business rule tested

✔ Scalar fields updated
✔ Nationality updated   */
@Test
void shouldUpdatePerson() {
    // GIVEN
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

    // WHEN
    PersonDetailDTO updated =
            personService.update(created.getId(), updateDto);

    // THEN
    assertThat(updated.getFirstName()).isEqualTo("Zinedine");
    assertThat(updated.getNationality().getId()).isEqualTo(nat2.getId());
}

/*  TEST 5 — Patch person (partial update)
Business rule tested

✔ Only provided fields are updated */
@Test
void shouldPatchPersonPartially() {
    // GIVEN
    Nationality nat = createNationality();
    PersonDetailDTO created =
            personService.create(basePersonDto(nat.getId()));

    PersonPatchDTO patch = new PersonPatchDTO();
    patch.setFirstName(Optional.of("Riyad"));

    // WHEN
    PersonDetailDTO patched =
            personService.patch(created.getId(), patch);

    // THEN
    assertThat(patched.getFirstName()).isEqualTo("Riyad");
    assertThat(patched.getLastName()).isEqualTo("Benzema"); // unchanged
}

/*   TEST 6 — Update current address (replace)
Business rule tested

✔ Only one current address
✔ Address is updated, not duplicated  */
@Test
void shouldUpdateCurrentAddress() {
    // GIVEN
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

    // WHEN
    personService.update(created.getId(), update);

    // THEN
    Person person =
            personRepository.findById(created.getId()).orElseThrow();

    Assertions.assertThat(person.getAddresses()).hasSize(1);
    assertThat(person.getCurrentAddress().getCommune().getId())
            .isEqualTo(commune2.getId());
}

/*  TEST 7 — Get by id */
@Test
void shouldGetPersonById() {
    Nationality nat = createNationality();
    PersonDetailDTO created =
            personService.create(basePersonDto(nat.getId()));

    PersonDetailDTO found =
            personService.getById(created.getId());

    assertThat(found.getId()).isEqualTo(created.getId());
}


    //*****************************
    //**** Helper data creators****
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
