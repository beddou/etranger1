package com.drag.foreignnationals.etranger.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NationalityDTO {
    private Long id;
    private String country;
    private String countryAr;
    private List<PersonDTO> persons;
}