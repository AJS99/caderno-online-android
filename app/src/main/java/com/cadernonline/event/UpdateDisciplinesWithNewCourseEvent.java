package com.cadernonline.event;

import com.cadernonline.model.Course;

public class UpdateDisciplinesWithNewCourseEvent {
    public final Course course;

    public UpdateDisciplinesWithNewCourseEvent(Course course){
        this.course = course;
    }

}