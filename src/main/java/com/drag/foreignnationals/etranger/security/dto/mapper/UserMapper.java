package com.drag.foreignnationals.etranger.security.dto.mapper;

import com.drag.foreignnationals.etranger.dto.CommuneDTO;
import com.drag.foreignnationals.etranger.entity.Commune;
import com.drag.foreignnationals.etranger.security.dto.request.SignupRequest;
import com.drag.foreignnationals.etranger.security.dto.response.UserResponse;
import com.drag.foreignnationals.etranger.security.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "locked", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    User toEntity(SignupRequest signupRequest);

}
