package com.drag.foreignnationals.etranger.service.residencePermit;



import com.drag.foreignnationals.etranger.dto.ResidencePermitDTO;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.ResidencePermitMapper;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.repository.ResidencePermitRepository;
import com.drag.foreignnationals.etranger.service.impl.ResidencePermitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

public class ResidencePermitTest  {

    @InjectMocks
    private ResidencePermitServiceImpl service;

    @Mock
    private ResidencePermitRepository permitRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private ResidencePermitMapper permitMapper;

    private ResidencePermit permit;
    private ResidencePermitDTO permitDTO;
    private Person person;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        person = new Person();
        person.setId(1L);

        permit = new ResidencePermit();
        permit.setId(1L);
        permit.setPerson(person);
        permit.setDateOfIssue(LocalDate.of(2025, 1, 1));
        permit.setDurationInMonths(12);

        permitDTO = new ResidencePermitDTO();
        permitDTO.setId(1L);
        permitDTO.setPersonId(1L);
        permitDTO.setDateOfIssue(LocalDate.of(2025, 1, 1));
        permitDTO.setDurationInMonths(12);
    }

    // ==========================================================
    // CREATE
    // ==========================================================
    @Nested
    class CreateTests {

        @Test
        void shouldCreateResidencePermitSuccessfully() {
            when(permitMapper.toEntity(permitDTO)).thenReturn(permit);
            when(personRepository.findById(1L)).thenReturn(Optional.of(person));
            when(permitRepository.save(permit)).thenReturn(permit);
            when(permitMapper.toDTO(permit)).thenReturn(permitDTO);

            ResidencePermitDTO result = service.create(permitDTO);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertSame(person, permit.getPerson());

            verify(personRepository).findById(1L);
            verify(permitRepository).save(permit);
            verify(permitMapper).toDTO(permit);
        }

        @Test
        void shouldThrowValidationErrorWhenPersonIdIsNull() {
            permitDTO.setPersonId(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.create(permitDTO));

            assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Person information is required"));
            verifyNoInteractions(personRepository, permitRepository, permitMapper);
        }

        @Test
        void shouldThrowEntityNotFoundWhenPersonDoesNotExist() {
            when(personRepository.findById(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.create(permitDTO));

            assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Person not found"));
            verify(personRepository).findById(1L);
            verifyNoInteractions(permitRepository, permitMapper);
        }
    }

    // ==========================================================
    // UPDATE
    // ==========================================================
    @Nested
    class UpdateTests {

        @Test
        void shouldUpdateResidencePermitSuccessfully() {
            ResidencePermitDTO updateDTO = new ResidencePermitDTO();
            updateDTO.setDateOfIssue(LocalDate.of(2025, 2, 1));
            updateDTO.setDurationInMonths(24);

            when(permitRepository.findById(1L)).thenReturn(Optional.of(permit));
            when(permitRepository.save(permit)).thenReturn(permit);
            when(permitMapper.toDTO(permit)).thenReturn(updateDTO);

            ResidencePermitDTO result = service.update(1L, updateDTO);

            assertEquals(updateDTO, result);
            assertEquals(LocalDate.of(2025, 2, 1), permit.getDateOfIssue());
            assertEquals(24, permit.getDurationInMonths());

            verify(permitRepository, times(2)).save(permit);
            verify(permitRepository).findById(1L);
            verify(permitMapper).toDTO(permit);
        }

        @Test
        void shouldThrowEntityNotFoundWhenPermitDoesNotExist() {
            when(permitRepository.findById(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.update(1L, permitDTO));

            assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
            verify(permitRepository).findById(1L);
            verifyNoMoreInteractions(permitRepository, permitMapper);
        }
    }

    // ==========================================================
    // DELETE
    // ==========================================================
    @Nested
    class DeleteTests {

        @Test
        void shouldDeleteResidencePermitSuccessfully() {
            when(permitRepository.findById(1L)).thenReturn(Optional.of(permit));

            service.delete(1L);

            verify(permitRepository).delete(permit);
        }

        @Test
        void shouldThrowEntityNotFoundWhenDeletingNonExistingPermit() {
            when(permitRepository.findById(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.delete(1L));

            assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
            verify(permitRepository).findById(1L);
            verify(permitRepository, never()).delete(any());
        }
    }

    // ==========================================================
    // GET BY PERSON ID
    // ==========================================================
    @Nested
    class GetByPersonIdTests {

        @Test
        void shouldReturnAllPermitsForPerson() {
            List<ResidencePermit> permits = List.of(permit);

            when(permitRepository.findByPersonId(1L)).thenReturn(permits);
            when(permitMapper.toDTO(permit)).thenReturn(permitDTO);

            List<ResidencePermitDTO> result = service.getByPersonId(1L);

            assertEquals(1, result.size());
            assertEquals(permitDTO, result.get(0));

            verify(permitRepository).findByPersonId(1L);
            verify(permitMapper).toDTO(permit);
        }
    }

    // ==========================================================
    // GET BY ID
    // ==========================================================
    @Nested
    class GetByIdTests {

        @Test
        void shouldReturnPermitById() {
            when(permitRepository.findById(1L)).thenReturn(Optional.of(permit));
            when(permitMapper.toDTO(permit)).thenReturn(permitDTO);

            ResidencePermitDTO result = service.getById(1L);

            assertEquals(permitDTO, result);

            verify(permitRepository).findById(1L);
            verify(permitMapper).toDTO(permit);
        }

        @Test
        void shouldThrowEntityNotFoundWhenPermitDoesNotExist() {
            when(permitRepository.findById(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.getById(1L));

            assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
            verify(permitRepository).findById(1L);
            verifyNoInteractions(permitMapper);
        }
    }
}

