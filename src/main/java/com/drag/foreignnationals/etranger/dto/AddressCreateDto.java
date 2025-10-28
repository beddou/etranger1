package com.drag.foreignnationals.etranger.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressCreateDto {
    private String street;
    private String city;
    private String zipCode;
    private Long communeId;
}
