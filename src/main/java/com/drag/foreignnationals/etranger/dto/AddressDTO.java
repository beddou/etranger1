package com.drag.foreignnationals.etranger.dto;


import lombok.*;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    private Long id;

    private String street;
    private String city;
    private String zipCode;

    private boolean current;
    private PersonDTO person;
    private CommuneDTO commune;

}
