package com.drag.foreignnationals.etranger.integrationTest.controller;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AddressControllerIT extends AbstractMySqlIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    NationalityRepository nationalityRepository;

    @Autowired
    CommuneRepository communeRepository;

    @Autowired
    AddressRepository addressRepository;

    @BeforeEach
    void cleanup() {
        addressRepository.deleteAll();
        communeRepository.deleteAll();
        personRepository.deleteAll();
        nationalityRepository.deleteAll();
    }

    /* ============================================================
       Helpers
     ============================================================ */

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

    private Person createPerson() {
        Nationality nat = createNationality();
        return personRepository.save(
                Person.builder()
                        .firstName("Karim")
                        .lastName("Benzema")
                        .gender(Person.Gender.MALE)
                        .dateOfBirth(LocalDate.now())
                        .nationality(nat)
                        .build()
        );
    }

    private AddressCreateDto addressDto(Long communeId) {
        AddressCreateDto dto = new AddressCreateDto();
        dto.setStreet("Didouche Mourad");
        dto.setCity("Algiers");
        dto.setCommuneId(communeId);
        return dto;
    }

    /* ============================================================
       CREATE — Positive
     ============================================================ */
    @Nested
    class CreateAddressPositive {

        @Test
        void shouldAddAddressSuccessfully() throws Exception {
            Person person = createPerson();
            Commune commune = createCommune();

            AddressCreateDto dto = addressDto(commune.getId());

            mockMvc.perform(post("/api/persons/{idPerson}/addresses", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.current").value(true))
                    .andExpect(jsonPath("$.commune.id").value(commune.getId()));
        }

        @Test
        void shouldDeactivatePreviousCurrentAddress() throws Exception {
            Person person = createPerson();
            Commune commune1 = createCommune();

            Address oldAddress = Address.builder()
                    .street("Old Street")
                    .city("Algiers")
                    .current(true)
                    .commune(commune1)
                    .person(person)
                    .build();

            addressRepository.save(oldAddress);

            Commune commune2 = communeRepository.save(
                    new Commune(null, "Oran", "وهران", "3100", null)
            );

            AddressCreateDto dto = addressDto(commune2.getId());

            mockMvc.perform(post("/api/persons/{idPerson}/addresses", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());

            Address previous = addressRepository.findById(oldAddress.getId()).orElseThrow();
            assertThat(previous.isCurrent()).isFalse();
        }
    }

    /* ============================================================
       CREATE — Negative
     ============================================================ */
    @Nested
    class CreateAddressNegative {

        @Test
        void shouldFailWhenPersonNotFound() throws Exception {
            Commune commune = createCommune();
            AddressCreateDto dto = addressDto(commune.getId());

            mockMvc.perform(post("/api/persons/{idPerson}/addresses", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Person not found with ID 999"));
        }

        @Test
        void shouldFailWhenCommuneIsMissing() throws Exception {
            Person person = createPerson();
            AddressCreateDto dto = new AddressCreateDto();

            mockMvc.perform(post("/api/persons/{idPerson}/addresses", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Commune information is required to add address."));
        }

        @Test
        void shouldFailWhenCommuneNotFound() throws Exception {
            Person person = createPerson();
            AddressCreateDto dto = addressDto(999L);

            mockMvc.perform(post("/api/persons/{idPerson}/addresses", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Commune not found with ID 999"));
        }
    }

    /* ============================================================
       GET ALL
     ============================================================ */
    @Nested
    class GetAddresses {

        @Test
        void shouldGetAllAddressesForPerson() throws Exception {
            Person person = createPerson();
            Commune commune = createCommune();

            addressRepository.save(
                    Address.builder()
                            .street("Street 1")
                            .city("Algiers")
                            .current(true)
                            .commune(commune)
                            .person(person)
                            .build()
            );

            mockMvc.perform(get("/api/persons/{idPerson}/addresses", person.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].street").value("Street 1"));
        }

        @Test
        void shouldReturnEmptyListWhenNoAddresses() throws Exception {
            Person person = createPerson();

            mockMvc.perform(get("/api/persons/{idPerson}/addresses", person.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
