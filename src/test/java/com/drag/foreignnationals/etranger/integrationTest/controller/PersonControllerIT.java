package com.drag.foreignnationals.etranger.integrationTest.controller;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonPatchDTO;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.repository.AddressRepository;
import com.drag.foreignnationals.etranger.repository.CommuneRepository;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser(roles = "USER")
class PersonControllerIT extends AbstractMySqlIT {

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
        // Order matters: delete child tables first
        addressRepository.deleteAll();
        communeRepository.deleteAll();
        personRepository.deleteAll();
        nationalityRepository.deleteAll();
    }



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
        dto.setGender(Person.Gender.MALE);
        dto.setDateOfBirth(LocalDate.now());
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

    /*============================================================
    CREATE — Positive Tests
     ============================================================*/
    @Nested

    class CreatePersonPositive {

        @Test

        void shouldCreatePersonSuccessfully() throws Exception {
            // GIVEN
            Nationality nat = createNationality();
            PersonCreateDTO dto = basePersonDto(nat.getId());

            // WHEN + THEN
            mockMvc.perform(post("/api/persons")
                            //.with(regularUser())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.firstName").value("Karim"))
                    .andExpect(jsonPath("$.nationality.id").value(nat.getId()));
        }

        @Test
        void shouldCreatePersonWithCurrentAddress() throws Exception {
            Nationality nat = createNationality();
            Commune commune = createCommune();

            PersonCreateDTO dto = basePersonDto(nat.getId());
            dto.setCurrentAddress(addressDto(commune.getId()));

            mockMvc.perform(post("/api/persons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.currentAddress.commune.id")
                            .value(commune.getId()));
        }
    }

    /*============================================================
    CREATE — Negative Tests
     ============================================================*/
    @Nested
    class CreatePersonNegative {

        @Test
        void shouldFailWhenNationalityDoesNotExist() throws Exception {
            PersonCreateDTO dto = basePersonDto(999L);

            mockMvc.perform(post("/api/persons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Nationality not found"));
        }

        @Test
        void shouldFailWhenAddressProvidedWithoutCommune() throws Exception {
            Nationality nat = createNationality();
            PersonCreateDTO dto = basePersonDto(nat.getId());
            dto.setCurrentAddress(new AddressCreateDto());

            mockMvc.perform(post("/api/persons")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Commune is required when address is provided"));
        }
    }

    /*============================================================
    scalar validation tests
     ============================================================*/

    /*============================================================
    GET by ID
     ============================================================*/
    @Nested
    class GetPerson {

        @Test
        void shouldGetPersonById() throws Exception {
            Nationality nat = createNationality();
            Person saved = personRepository.save(
                    Person.builder()
                            .firstName("Karim")
                            .lastName("Benzema")
                            .gender(Person.Gender.MALE)
                            .dateOfBirth(LocalDate.now())
                            .nationality(nat)
                            .build()
            );

            mockMvc.perform(get("/api/persons/{id}", saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.lastName").value(saved.getLastName()));
        }

        @Test
        void shouldFailWhenPersonNotFound() throws Exception {
            mockMvc.perform(get("/api/persons/{id}", 999L))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================================================
    // HELPERS
    // ======================================================

    /*============================================================
    UPDATE
    ============================================================ */
    @Nested
    class UpdatePerson {

        @Test
        void shouldUpdatePersonSuccessfully() throws Exception {
            Nationality nat1 = createNationality();
            Nationality nat2 = nationalityRepository.save(
                    new Nationality(null, "French", "فرنسي", null)
            );

            Person person = personRepository.save(
                    Person.builder()
                            .firstName("Karim")
                            .lastName("Benzema")
                            .gender(Person.Gender.MALE)
                            .dateOfBirth(LocalDate.now())
                            .nationality(nat1)
                            .build()
            );

            PersonCreateDTO update = new PersonCreateDTO();
            update.setFirstName("Zinedine");
            update.setLastName("Zidane");
            update.setDateOfBirth(LocalDate.now());
            update.setGender(Person.Gender.MALE);
            update.setNationalityId(nat2.getId());

            mockMvc.perform(put("/api/persons/{id}", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Zinedine"))
                    .andExpect(jsonPath("$.nationality.id").value(nat2.getId()));
        }
    }

    /*============================================================
    PATCH
     ============================================================*/
    @Nested
    class PatchPerson {

        @Test
        void shouldPatchPersonPartially() throws Exception {
            Nationality nat = createNationality();

            Person person = personRepository.save(
                    Person.builder()
                            .firstName("Karim")
                            .lastName("Benzema")
                            .gender(Person.Gender.MALE)
                            .dateOfBirth(LocalDate.now())
                            .nationality(nat)
                            .build()
            );

            PersonPatchDTO patch = new PersonPatchDTO();
            patch.setFirstName(java.util.Optional.of("Riyad"));

            mockMvc.perform(patch("/api/persons/{id}", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Riyad"))
                    .andExpect(jsonPath("$.lastName").value("Benzema"));
        }
    }

    /*============================================================
    DELETE
     ============================================================*/
    @Nested
    class DeletePerson {

        @Test
        @WithMockUser(roles = "ADMIN")
        void shouldDeletePersonSuccessfully() throws Exception {
            Nationality nat = createNationality();

            Person person = personRepository.save(
                    Person.builder()
                            .firstName("Karim")
                            .lastName("Benzema")
                            .gender(Person.Gender.MALE)
                            .dateOfBirth(LocalDate.now())
                            .nationality(nat)
                            .build()
            );

            mockMvc.perform(delete("/api/persons/{id}", person.getId()))
                    .andExpect(status().isNoContent());

            assertThat(personRepository.existsById(person.getId())).isFalse();
        }
    }

    /*---------------------------------------------------------------------
    scalar validation tests
    ------------------------------------------------------------------------ */

    @Nested
    class CreatePersonValidation {
        @Test
        void shouldFailWithMultipleValidationErrors() throws Exception {
            // GIVEN
            PersonCreateDTO dto = new PersonCreateDTO();
            dto.setFirstName(null);          // invalid
            dto.setLastName("   ");          // invalid
            dto.setGender(Person.Gender.MALE);
            dto.setDateOfBirth(LocalDate.now());
            dto.setNationalityId(null);      // invalid

            // WHEN
            ResultActions result = mockMvc.perform(post("/api/persons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // THEN
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))

                    // fields
                    .andExpect(jsonPath("$.fieldErrors[*].field",
                            hasItems("firstName", "lastName", "nationalityId")))
                    .andExpect(jsonPath("$.fieldErrors[*].message",
                            hasItems(
                                    "First name is required",
                                    "Last name is required",
                                    "Nationality is required"
                            )));
        }

        @Test
        void shouldFailWhenMultipleFieldsAreMissing() throws Exception {
            String json = """
        {
          "gender": "MALE",
          "dateOfBirth": "2026-01-25"
        }
        """;

            ResultActions result = mockMvc.perform(post("/api/persons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json));

            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors[*].field",
                            hasItems("firstName", "lastName", "nationalityId")))
                    .andExpect(jsonPath("$.fieldErrors[*].message",
                            hasItems(
                                    "First name is required",
                                    "Last name is required",
                                    "Nationality is required"
                            )));
        }

    }

    /*============================================================
    SECURITY — New Negative Tests
     ============================================================*/
    @Nested
    class SecurityTests {

        @Test
        void shouldFailWhenUnauthenticated() throws Exception {
            // No .with(user()) here
            mockMvc.perform(get("/api/persons").with(anonymous()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailWhenUserIsMissingRequiredRole() throws Exception {
            // Assume DELETE requires ADMIN, but we provide USER
            mockMvc.perform(delete("/api/persons/1")
                            )
                    .andExpect(status().isForbidden()); // 403
        }
    }



}