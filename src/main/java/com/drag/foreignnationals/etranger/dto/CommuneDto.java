package com.drag.foreignnationals.etranger.dto;

import com.drag.foreignnationals.etranger.entity.Address;
import jakarta.persistence.OneToOne;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommuneDto {

    private Long id;

    private String name;
    private String nameAr;
    private String code;

    private AddressDto addressDto;
}
