package com.example.myapplication.models;

import com.example.myapplication.R;

import java.util.ArrayList;

public class ModelNews extends ModelResponse {
    private String faculty_department;
    private String title;
    private String date;
    private boolean type; // false: downContent, true: viewContent
    private String content;
    private String url;
    private int image = R.drawable.logo_must;
    private ArrayList<ModelAttachment> attachment_list;

    public ModelNews() {

    }

    public ModelNews(String faculty_department, String title, String date, boolean type, String url, int image) {
        this.faculty_department = faculty_department;
        this.title = title;
        this.date = date;
        this.type = type;
        this.url = url;
        this.image = image;
    }

    public String getFaculty_department() {
        return faculty_department;
    }

    public void setFaculty_department(String faculty_department) {
        this.faculty_department = faculty_department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public ArrayList<ModelAttachment> getAttachment_list() {
        return attachment_list;
    }

    public void setAttachment_list(ArrayList<ModelAttachment> attachment_list) {
        this.attachment_list = attachment_list;
    }


}
