package com.gyang.tutu.wordselect;

import java.util.List;

/**
 * Created by declan.guo on 15-10-28.
 */
public class WordSelectOptionModel {
    private String id;
    private String code;
    private String num;
    private String stem;
    private String analytical;
    private List<SelectOptionModel> options;
    private List<String> correctOptionses;

    public WordSelectOptionModel() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getStem() {
        return stem;
    }

    public void setStem(String stem) {
        this.stem = stem;
    }

    public String getAnalytical() {
        return analytical;
    }

    public void setAnalytical(String analytical) {
        this.analytical = analytical;
    }

    public List<SelectOptionModel> getOptions() {
        return options;
    }

    public void setOptions(List<SelectOptionModel> options) {
        this.options = options;
    }

    public List<String> getCorrectOptionses() {
        return correctOptionses;
    }

    public void setCorrectOptionses(List<String> correctOptionses) {
        this.correctOptionses = correctOptionses;
    }
}
