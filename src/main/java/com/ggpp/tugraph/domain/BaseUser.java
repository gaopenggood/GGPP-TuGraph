package com.ggpp.tugraph.domain;

import lombok.Data;

@Data
public class BaseUser {

    private Long id;
    private String name;
    private String code;
    private String userName;
    private String password;
}
