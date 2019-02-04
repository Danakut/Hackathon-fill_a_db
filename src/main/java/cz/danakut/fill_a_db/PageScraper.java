package cz.danakut.fill_a_db;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageScraper {

    static Pattern hoursPattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public Elements getElementsforScraping(String url) {

        Document parsedDocument = null;

        try {
            parsedDocument = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Elements calendarEvents = parsedDocument.select(".list-item"); //ArrayList
        return calendarEvents;
    }

    public Course scrapeCoursePartially(Element calendarEvent) {
        Course newCourse = new Course();
        newCourse.name = scrapeName(calendarEvent);
        newCourse.startDate = scrapeStartDay(calendarEvent);
        newCourse.quickLocation = scrapeQuickLocation(calendarEvent);

        return newCourse;
    }

    public Course scrapeCourseWhole(Course partiallyScrapedCourse, Element calendarEvent) {
        Attributes attributes = calendarEvent.attributes();
        Element duration = calendarEvent.selectFirst(".day");

        Course newCourse = partiallyScrapedCourse;

        newCourse.type = scrapeType(calendarEvent);
        newCourse.endDate = scrapeEndDay(duration);
        newCourse.startTime = scrapeHours(duration)[0];
        newCourse.endTime = scrapeHours(duration)[1];
        newCourse.topic = scrapeTopic(calendarEvent);
        newCourse.knowledgeLevel = scrapeKnowledgeLevel(calendarEvent);
        newCourse.status = scrapeStatus(calendarEvent);
        newCourse.instructor = "";
        newCourse.link = scrapeLink(attributes);
        newCourse.description = scrapeDescription(calendarEvent);
        newCourse.location = scrapeLocation(newCourse.link);

        return newCourse;
    }

    private static CourseType scrapeType(Element calendarEvent) {
        String type = calendarEvent.selectFirst(".intesity span").text();
        if (type.equals("Jednodenní")) {
            return CourseType.WORKSHOP;
        } else if (type.equals("Pravidelný")) {
            return CourseType.DLOUHODOBY;
        } else if (type.equals("Intenzivní")) {
            return CourseType.INTENZIVNI;
        } else {
            return CourseType.NEURCENO;
        }
    }

    private static Date scrapeStartDay(Element calendarEvent) {
        String string = calendarEvent.selectFirst(".date").text();
        // convert the string to a proper format for Date.valuoOf()
        return Date.valueOf(string.replaceAll("/", "-"));

    }

    private static Date scrapeEndDay(Element duration) {
        if (duration.is(".multi")) {
            String dayString = duration.select(".dday").last().text().trim();
            String day = dayString.substring(0, dayString.length() - 1);
            String monthyear = duration.select(".dmonth").last().text().trim();
            String month = monthyear.substring(0,3);
            String year = monthyear.substring(5);
            return Date.valueOf(year + "-" + MonthConverter.fromString(month) + "-" + day);
        }

        return null;
    }

    private static String[] scrapeHours(Element duration) {
        String[] hours = new String[2];

        Element dayAndHoursDiv = duration.selectFirst(".hours");
        if (dayAndHoursDiv != null) {
            String dayAndHours = dayAndHoursDiv.text();
            Matcher matcher = hoursPattern.matcher(dayAndHours);
            matcher.find();
            hours[0] = matcher.group();
            matcher.find();
            hours[1] = matcher.group();
        }

        return hours;
    }

    private static String scrapeTopic(Element calendarEvent) {
        return calendarEvent.selectFirst(".grid-icon span").text().trim();
    }

    private static int scrapeKnowledgeLevel(Element calendarEvent) {
        String levelInfo = calendarEvent.selectFirst(".eventDifficulty").className();
        String level = levelInfo.substring(16,levelInfo.length() - 6 );
        int result;
        switch (level) {
            case "zadne": result = 0;
            case "zacatecnik": result = 1;
            case "pokrocily": result = 2 ;
            default: result = 3;
        }

        return result;
    }

    private static String scrapeName(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventName div").text().trim();
    }

    private static RegistrationStatus scrapeStatus(Element calendarEvent) {
        String[] registrationInfo = calendarEvent.selectFirst(".eventStatus").className().split("\\s+");
        String status = registrationInfo[1];

        switch (status) {
            case "registraceOtevrena": return RegistrationStatus.OTEVRENA;
            case "konecRegistrace" : return RegistrationStatus.UZAVRENA;
            case "dejteMiVedet" : return RegistrationStatus.POZDEJI;
            case "bezRegistrace" : return RegistrationStatus.NETREBA;
            default: return RegistrationStatus.NEZJISTENO;
        }
    }

    private static String scrapeQuickLocation(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventPlace div").text().trim();
    }

    private static String scrapeLink(Attributes attributes) {
        return attributes.get("href");
    }

    private static String scrapeDescription(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventDesc").text().trim();
    }

    private static Location scrapeLocation(String courseUrl) {
        Document parsedDocument = null;
        try {
            parsedDocument = Jsoup.connect(courseUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element addressDiv = parsedDocument.getElementsByAttributeValue("itemtype", "http://schema.org/Organization").first();
        Location location = new Location();

        Element nameDiv = addressDiv.getElementsByAttributeValue("itemprop", "name").first();
        if (nameDiv != null) {
            location.name = nameDiv.text().trim();
        }

        Element streetDiv = addressDiv.getElementsByAttributeValue("itemprop", "streetAddress").first();
        if (streetDiv != null) {
            location.street = streetDiv.text().trim();
        }

        Element cityDiv = addressDiv.getElementsByAttributeValue("itemprop", "addressLocality").first();
        if (cityDiv != null) {
            location.city = cityDiv.text().trim();
        }

        Element postalDiv = addressDiv.getElementsByAttributeValue("itemprop", "postalCode").first();
        if (postalDiv != null) {
            location.postalCode = postalDiv.text().trim();
        }

        return location;
    }
}
