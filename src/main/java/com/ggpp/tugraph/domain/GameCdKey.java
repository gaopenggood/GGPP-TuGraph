package com.ggpp.tugraph.domain;

import lombok.Data;

@Data
public class GameCdKey {
    private Long id;
    private Long gameId;
    private String code;
    private String name;
    private String cdKey;
    private String gameName;
    private String gameCode;
    private Integer workFlag;
}
