package com.gyang.tutu.wordselect;

/**
 * Created by declan.guo on 15-10-28.
 */
public class SelectOptionModel {

    private String id;
    private String content;
    private String questionId;
    private String stemId;
    private int num;

    public SelectOptionModel() {

    }

    public SelectOptionModel(String id, String content, String questionId, String stemId, int num) {
        this.id = id;
        this.content = content;
        this.questionId = questionId;
        this.stemId = stemId;
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getStemId() {
        return stemId;
    }

    public void setStemId(String stemId) {
        this.stemId = stemId;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
