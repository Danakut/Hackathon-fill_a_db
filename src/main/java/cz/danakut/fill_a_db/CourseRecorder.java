package cz.danakut.fill_a_db;


import java.sql.SQLException;

public interface CourseRecorder {

    public int findCourse(Course course) throws Exception;

    public void insertCourse(Course course) throws Exception;

}
