package cz.danakut.fill_a_db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseRecorder implements CourseRecorder{

    private static String dbUrl = "jdbc:mariadb://localhost:3306/hackathon";
    private static String user = "jetidea";
    private static String pass = "ideapass";

    Connection conn;

    public DatabaseRecorder() throws SQLException {
        conn = DriverManager.getConnection(dbUrl, user, pass);
    }

    public void closeConnection() throws SQLException {
        if (!conn.isClosed()) {
            conn.close();
        }
    }

    public int findCourse(Course course) throws SQLException {
        int id = -1;

        //solves the problem of startTime == null
        String findString;
        if (course.startTime == null) {
            findString = "SELECT id FROM courses WHERE name = ? AND startDate = ? AND startTime IS NULL AND quickLocation = ?";
        } else {
            findString = "SELECT id FROM courses WHERE name = ? AND startDate = ? AND startTime = ? AND quickLocation = ?";
        }
        PreparedStatement stmnt = this.conn.prepareStatement(findString);
        stmnt.setString(1, course.name);
        stmnt.setDate(2, course.startDate);
        if (course.startTime == null) {
            stmnt.setString(3, course.quickLocation);

        } else {
            stmnt.setString(3, course.startTime);
            stmnt.setString(4, course.quickLocation);
        }

        ResultSet results = stmnt.executeQuery();
        if (results.next()) {
            id = results.getInt("id");
        }

        if (!stmnt.isClosed()) {
            stmnt.close();
        }
        return id;
    }

    public void insertCourse(Course course) throws SQLException {
        String insertionString = "INSERT INTO courses (" +
                "type, " +
                "startDate, " +
                "endDate, " +
                "startTime, " +
                "endTime, " +
                "topic, " +
                "knowledgeLevel, " +
                "name, " +
                "status, " +
                "quickLocation," +
                "location," +
                "link, " +
                "description" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmnt = this.conn.prepareStatement(insertionString);

        stmnt.setString(1, typeToString(course.type));
        stmnt.setDate(2, course.startDate);
        stmnt.setDate(3, course.endDate);
        stmnt.setString(4, course.startTime);
        stmnt.setString(5, course.endTime);
        stmnt.setString(6, course.topic);
        stmnt.setInt(7, course.knowledgeLevel);
        stmnt.setString(8, course.name);
        stmnt.setString(9, statusToString(course.status));
        stmnt.setString(10, course.quickLocation);
        stmnt.setString(12, course.link);
        stmnt.setString(13, course.description);

        //provide id from a record in "locations" table. If a matching quickLocation/quickName is not found, a new record is created and its id provided
        int locationId = lookUpLocationId(course.location);
        if (locationId == -1) {
            locationId = insertNewLocation(course.location);
        }
        stmnt.setInt(11, locationId);

        //make the insertion before processing instructors - to have a course id available (assigned automatically by the database)
        stmnt.executeUpdate();


        //add instructors by recording relations to courses/instructors table. If an instructor has no record in "instructors" table yet, a new record
        //    is created and its id provided - this throws "java.sql.SQLException: Cannot add or update a child row: a foreign key constraint fails"4
        //new approach - first make sure all instructors are in their table, THEN pair instructors with courses

        List<Integer> instructorIds = new ArrayList<>();
        for (String name : course.instructors) {
            int id = lookUpInstructorId(name);
            if (id == -1) {
                id = insertNewInstructor(name);
            }
            instructorIds.add(id);
        }

        PreparedStatement instructorStmnt = this.conn.prepareStatement("INSERT INTO coursesAndInstructors VALUES (?, ?)");

        for (Integer instructorId : instructorIds) {

            instructorStmnt.setInt(1, findCourse(course));
            instructorStmnt.setInt(2, instructorId);

            int rowsAffected = instructorStmnt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create course/instructor relation. Course: " + course.name + ", instructorId: " + instructorId);
            }
        }
        instructorStmnt.close();

        stmnt.close();
    }

    private String typeToString(CourseType type) {
        String courseType = "";
        switch (type) {
            case WORKSHOP:
                courseType = "workshop";
                break;
            case DLOUHODOBY:
                courseType = "dlouhodoby";
                break;
            case INTENZIVNI:
                courseType = "intenzivni";
                break;
            case AKCE:
                courseType = "akce";
                break;
            case NEURCENO: courseType = "neurceno";
        }

        return courseType;
    }

    private String statusToString(RegistrationStatus status) {
        String courseStatus = "";
        switch (status) {
            case OTEVRENA:
                courseStatus = "otevrena";
                break;
            case UZAVRENA:
                courseStatus = "uzavrena";
                break;
            case POZDEJI:
                courseStatus = "pozdeji";
                break;
            case NETREBA:
                courseStatus = "netreba";
                break;
            case NEZJISTENO: courseStatus = "nezjisteno";
        }

        return courseStatus;
    }

    //returns -1 if the place is not in database yet, otherwise returns the location's id
    private int lookUpLocationId (Location location) throws SQLException {
        PreparedStatement stmnt = this.conn.prepareStatement("SELECT id FROM locations WHERE quickName = ?");
        stmnt.setString(1, location.quickName);

        ResultSet results = stmnt.executeQuery();
        int id = -1;
        if (results.next()) {
            id = results.getInt("id");
        }

        stmnt.close();
        return id;
    }

    private int insertNewLocation(Location location) throws SQLException {
        PreparedStatement stmnt = this.conn.prepareStatement
                ("INSERT INTO locations (quickName, name, street, city, postalCode) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        stmnt.setString(1, location.quickName);
        stmnt.setString(2, location.name);
        stmnt.setString(3, location.street);
        stmnt.setString(4, location.city);
        stmnt.setString(5, location.postalCode);

        int rowsAffected = stmnt.executeUpdate();
        if (rowsAffected == 0) {
            throw new SQLException("Failed to create location: " + location.quickName + "|" + location.street + "|" + location.city);
        }

        try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt("id");
            }
            else {
                throw new SQLException("Failed to retrieve id for location: " + location.quickName);
            }
        }
    }

    //returns -1 if the instructors is not in database yet, otherwise returns the instructors's id
    private int lookUpInstructorId (String name) throws SQLException {
        PreparedStatement stmnt = this.conn.prepareStatement("SELECT id FROM instructors WHERE firstname = ? AND lastname = ?");
        String[] names = name.split(" ");
        //docasne reseni problemu s viceslovnymi jmeny/prijmenimi - rozpoznat, co je co - vybira pouze prvni (coz je urcite krestni jmeno) a posledni
        //     (coz je urcite prijmeni) slovo z retezce
        String fName = names[0];
        String lName = names[names.length - 1];
        stmnt.setString(1, fName);
        stmnt.setString(2, lName);

        ResultSet results = stmnt.executeQuery();
        int id = -1;
        if (results.next()) {
            id = results.getInt("id");
        }

        stmnt.close();

        return id;
    }

    private int insertNewInstructor(String name) throws SQLException {
        PreparedStatement stmnt = this.conn.prepareStatement("INSERT INTO instructors (firstname, lastname) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
        String[] names = name.split(" ");
        stmnt.setString(1, names[0]);
        stmnt.setString(2, names[names.length - 1]);

        int rowsAffected = stmnt.executeUpdate();
        if (rowsAffected == 0) {
            throw new SQLException("Failed to create instructor: " + name);
        }

        try (ResultSet generatedKeys = stmnt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt("id");
            }
            else {
                throw new SQLException("Failed to retrieve id for instructor: " + name);
            }
        }
    }


}
