package com.drag.foreignnationals.etranger.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NationalityDTO {
    private Long id;
    private String country;
    private String countryAr;
}