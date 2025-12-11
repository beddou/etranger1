package com.drag.foreignnationals.etranger.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class AddressServiceImpl {

    @Autowired
    PersonRepository personRepository;
    @Autowired
    AddressMapper addressMapper;
    @Autowired
    CommuneRepository communeRepository;
    @Autowired
    AddressRepository addressRepository;

    @Transactional
    public AddressDTO add(Long idPerson, AddressCreateDto dto) {

        Person person = personRepository.findById(idPerson)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ENTITY_NOT_FOUND,
                        "Person not found with ID " + idPerson
                ));


        // Get current active address
        Address currentAddress = person.getCurrentAddress();

        if (currentAddress != null) {
            currentAddress.setCurrent(false);
            addressRepository.save(currentAddress);

        }
        Address address = addressMapper.toEntity(dto);

        // Set commune
        if (dto.getCommuneId() == null ) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Commune information is required to add address."
            );
        }


            Commune commune = communeRepository.findById(dto.getCommuneId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.ENTITY_NOT_FOUND,
                            "Commune not found with ID " + dto.getCommuneId()
                    ));

            address.setCommune(commune);
        address.setPerson(person);
        address.setCurrent(true);
        return(addressMapper.toDTO(addressRepository.save(address))) ;

}
    List<AddressDTO> getAllByPerson(Long idPerson) {

        return addressRepository.findByPersonId(idPerson)
                .stream().map(addressMapper::toDTO).toList();
    }
}
