package cz.danakut.fill_a_db;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class App {

    static String url = "https://www.czechitas.cz/cs/kalendar-akci#views:view=jplist-list-view";
    static Pattern hoursPattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public static void main(String[] args) throws SQLException {

        PageScraper scraper = new PageScraper();
        List<Course> courseList =  scraper.parsePage(url);

        DataInserter inserter = new DataInserter();

        for (Course course : courseList) {

            boolean wasInserted = inserter.insertIfNotPresent(course);
            System.out.println(course.name + ": stav vlozeni " + wasInserted);

        }
    }
}
