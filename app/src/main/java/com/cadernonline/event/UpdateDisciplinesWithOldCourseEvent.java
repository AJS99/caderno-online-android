package com.cadernonline.event;

import com.cadernonline.model.Course;

public class UpdateDisciplinesWithOldCourseEvent {
    public final Course course;

    public UpdateDisciplinesWithOldCourseEvent(Course course){
        this.course = course;
    }

}