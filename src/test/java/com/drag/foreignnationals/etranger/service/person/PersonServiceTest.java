package com.drag.foreignnationals.etranger.service.person;




import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.dto.PersonPatchDTO;
import com.drag.foreignnationals.etranger.entity.Nationality;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.AddressMapper;
import com.drag.foreignnationals.etranger.mapper.PersonMapper;
import com.drag.foreignnationals.etranger.repository.*;
import com.drag.foreignnationals.etranger.service.PersonService;
import com.drag.foreignnationals.etranger.service.impl.PersonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)


class PersonServiceTest {

    @InjectMocks
    private PersonServiceImpl personService;

    @Mock
    private PersonRepository personRepository;
    @Mock
    private NationalityRepository nationalityRepository;
    @Mock
    private SituationRepository situationRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private CommuneRepository communeRepository;

    // Mappers
    @Mock
    private PersonMapper personMapper;
    @Mock
    private PersonMapper personDetailMapper;  // you inject two!
    @Mock
    private AddressMapper addressMapper;

    private Person person;
    private PersonDetailDTO personDetailDTO;
    private PersonDTO personDTO;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        person = new Person();
        person.setId(1L);
        person.setFirstName("John");

        personDetailDTO = new PersonDetailDTO();
        personDetailDTO.setId(1L);
        personDetailDTO.setFirstName("John");

        personDTO = new PersonDTO();
        personDTO.setId(1L);
        personDTO.setFirstName("John");
    }

    // ----------------------------------------------------------
    // GET BY ID
    // ----------------------------------------------------------
    @Test
    void getById_shouldReturnPersonDetailDto() {
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personMapper.toPersonDetailDto(person)).thenReturn(personDetailDTO);

        PersonDetailDTO result = personService.getById(1L);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(personRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> personService.getById(1L)
        );

        assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
    }

    // ----------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------
    @Test
    void create_shouldCreatePerson() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setNationalityId(10L);

        Nationality nat = new Nationality();
        nat.setId(10L);

        when(personMapper.toEntity(dto)).thenReturn(person);
        when(nationalityRepository.findById(10L)).thenReturn(Optional.of(nat));
        when(personRepository.save(person)).thenReturn(person);
        when(personMapper.toPersonDetailDto(person)).thenReturn(personDetailDTO);

        PersonDetailDTO result = personService.create(dto);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void create_shouldThrow_whenNationalityNotFound() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setNationalityId(99L);

        when(personMapper.toEntity(dto)).thenReturn(person);
        when(nationalityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> personService.create(dto));
    }

    // ----------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------
    @Test
    void update_shouldUpdatePerson() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setNationalityId(5L);

        Nationality nat = new Nationality();
        nat.setId(5L);

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(nationalityRepository.findById(5L)).thenReturn(Optional.of(nat));
        when(personRepository.save(person)).thenReturn(person);
        when(personDetailMapper.toPersonDetailDto(person)).thenReturn(personDetailDTO);

        PersonDetailDTO result = personService.update(1L, dto);

        assertNotNull(result);
    }

    @Test
    void update_shouldThrow_whenPersonNotFound() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> personService.update(1L, new PersonCreateDTO()));
    }

    // ----------------------------------------------------------
    // PATCH
    // ----------------------------------------------------------
    @Test
    void patch_shouldUpdateFields() {
        PersonPatchDTO dto = new PersonPatchDTO();
        dto.setFirstName(Optional.of("Ali"));

        when(personRepository.findById(1L)).thenReturn(Optional.of(person));
        when(personRepository.save(person)).thenReturn(person);
        when(personDetailMapper.toPersonDetailDto(person)).thenReturn(personDetailDTO);

        PersonDetailDTO result = personService.patch(1L, dto);

        assertNotNull(result);
        verify(personRepository).save(person);
    }

    @Test
    void patch_shouldThrow_whenNotFound() {
        when(personRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> personService.patch(1L, new PersonPatchDTO()));
    }

    // ----------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------
    @Test
    void delete_shouldRemovePerson() {
        when(personRepository.existsById(1L)).thenReturn(true);

        personService.delete(1L);

        verify(personRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(personRepository.existsById(1L)).thenReturn(false);

        assertThrows(Exception.class, () -> personService.delete(1L));
    }

    // ----------------------------------------------------------
    // SEARCH
    // ----------------------------------------------------------
    @Test
    void search_shouldReturnPagedResults() {
        Page<Person> page = new PageImpl<>(List.of(person));

        when(personRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(personMapper.toPersonDto(person)).thenReturn(personDTO);

        Page<PersonDTO> result = personService.search("", 0, 10, "id", "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }
}


