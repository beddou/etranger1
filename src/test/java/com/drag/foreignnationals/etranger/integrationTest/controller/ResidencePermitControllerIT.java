package com.drag.foreignnationals.etranger.integrationTest.controller;

import com.drag.foreignnationals.etranger.AbstractMySqlIT;
import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.enums.ResidenceType;
import com.drag.foreignnationals.etranger.repository.NationalityRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser(roles = "USER")
class ResidencePermitControllerIT extends AbstractMySqlIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    ResidencePermitRepository permitRepository;

    @Autowired
    NationalityRepository nationalityRepository;

    @BeforeEach
    void cleanup() {
        permitRepository.deleteAll();
        personRepository.deleteAll();
    }

    // ======================================================
    // HELPERS
    // ======================================================

    private Nationality createNationality() {
        return nationalityRepository.save(
                new Nationality(null, "Algerian", "جزائري", null)
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

    private ResidencePermitDTO basePermitDto(Long personId) {
        ResidencePermitDTO dto = new ResidencePermitDTO();
        dto.setPersonId(personId);
        dto.setDateOfIssue(LocalDate.now());
        dto.setDurationInMonths(12);
        dto.setType(ResidenceType.Commerçant);
        return dto;
    }

    // ======================================================
    // CREATE — POSITIVE
    // ======================================================

    @Nested
    class CreatePermitPositive {

        @Test
        void shouldCreatePermitSuccessfully() throws Exception {
            Person person = createPerson();
            ResidencePermitDTO dto = basePermitDto(person.getId());

            mockMvc.perform(post("/api/persons/{idPerson}/residence-permits", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        void shouldDeactivatePreviousActivePermit() throws Exception {
            Person person = createPerson();

            ResidencePermit oldPermit = permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .dateOfIssue(LocalDate.now())
                            .type(ResidenceType.Etudiant)
                            .active(true)
                            .build()
            );

            ResidencePermitDTO dto = basePermitDto(person.getId());

            mockMvc.perform(post("/api/persons/{idPerson}/residence-permits", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());

            ResidencePermit refreshed = permitRepository.findById(oldPermit.getId()).orElseThrow();
            assertThat(refreshed.isActive()).isFalse();
        }
    }

    // ======================================================
    // CREATE — VALIDATION
    // ======================================================

    @Nested
    class CreatePermitValidation {


        @Test
        void shouldFailWithMultipleValidationErrors() throws Exception {
            Person person = createPerson();
            ResidencePermitDTO dto = new ResidencePermitDTO();
            dto.setType(null);           // invalid
            dto.setDateOfIssue(null);    // invalid
            dto.setDurationInMonths(null); // invalid

            mockMvc.perform(post("/api/persons/{idPerson}/residence-permits", person.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.fieldErrors[*].field",
                            hasItems("type", "dateOfIssue", "durationInMonths")));

        }

        @Test
        void shouldFailWhenPersonDoesNotExist() throws Exception {
            ResidencePermitDTO dto = basePermitDto(999L);

            mockMvc.perform(post("/api/persons/{idPerson}/residence-permits", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value("Person not found with ID 999"));
        }
    }

    // ======================================================
    // GET
    // ======================================================

    @Nested
    class GetPermit {

        @Test
        void shouldGetPermitById() throws Exception {
            Person person = createPerson();

            ResidencePermit permit = permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .active(true)
                            .type(ResidenceType.Commerçant)
                            .dateOfIssue(LocalDate.now())
                            .durationInMonths(12)
                            .build()
            );

            mockMvc.perform(get("/api/persons/{personId}/residence-permits/{permitId}",
                            person.getId(), permit.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(permit.getId()));
        }

        @Test
        void shouldFailWhenPermitNotFound() throws Exception {
            mockMvc.perform(get("/api/persons/{personId}/residence-permits/{permitId}",
                            999L, 999L))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldGetPermitsByPerson() throws Exception {
            Person person = createPerson();

            permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .active(true)
                            .dateOfIssue(LocalDate.now())
                            .durationInMonths(12)
                            .type(ResidenceType.Etudiant)
                            .build()
            );
            permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .active(false)
                            .dateOfIssue(LocalDate.now())
                            .durationInMonths(12)
                            .type(ResidenceType.Etudiant)
                            .build()
            );

            mockMvc.perform(get("/api/persons/{personId}/residence-permits", person.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }
    }

    // ======================================================
    // UPDATE
    // ======================================================

    @Nested
    class UpdatePermit {

        @Test
        void shouldUpdatePermitSuccessfully() throws Exception {
            Person person = createPerson();

            ResidencePermit permit = permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .active(true)
                            .dateOfIssue(LocalDate.now())
                            .durationInMonths(12)
                            .type(ResidenceType.Etudiant)
                            .build()
            );

            ResidencePermitDTO update = new ResidencePermitDTO();
            update.setDurationInMonths(24);
            update.setDateOfIssue(LocalDate.now());
            update.setType(ResidenceType.Commerçant);

            mockMvc.perform(put("/api/persons/{personId}/residence-permits/{permitId}",
                            person.getId(), permit.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.durationInMonths").value(24));
        }

        @Test
        void shouldFailWhenUpdatingUnknownPermit() throws Exception {
            ResidencePermitDTO dto = new ResidencePermitDTO();

            mockMvc.perform(put("/api/persons/{personId}/residence-permits/{permitId}",
                            999L, 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }
    }

    // ======================================================
    // DELETE
    // ======================================================

    @Nested
    class DeletePermit {

        @Test
        void shouldDeleteNonActivePermit() throws Exception {
            Person person = createPerson();

            ResidencePermit permit = permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .active(false)
                            .dateOfIssue(LocalDate.now())
                            .durationInMonths(12)
                            .type(ResidenceType.Commerçant)
                            .build()
            );

            mockMvc.perform(delete("/api/persons/{personId}/residence-permits/{permitId}",
                            person.getId(), permit.getId()))
                    .andExpect(status().isNoContent());

            assertThat(permitRepository.existsById(permit.getId())).isFalse();
        }

        @Test
        void shouldFailWhenDeletingActivePermit() throws Exception {
            Person person = createPerson();

            ResidencePermit permit = permitRepository.save(
                    ResidencePermit.builder()
                            .person(person)
                            .active(true)
                            .dateOfIssue(LocalDate.now())
                            .durationInMonths(12)
                            .type(ResidenceType.Commerçant)
                            .build()
            );

            mockMvc.perform(delete("/api/persons/{personId}/residence-permits/{permitId}",
                            person.getId(), permit.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value("Cannot delete active residence permit"));
        }

        @Test
        void shouldFailWhenPermitNotFound() throws Exception {
            mockMvc.perform(delete("/api/persons/{personId}/residence-permits/{permitId}",
                            999L, 999L))
                    .andExpect(status().isNotFound());
        }
    }
}
