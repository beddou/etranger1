package com.drag.foreignnationals.etranger.dto;

import lombok.*;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommuneDTO {

    private Long id;

    private String name;
    private String nameAr;
    private String code;

    private List<AddressDTO> addresses;
}
