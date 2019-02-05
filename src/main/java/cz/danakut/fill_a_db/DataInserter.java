package cz.danakut.fill_a_db;

import javax.swing.plaf.nimbus.State;
import java.sql.*;

public class DataInserter {

    private static String dbUrl = "jdbc:mariadb://localhost:3306/hackathon";
    private static String user = "jetidea";
    private static String pass = "ideapass";

    Connection conn;

    //first thing to call on a datainserter
    public void openConnection() throws SQLException {
        conn = DriverManager.getConnection(dbUrl, user, pass);
    }

    public void closeConnection() throws SQLException {
        if (!conn.isClosed()) {
            conn.close();
        }

    }

    public int findInDatabase(Course course) throws SQLException {
        int id = -1;

        String foundInDatabaseString = "SELECT id FROM courses WHERE (name = ?, startDate = ?, location = ?)";
        PreparedStatement stmnt = this.conn.prepareStatement(foundInDatabaseString);
        stmnt.setString(1, course.name);
        stmnt.setDate(2, course.startDate);
        stmnt.setString(3, course.quickLocation);
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
        //    is created and its id provided
        PreparedStatement instructorStmnt = this.conn.prepareStatement("INSERT INTO coursesAndInstructors VALUES (?, ?)");
        for (String name : course.instructors) {
            int id = lookUpInstructorId(name);
            if (id == -1) {
                id = insertNewInstructor(name);
            }
            instructorStmnt.setInt(1, id);
            instructorStmnt.setInt(2, findInDatabase(course));

            int rowsAffected = instructorStmnt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create course/instructor relation. Course: " + course.name + ", instructor: " + name);
            }
        }
        instructorStmnt.close();

        stmnt.close();
    }

    private String typeToString(CourseType type) {
        String courseType = "";
        switch (type) {
            case WORKSHOP: courseType = "workshop";
            case DLOUHODOBY: courseType = "dlouhodoby";
            case INTENZIVNI: courseType = "intenzivni";
            case AKCE: courseType = "akce";
            case NEURCENO: courseType = "neurceno";
        }

        return courseType;
    }

    private String statusToString(RegistrationStatus status) {
        String courseStatus = "";
        switch (status) {
            case OTEVRENA: courseStatus = "otevrena";
            case UZAVRENA: courseStatus = "uzavrena";
            case POZDEJI: courseStatus = "pozdeji";
            case NETREBA: courseStatus = "netreba";
            case NEZJISTENO: courseStatus = "nezjisteno";
        }

        return courseStatus;
    }

    //returns -1 if the place is not in database yet, otherwise returns the location's id
    private int lookUpLocationId (Location location) throws SQLException {
        PreparedStatement stmnt = this.conn.prepareStatement("SELECT id FROM locations WHERE quickname = ?");
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
        PreparedStatement stmnt = this.conn.prepareStatement("INSERT INTO locations VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
        PreparedStatement stmnt = this.conn.prepareStatement("SELECT id FROM instructors WHERE (firstname = ?, lastname = ?)");
        String[] names = name.split(" ");
        //docasne reseni problemu s viceslovnymi jmeny/prijmenimi - rozpoznat, co je co - vybira pouze prvni (coz je urcite krestni jmeno) a posledni
        //     (coz je urcite prijmeni) slovo z retezce
        stmnt.setString(1, names[0]);
        stmnt.setString(2, names[names.length - 1]);

        ResultSet results = stmnt.executeQuery();
        int id = -1;
        if (results.next()) {
            id = results.getInt("id");
        }

        stmnt.close();

        return id;
    }

    private int insertNewInstructor(String name) throws SQLException {
        PreparedStatement stmnt = this.conn.prepareStatement("INSERT INTO locations VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
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
