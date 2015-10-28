package com.gyang.tutu.wordselect;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by declan.guo on 15-10-28.
 */
public class WordExcelModel {
    private String stem;
    private List<String> selectOptions = Lists.newArrayList();
    private String correctOption;
    private String analysis;

    public WordExcelModel() {
    }

    public WordExcelModel(String stem, List<String> selectOptions, String correctOption, String analysis) {
        this.stem = stem;
        this.selectOptions = selectOptions;
        this.correctOption = correctOption;
        this.analysis = analysis;
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public List<String> getSelectOptions() {
        return selectOptions;
    }

    public void setSelectOptions(List<String> selectOptions) {
        this.selectOptions = selectOptions;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
}
