package com.cadernonline.event;

public class FilterDisciplinesEvent {
    public final String query;

    public FilterDisciplinesEvent(String query){
        this.query = query;
    }

}