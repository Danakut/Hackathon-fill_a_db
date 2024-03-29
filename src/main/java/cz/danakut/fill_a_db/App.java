package cz.danakut.fill_a_db;

import org.jsoup.nodes.Element;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class App {

    static String url = "https://www.czechitas.cz/cs/kalendar-akci#views:view=jplist-list-view";
    static Pattern hoursPattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public static void main(String[] args) throws Exception {

        PageScraper scraper = new PageScraper(url);
        DatabaseRecorder inserter = null;
        try {
            inserter = new DatabaseRecorder();
        } catch (SQLException e) {
            throw new Exception("DatabaseRecorder failed to initialize.");
        }

        scrapeForNewCourses(scraper, inserter);
        inserter.closeRegistrationOnPastCourses();


        inserter.closeConnection();


    }

    private static void scrapeForNewCourses(PageScraper scraper, DatabaseRecorder inserter) {
        int coursesInserted = 0;

        for (Element courseElement : scraper.scrapedElements) {
            Course partialCourse = scraper.scrapeCoursePartially(courseElement);

            //query database of course id
            int inDatabase = inserter.findCourse(partialCourse);

            //if the course is not in database yet
            if (inDatabase == -1) {
                Course completeCourse = scraper.scrapeCourseWhole(partialCourse, courseElement);
                inserter.insertCourse(completeCourse);
                coursesInserted++;
            } else {
                //consider only courses starting today or later
                if (partialCourse.startDate.toLocalDate().compareTo(LocalDate.now()) > 0) {
                    //check registration status in existing course and update if needed
                    boolean courseUpdated = inserter.updateRegistrationStatus(inDatabase, partialCourse);
                    if (courseUpdated) {
                        System.out.println("Update kurzu " + partialCourse.name + ", nová hodnota: " + partialCourse.status);
                    }
                }
            }
        }

        System.out.println("Zapsano " + coursesInserted + " kurzu.");
    }

    private static void updateCurrentCourses(PageScraper scraper, DatabaseRecorder inserter) {

    }

    private static Course makeSampleCourse() {

        Course course = new Course();
        course.id = 1;
        course.type = CourseType.WORKSHOP;
        course.startDate = Date.valueOf("2019-12-02");
        course.endDate = Date.valueOf("2019-12-05");
        course.startTime = "08:10";
        course.endTime = "20:30";
        course.topic = "programuju";
        course.knowledgeLevel = 2;
        course.name = "Jeden nový kurz - Czechitas";
        course.status = RegistrationStatus.valueOf("OTEVRENA");
        course.quickLocation = "ABC Praha";
        course.link = "https://docs.oracle.com/javase/tutorial/essential/io/file.html";
        course.description = "You can use the newBufferedWriter(Path, Charset, OpenOption...) method to write to a file using a BufferedWriter." +
                "The following code snippet shows how to create a file encoded in \"US-ASCII\" using this method:";

        Location newlocation = new Location();
        newlocation.quickName = course.quickLocation;
        newlocation.street = "Bbbbbého 7";
        newlocation.city = "Praha 8";
        newlocation.postalCode = "110 00";
        newlocation.id = 2;
        course.location = newlocation;

        List<String> newList = new ArrayList<>();
        newList.add("Marie Nová");
        newList.add("Markéta Stará");
        course.instructors = newList;

        LocalDateTime nowStamp = (LocalDateTime.now());
        course.lastUpdate = Timestamp.valueOf(nowStamp.getYear() + "-" + nowStamp.getMonthValue() + "-" + nowStamp.getDayOfMonth() + " " +
                nowStamp.getHour() + ":" + nowStamp.getMinute() + ":" + nowStamp.getSecond());

        System.out.println(course);
        return course;
    }
}
