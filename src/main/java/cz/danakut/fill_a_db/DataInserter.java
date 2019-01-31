package cz.danakut.fill_a_db;

import java.sql.*;

public class DataInserter {

    private static String dbUrl = "jdbc:mariadb://localhost:3306/hackathon";
    private static String user = "jetidea";
    private static String pass = "ideapass";
    private static String insertionString =
            "INSERT INTO courses (type, startDate, endDate, startTime, endTime, topic, knowledgeLevel, name, status, location, instructor, link, description)" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static String selectString =
            "SELECT id FROM courses WHERE name = ? and startDate = ? and location = ?";

    Connection conn;
    PreparedStatement selectStatement;
    PreparedStatement insertStatement;

    public DataInserter() throws SQLException {

        conn = DriverManager.getConnection(dbUrl, user, pass);
        selectStatement = conn.prepareStatement(selectString);
        insertStatement = conn.prepareStatement(insertionString);

    }

    public boolean insertIfNotPresent(Course course) {
        boolean inserted = false;
        //tests whether the scraped course has already been added to the database (in which case it is not inserted now)
        try {
            selectStatement.setString(1, course.name);
            selectStatement.setDate(2, course.startDate);
            selectStatement.setString(3, course.location);

            ResultSet resultSet = selectStatement.executeQuery();
            if (!resultSet.next()) {
                insertStatement.setString(1, course.type.toDatabaseString());
                insertStatement.setDate(2, course.startDate);
                insertStatement.setDate(3, course.endDate);
                insertStatement.setString(4, course.startTime);
                insertStatement.setString(5, course.endTime);
                insertStatement.setString(6, course.topic);
                insertStatement.setInt(7, course.knowledgeLevel);
                insertStatement.setString(8, course.name);
                insertStatement.setString(9, course.status.toDatabaseString());
                insertStatement.setString(10, course.location);
                insertStatement.setString(11, course.instructor);
                insertStatement.setString(12, course.link);
                insertStatement.setString(13, course.description);

                insertStatement.execute();
                inserted = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return inserted;
    }



}
