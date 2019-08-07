package com.example.myapplication.models;


import java.util.ArrayList;
import java.util.List;

public class ModelTimetable extends ModelResponse {

    private List<ModelTimetableCell> timetable = new ArrayList<ModelTimetableCell>();

    public ModelTimetable() {

    }

    public List<ModelTimetableCell> getTimetable() {
        return timetable;
    }

    public void setTimetable(List<ModelTimetableCell> timetable) {
        this.timetable = timetable;
    }
}
