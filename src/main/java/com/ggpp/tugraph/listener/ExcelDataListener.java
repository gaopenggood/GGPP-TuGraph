package com.ggpp.tugraph.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.ggpp.tugraph.domain.dto.MainDto;
import graphql.language.ObjectValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExcelDataListener extends AnalysisEventListener<Map<Integer, Object>> {
    private List<MainDto> budgets = new ArrayList<>();
    private String currentCode;
    private List<String> fieldNames;
    private boolean isFirstRow = true;

    public ExcelDataListener(List<String> fieldNames) {
        this.fieldNames = fieldNames; // 假设表头字段名已知
    }

    @Override
    public void invoke(Map<Integer, Object> data, AnalysisContext context) {
        for(int i=1; i<fieldNames.size()+1; i++) {
            String fieldName = fieldNames.get(i-1);
            String code = data.get(0).toString();
            Object value = data.get(i);
            MainDto budget = new MainDto(fieldName, code, value);//data.get(i)
            budgets.add(budget);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 解析完成后的操作
    }

    public List<MainDto> getBudgets() {
        return budgets;
    }

}
