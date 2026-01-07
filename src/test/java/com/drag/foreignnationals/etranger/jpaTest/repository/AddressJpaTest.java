package com.drag.foreignnationals.etranger.jpaTest.repository;


import com.drag.foreignnationals.etranger.entity.*;
import com.drag.foreignnationals.etranger.repository.AddressRepository;
import com.drag.foreignnationals.etranger.repository.CommuneRepository;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AddressJpaTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private NationalityRepository nationalityRepository;

    @Autowired
    private CommuneRepository communeRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EntityManager em;

    @Test
    void shouldPersistAddressWithPersonAndCommune() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "Algerian","جزائري",null));

        Person person = Person.builder()
                .firstName("Ali")
                .lastName("Benkhaled")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .nationality(nat)
                .build();

        Commune commune =
                communeRepository.save(new Commune(null,"Algiers","الجزائر","1601",null));

        Address address = Address.builder()
                .street("Rue 1")
                .city("Algiers")
                .current(true)
                .person(person)
                .commune(commune)
                .build();

        person.setAddresses(List.of(address));

        personRepository.save(person);

        em.flush();
        em.clear();

        Person found = personRepository.findById(person.getId()).orElseThrow();

        assertThat(found.getAddresses()).hasSize(1);
        assertThat(found.getAddresses().get(0).getCommune().getCode())
                .isEqualTo("1601");
    }
    @Test
    void shouldReturnCurrentAddress() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "Italian","إيطالي",null));

        Person person = Person.builder()
                .firstName("Samir")
                .lastName("Haddad")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        Commune commune =
                communeRepository.save(new Commune(null,"Oran","وهران","3100",null));

        Address oldAddress = Address.builder()
                .street("Old Street")
                .city("Oran")
                .current(false)
                .person(person)
                .commune(commune)
                .build();

        Address currentAddress = Address.builder()
                .street("New Street")
                .city("Algiers")
                .current(true)
                .person(person)
                .commune(commune)
                .build();

        person.setAddresses(List.of(oldAddress, currentAddress));

        personRepository.save(person);


        em.flush();
        em.clear();

        Person found = personRepository.findById(person.getId()).orElseThrow();

        Address current = found.getCurrentAddress();

        assertThat(current).isNotNull();
        assertThat(current.getCity()).isEqualTo("Algiers");
    }

    @Test
    void shouldRemoveAddressWhenRemovedFromPerson() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "French","فرنسي",null));

        Person person = Person.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1985, 5, 5))
                .nationality(nat)
                .build();

        Commune commune =
                communeRepository.save(new Commune(null,"Paris","باريس","75000",null));

        Address address = Address.builder()
                .street("Rue 1")
                .city("Paris")
                .current(true)
                .person(person)
                .commune(commune)
                .build();

        person.setAddresses(List.of(address));

        person = personRepository.save(person);

        em.flush();
        em.clear();

        // Remove address
        Person managed = personRepository.findById(person.getId()).orElseThrow();
        managed.getAddresses().clear();

        em.flush();
        em.clear();

        assertThat(addressRepository.findByPersonId(person.getId())).isEmpty();
    }

    @Test
    void shouldRemoveAddressWhenRemovePerson() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "French","فرنسي",null));

        Person person = Person.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1985, 5, 5))
                .nationality(nat)
                .build();

        Commune commune =
                communeRepository.save(new Commune(null,"Paris","باريس","75000",null));

        Address address = Address.builder()
                .street("Rue 1")
                .city("Paris")
                .current(true)
                .person(person)
                .commune(commune)
                .build();

        person.setAddresses(List.of(address));

        person = personRepository.save(person);

        em.flush();
        em.clear();

        assertThat(addressRepository.findByPersonId(person.getId())).isNotEmpty();

        Long idPerson = person.getId();

        // Remove person
        personRepository.delete(person);

        em.flush();
        em.clear();

        assertThat(addressRepository.findByPersonId(idPerson)).isEmpty();
    }

    @Test
    void deletingAddressShouldNotDeletePerson(){
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "French","فرنسي",null));

        Person person = Person.builder()
                .firstName("Jean")
                .lastName("Dupont")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1985, 5, 5))
                .nationality(nat)
                .build();

        Commune commune =
                communeRepository.save(new Commune(null,"Paris","باريس","75000",null));

        Address address = Address.builder()
                .street("Rue 1")
                .city("Paris")
                .current(true)
                .person(person)
                .commune(commune)
                .build();

        person.setAddresses(List.of(address));

        person = personRepository.save(person);

        em.flush();
        em.clear();



        // Remove address
        Person managed = personRepository.findById(person.getId()).orElseThrow();
        managed.getAddresses().clear();

        em.flush();
        em.clear();

        assertThat(personRepository.findById(person.getId())).isPresent();

    }


    @Test
    void shouldFindAddressesByPersonId() {
        Nationality nat =
                nationalityRepository.save(new Nationality(null, "Spanish","اسباني",null));

        Person person = Person.builder()
                .firstName("Carlos")
                .lastName("Mendez")
                .gender(Person.Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality(nat)
                .build();

        Commune commune =
                communeRepository.save(new Commune(null,"Madrid","مدريد","28001",null));

        Address a1 = Address.builder()
                .street("Street 1")
                .city("Madrid")
                .current(false)
                .person(person)
                .commune(commune)
                .build();

        Address a2 = Address.builder()
                .street("Street 2")
                .city("Barcelona")
                .current(true)
                .person(person)
                .commune(commune)
                .build();

        person.setAddresses(List.of(a1, a2));

        personRepository.save(person);

        em.flush();
        em.clear();

        assertThat(addressRepository.findByPersonId(person.getId()))
                .hasSize(2);
    }
}
