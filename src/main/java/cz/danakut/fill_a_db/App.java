package cz.danakut.fill_a_db;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class App {

    static String url = "https://www.czechitas.cz/cs/kalendar-akci#views:view=jplist-list-view";
    static Pattern hoursPattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public static void main(String[] args) throws SQLException {

        List<Course> firstList = new ArrayList<>();
        List<Course> secondList = new ArrayList<>();
        PageScraper scraper = new PageScraper();
        DataInserter inserter = new DataInserter();

        Elements courseElements = scraper.getElementsforScraping(url);
        int test = 0;

        for (Element courseElement : courseElements) {
            Course partialCourse = scraper.scrapeCoursePartially(courseElement);
            firstList.add(partialCourse);
            test++;
            System.out.println("Partial course added." + test);

            //splnena podminka, ze kurz dosud nebyl ulozen v databazi
            Course completedCourse = scraper.scrapeCourseWhole(partialCourse, courseElement);
            secondList.add(completedCourse);
            System.out.println("Whole course added." + test);
        }

        int listSize = firstList.size();
        System.out.println("1st list is " + listSize + " courses big.");
        listSize = secondList.size();
        System.out.println("2nd list is " + listSize + " courses big.");

    }
}
