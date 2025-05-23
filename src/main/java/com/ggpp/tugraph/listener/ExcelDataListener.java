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
    private Integer rowIndex;//第一行数据 从0开始
    private Integer columnIndex;//第一列数据 从1开始
    private List<String> xCodeList;

    public ExcelDataListener(Integer rowIndex, Integer columnIndex, List<String> xCodeList) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
        this.xCodeList = xCodeList;
    }

    @Override
    public void invoke(Map<Integer, Object> data, AnalysisContext context) {
        int index = context.readRowHolder().getRowIndex();
        if(index >= rowIndex) {
            String yCode = data.get(0).toString();
            int j = columnIndex;
            for(int i=0; i<xCodeList.size(); i++) {
                String fieldName = xCodeList.get(i);
                Object value = data.get(j);
                MainDto budget = new MainDto(fieldName, yCode, value);//data.get(i)
                budgets.add(budget);
                j++;
            }
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
