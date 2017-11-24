package com.sunrain.timetablev4.bean;

public class CourseClassroomBean {

    public String course;
    public String classroom;


    public CourseClassroomBean() {
    }

    public CourseClassroomBean(String course, String classroom) {
        this.course = course;
        this.classroom = classroom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CourseClassroomBean that = (CourseClassroomBean) o;

        if (course != null ? !course.equals(that.course) : that.course != null)
            return false;
        return classroom != null ? classroom.equals(that.classroom) : that.classroom == null;
    }

    @Override
    public int hashCode() {
        int result = course != null ? course.hashCode() : 0;
        result = 31 * result + (classroom != null ? classroom.hashCode() : 0);
        return result;
    }
}
