package com.drag.foreignnationals.etranger.repository;

import com.drag.foreignnationals.etranger.entity.*;
import com.drag.foreignnationals.etranger.enums.SituationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)

public class PersonJpaTest {
    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private NationalityRepository nationalityRepository;

    @Autowired
    private  CommuneRepository communeRepository;

    @Autowired
    private SituationRepository situationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager em;

    // -------------------------------------------------------
    // Test 1: Save basic Person + Nationality
    // -------------------------------------------------------
    @Test
    void shouldSavePersonWithNationality() {
        Nationality nat = new Nationality(null, "Algerian","جزائري",null);

        nat = nationalityRepository.save(nat);

        Person p = Person.builder()
                .firstName("Karim")
                .lastName("Benzema")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        personRepository.save(p);

        em.flush();
        em.clear();

        // ✔ reload from the database
        Person found = personRepository.findById(p.getId()).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getNationality().getName()).isEqualTo("Algerian");
    }

    // -------------------------------------------------------
    // Test 2: Save person with addresses (cascade)
    // -------------------------------------------------------
    @Test
    void shouldSavePersonWithAddresses() {
        Nationality nat = nationalityRepository.save(new Nationality(null, "French","فرنسا",null));

        Person p = Person.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1985, 5, 5))
                .nationality(nat)
                .build();

        Commune commune = new Commune(null,"Ain temouchent","عين تموضنت","4601",null);
        commune = communeRepository.save(commune);
        Address addr1 = Address.builder()
                .street("Rue 1")
                .city("Paris")
                .current(true)
                .person(p)
                .commune(commune)
                .build();

        Address addr2 = Address.builder()
                .street("Rue 2")
                .city("Lyon")
                .current(false)
                .person(p)
                .commune(commune)
                .build();

        p.setAddresses(List.of(addr1, addr2));

        Person saved = personRepository.save(p);

        em.flush();
        em.clear();

        Person found = personRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getAddresses()).hasSize(2);
        assertThat(found.getCurrentAddress().getCity()).isEqualTo("Paris");
        assertThat(found.getCurrentAddress().getCommune().getCode()).isEqualTo("4601");
    }

    // -------------------------------------------------------
    // Test 3: Save person with Situation (OneToOne)
    // -------------------------------------------------------
    @Test
    void shouldSavePersonWithSituation() {
        Nationality nat = nationalityRepository.save(new Nationality(null, "Moroccan","مغربي",null));

        Situation situation = new Situation();
        situation.setType(SituationType.NATURALIZATION);
        situation.setDate(LocalDate.of(2025,12,11));

        Person p = Person.builder()
                .firstName("Ali")
                .lastName("Fassi")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .nationality(nat)
                .situation(situation)
                .build();

        situation.setPerson(p); // VERY IMPORTANT for mappedBy

        Person saved = personRepository.save(p);

        em.flush();
        em.clear();

        Person found = personRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getSituation()).isNotNull();
        assertThat(found.getSituation().getType()).isEqualTo(SituationType.NATURALIZATION);
    }

    // -------------------------------------------------------
    // Test 4: Custom search query
    // -------------------------------------------------------
    @Test
    void shouldSearchByKeywordAcrossNameNationalitySituation() {

        Nationality nat = nationalityRepository.save(new Nationality(null, "Tunisian","تونسي",null));

        Situation sit = new Situation();
        sit.setType(SituationType.CHANGE_ADDRESS);
        sit.setDate(LocalDate.of(2025,12,11));
        situationRepository.save(sit);

        Person p = Person.builder()
                .firstName("Mohamed")
                .lastName("Ali")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 3, 15))
                .nationality(nat)
                .situation(sit)
                .build();

        sit.setPerson(p);

        personRepository.save(p);

        Pageable pageable = PageRequest.of(0, 5);

        var result = personRepository.search("moh", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLastName()).isEqualTo("Ali");
    }

    // -------------------------------------------------------
    // Test 5: Cascade delete → addresses and situation removed?
    // -------------------------------------------------------
    @Test
    void shouldCascadeDeletePerson() {
        Nationality nat = nationalityRepository.save(new Nationality(null, "Italian","إيطالي",null));

        Person p = Person.builder()
                .firstName("Luca")
                .lastName("Rossi")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .nationality(nat)
                .build();
        Commune commune = new Commune(null,"Ain temouchent","عين تموضنت","4601",null);
        commune = communeRepository.save(commune);

        Address address = Address.builder()
                .street("Via Roma")
                .city("Rome")
                .current(true)
                .person(p)
                .commune(commune)
                .build();

        p.setAddresses(List.of(address));

        personRepository.save(p);
        em.flush();
        em.clear();

        // BEFORE DELETE → address must exist
        assertThat(addressRepository.findByPersonId(p.getId())).hasSize(1);

        // DELETE PERSON
        personRepository.deleteById(p.getId());
        em.flush();
        em.clear();

        // Since orphanRemoval = true, addresses should be deleted
        assertThat(personRepository.findById(p.getId())).isEmpty();
        assertThat(addressRepository.findByPersonId(p.getId())).isEmpty();
    }

    // -------------------------------------------------------
    // Test 6: Test for getCurrentAddress()
    // -------------------------------------------------------
    @Test
    void shouldReturnCurrentAddress() {
        Nationality nat = nationalityRepository.save(new Nationality(null, "Italian","إيطالي",null));

        Person p = Person.builder()
                .firstName("Samir")
                .lastName("Haddad")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        Commune commune = new Commune(null,"Ain temouchent","عين تموضنت","4601",null);
        commune = communeRepository.save(commune);

        Address address1 = Address.builder()
                .street("Old Street")
                .city("Oran")
                .current(false)
                .person(p)
                .commune(commune)
                .build();

        Address address2 = Address.builder()
                .street("New Street")
                .city("Algiers")
                .current(true)
                .person(p)
                .commune(commune)
                .build();

        p.setAddresses(List.of(address1, address2));

        Person saved = personRepository.save(p);

        em.flush();
        em.clear();

        Person found = personRepository.findById(saved.getId()).orElseThrow();

        Address current = found.getCurrentAddress();

        assertThat(current).isNotNull();
        assertThat(current.getStreet()).isEqualTo("New Street");
        assertThat(current.getCity()).isEqualTo("Algiers");
    }

    // -------------------------------------------------------
    // Test 7: Test for getCurrentAddress()
    //test address switching
    //
    //This verifies that only the new address with current = true is returned.
    // -------------------------------------------------------
    @Test
    void shouldReturnUpdatedCurrentAddressAfterSwitch() {
        Nationality nat = nationalityRepository.save(new Nationality(null, "Italian","إيطالي",null));

        Person p = Person.builder()
                .firstName("Nadia")
                .lastName("Belkacem")
                .gender(Person.Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .nationality(nat)
                .build();

        Commune commune = new Commune(null,"Ain temouchent","عين تموضنت","4601",null);
        commune = communeRepository.save(commune);

        Address address1 = Address.builder()
                .street("Street A")
                .city("Paris")
                .current(true)
                .person(p)
                .commune(commune)
                .build();

        Address address2 = Address.builder()
                .street("Street B")
                .city("Lyon")
                .current(false)
                .person(p)
                .commune(commune)
                .build();

        List<Address> addresses = new ArrayList<>();
        addresses.add(address1);
        addresses.add(address2);

        p.setAddresses(addresses);

        Person saved = personRepository.save(p);

        // Now switch
        saved.getAddresses().forEach(a -> a.setCurrent(false));
        saved.getAddresses().get(1).setCurrent(true); // Lyon becomes current

        personRepository.save(saved);

        em.flush();
        em.clear();

        Person found = personRepository.findById(saved.getId()).orElseThrow();

        Address current = found.getCurrentAddress();

        assertThat(current).isNotNull();
        assertThat(current.getCity()).isEqualTo("Lyon");
    }

    // -------------------------------------------------------
    // Test 8: Test that search() is case-insensitive
    // -------------------------------------------------------
    @Test
    void searchShouldBeCaseInsensitive() {
        Nationality nat = nationalityRepository.save(new Nationality(null, "Spanish","اسباني",null));

        Person p = Person.builder()
                .firstName("Carlos")
                .lastName("Mendez")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        personRepository.save(p);

        var page = PageRequest.of(0, 10);

        assertThat(personRepository.search("CAR", page).getContent())
                .hasSize(1);
    }



}

