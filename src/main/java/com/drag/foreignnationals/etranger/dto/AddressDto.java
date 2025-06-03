package com.drag.foreignnationals.etranger.dto;


import lombok.*;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {

    private Long id;

    private String street;
    private String city;
    private String zipCode;

    private boolean current;
    private PersonDTO personDTO;
    private CommuneDto communeDto;

}
