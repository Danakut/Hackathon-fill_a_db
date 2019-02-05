package cz.danakut.fill_a_db;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class App {

    static String url = "https://www.czechitas.cz/cs/kalendar-akci#views:view=jplist-list-view";
    static Pattern hoursPattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public static void main(String[] args) throws SQLException {

        PageScraper scraper = new PageScraper(url);
        DataInserter inserter = new DataInserter();
        Map<Course, Element> newCourses = new LinkedHashMap<>();


        for (Element courseElement : scraper.scrapedElements) {
            Course partialCourse = scraper.scrapeCoursePartially(courseElement);

            //query database
            int inDatabase = inserter.findInDatabase(partialCourse);

            //if the course is not in database yet
            if (inDatabase == -1) {
                newCourses.put(partialCourse, courseElement);
            }
        }

        //later
        List<Course> courseList = new ArrayList<>();
        for (Course course : newCourses.keySet()) {
            Course completeCourse = scraper.scrapeCourseWhole(course, newCourses.get(course));
            //this is a list of courses to insert into database
            courseList.add(completeCourse);
        }

        //insert from list to database
        inserter.openConnection();
        int coursesInserted = 0;
        for (Course course : courseList) {
            inserter.insertCourse(course);
            coursesInserted++;
        }
        inserter.closeConnection();

        System.out.println("Zapsano " + coursesInserted + "kurzu.");

    }
}
