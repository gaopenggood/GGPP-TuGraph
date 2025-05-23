package com.ggpp.tugraph.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class BudgetDto {
    private String code;

    private String cbsName;

    private Map<String, Object> fees;
}
