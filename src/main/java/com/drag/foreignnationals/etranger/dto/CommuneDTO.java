package com.drag.foreignnationals.etranger.dto;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommuneDTO {

    private Long id;

    private String name;
    private String nameAr;
    private String code;

    private AddressDTO address;
}
