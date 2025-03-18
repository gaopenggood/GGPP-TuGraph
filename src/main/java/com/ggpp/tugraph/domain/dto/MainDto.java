package com.ggpp.tugraph.domain.dto;

import lombok.Data;

@Data
public class MainDto {
    private String name;
    private String code;
    private Object value;

    public MainDto(String name, String code, Object cellValue) {
        this.name = name;
        this.code = code;
        this.value = cellValue;
    }
}
