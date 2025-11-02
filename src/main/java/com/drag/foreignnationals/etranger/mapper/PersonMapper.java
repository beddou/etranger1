package com.drag.foreignnationals.etranger.mapper;

import com.drag.foreignnationals.etranger.dto.PersonCreateDTO;
import com.drag.foreignnationals.etranger.dto.PersonDTO;
import com.drag.foreignnationals.etranger.dto.PersonDetailDTO;
import com.drag.foreignnationals.etranger.entity.Address;
import com.drag.foreignnationals.etranger.entity.Person;
import com.drag.foreignnationals.etranger.entity.ResidencePermit;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
@Mapper(
        componentModel = "spring",
        uses = {NationalityMapper.class, SituationMapper.class, AddressMapper.class, ResidencePermitMapper.class}
)
public interface PersonMapper {

    // light list/read mapping
    @Mapping(target = "nationalityName", source = "nationality.name")
    @Mapping(target = "situationName", source = "situation.type")
    PersonDTO toPersonDto(Person person);

    // detailed mapping (entity -> PersonDetailDto)
    PersonDetailDTO toPersonDetailDto(Person person);

    // Create a new Person entity from createDto (don't set relations/addresses here)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationality", ignore = true) // set in service by id
    @Mapping(target = "situation", ignore = true)   // optional, set in service if provided
    @Mapping(target = "residencePermits", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    Person toEntity(PersonCreateDTO dto);

    // Update existing entity from update DTO (null properties ignored)
   // @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nationality", ignore = true)
    @Mapping(target = "situation", ignore = true)
    @Mapping(target = "residencePermits", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    void updateEntityFromDto(PersonCreateDTO dto, @MappingTarget Person entity);

    // After mapping: set currentAddress and lastResidencePermit for detail DTO
    @AfterMapping
    default void handleDetailMappings(Person person, @MappingTarget PersonDetailDTO dto,
                                      AddressMapper addressMapper,
                                      ResidencePermitMapper rpMapper) {

        // current address: find address with isCurrent == true
        if (person.getAddresses() != null) {
            person.getAddresses().stream()
                    .filter(Address::isCurrent)
                    .findFirst()
                    .ifPresent(a -> dto.setCurrentAddress(addressMapper.toDTO(a)));
        }

        // last residence permit: choose last by id (or by endDate/startDate depending on your model)
        if (person.getResidencePermits() != null && !person.getResidencePermits().isEmpty()) {
            // pick the permit with max id as a simple "last" heuristic
            person.getResidencePermits().stream()
                    .max(Comparator.comparing(ResidencePermit::getDateOfIssue))
                    .ifPresent(rp -> dto.setLastResidencePermit(rpMapper.toDTO(rp)));
        }
    }
}
