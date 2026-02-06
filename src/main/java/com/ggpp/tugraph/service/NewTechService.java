package com.ggpp.tugraph.service;

import cn.hutool.core.date.StopWatch;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.huaban.analysis.jieba.JiebaSegmenter;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewTechService {

    public Object doNLPTest() {
        String targetStr = "费控数据分类表";
        this.doNLPTrance(targetStr);
        return "";
    }

    private void doNLPTrance(String targetStr) {
        StopWatch sw = new StopWatch();
        sw.start("1 opennlp-tools");
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] tokens1 = tokenizer.tokenize(targetStr);
        String str1 = this.formatterStr(tokens1);
        sw.stop();
        sw.start("2 jieba-analysis");
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<String> words = segmenter.sentenceProcess(targetStr);
        String[] tokens2 = words.toArray(new String[0]);
        String str2 = this.formatterStr(tokens2);
        sw.stop();
        sw.start("3 hanlp");
        List<Term> termList = HanLP.segment(targetStr);
        String[] tokens3 = new String[termList.size()];
        int i = 0;
        for (Term term : termList) {
            tokens3[i] = term.word;
            i++;
        }
        String str3 = this.formatterStr(tokens3);
        sw.stop();
        Arrays.stream(sw.getTaskInfo()).forEach(x -> log.info("{} 【{}】 毫秒", x.getTaskName(), x.getTimeMillis()));
        log.info("\n1 opennlp-tools 结果：{} \n2 jieba-analysis 结果：{} \n3 hanlp 结果：{}", str1, str2, str3);
    }

    private String formatterStr(String[] tokens) {
        return String.join(" ", tokens);
    }
}
