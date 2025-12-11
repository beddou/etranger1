package com.drag.foreignnationals.etranger.service.address;


import com.drag.foreignnationals.etranger.dto.AddressCreateDto;
import com.drag.foreignnationals.etranger.dto.AddressDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.exception.BusinessException;
import com.drag.foreignnationals.etranger.exception.ErrorCode;
import com.drag.foreignnationals.etranger.mapper.AddressMapper;
import com.drag.foreignnationals.etranger.repository.AddressRepository;
import com.drag.foreignnationals.etranger.repository.CommuneRepository;
import com.drag.foreignnationals.etranger.repository.PersonRepository;
import com.drag.foreignnationals.etranger.service.AddressService;
import com.drag.foreignnationals.etranger.service.impl.AddressServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

    @ExtendWith(MockitoExtension.class)
    public class AddressServiceTest {

        @InjectMocks
        private AddressServiceImpl addressService;

        @Mock
        private PersonRepository personRepository;

        @Mock
        private AddressRepository addressRepository;

        @Mock
        private CommuneRepository communeRepository;

        @Mock
        private AddressMapper addressMapper;

        private Person person;
        private AddressCreateDto dto;
        private Address address;
        private Commune commune;
        private AddressDTO addressDTO;

        @BeforeEach
        void setUp() {
            person = new Person();
            person.setId(1L);

            dto = new AddressCreateDto();
            dto.setCommuneId(10L);

            address = new Address();
            commune = new Commune();
            commune.setId(10L);

            addressDTO = new AddressDTO();
        }

        // ----------------------------------------------------------
        // SUCCESS CASE: No previous address
        // ----------------------------------------------------------
        @Test
        void add_success_withoutPreviousAddress() {

            // Arrange
            when(personRepository.findById(1L)).thenReturn(Optional.of(person));
            when(addressMapper.toEntity(dto)).thenReturn(address);
            when(communeRepository.findById(10L)).thenReturn(Optional.of(commune));
            when(addressRepository.save(address)).thenReturn(address);
            when(addressMapper.toDTO(address)).thenReturn(addressDTO);

            // Act
            AddressDTO result = addressService.add(1L, dto);

            // Assert
            assertNotNull(result);
            assertSame(addressDTO, result);
            assertSame(person, address.getPerson());
            assertSame(commune, address.getCommune());
            assertTrue(address.isCurrent());

            verify(addressRepository).save(address);
        }

        // ----------------------------------------------------------
        // SUCCESS CASE: Previous address exists → must deactivate
        // ----------------------------------------------------------
        @Test
        void add_success_withPreviousAddress() {

            Address oldAddress = new Address();
            oldAddress.setCurrent(true);
            oldAddress.setId(99L);

            oldAddress.setPerson(person);
            person.getAddresses().add(oldAddress);

            // Arrange
            when(personRepository.findById(1L)).thenReturn(Optional.of(person));
            when(addressMapper.toEntity(dto)).thenReturn(address);
            when(communeRepository.findById(10L)).thenReturn(Optional.of(commune));
            when(addressRepository.save(oldAddress)).thenReturn(oldAddress); // updating old
            when(addressRepository.save(address)).thenReturn(address);
            when(addressMapper.toDTO(address)).thenReturn(addressDTO);

            // Act
            AddressDTO result = addressService.add(1L, dto);

            // Assert
            assertNotNull(result);
            assertFalse(oldAddress.isCurrent());
            assertSame(person, address.getPerson());
            assertTrue(address.isCurrent());

            verify(addressRepository).save(oldAddress); // previous address must be updated
            verify(addressRepository).save(address);    // new address saved
        }

        // ----------------------------------------------------------
        // ERROR CASE: Person not found
        // ----------------------------------------------------------
        @Test
        void add_error_personNotFound() {

            when(personRepository.findById(1L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> addressService.add(1L, dto)
            );

            assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Person not found"));

            verify(addressRepository, never()).save(any());
        }

        // ----------------------------------------------------------
        // ERROR CASE: Missing communeId
        // ----------------------------------------------------------
        @Test
        void add_error_missingCommuneId() {

            dto.setCommuneId(null);
            when(personRepository.findById(1L)).thenReturn(Optional.of(person));

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> addressService.add(1L, dto)
            );

            assertEquals(ErrorCode.VALIDATION_ERROR, ex.getErrorCode());
            assertEquals("Commune information is required to add address.", ex.getMessage());

            verify(addressRepository, never()).save(any());
        }

        // ----------------------------------------------------------
        // ERROR CASE: Commune not found
        // ----------------------------------------------------------
        @Test
        void add_error_communeNotFound() {

            when(personRepository.findById(1L)).thenReturn(Optional.of(person));
            when(addressMapper.toEntity(dto)).thenReturn(address);
            when(communeRepository.findById(10L)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> addressService.add(1L, dto)
            );

            assertEquals(ErrorCode.ENTITY_NOT_FOUND, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Commune not found"));

            verify(addressRepository, never()).save(any());
        }


}
