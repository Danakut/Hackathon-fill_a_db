package cz.danakut.fill_a_db;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageScraper {

    static Pattern hoursPattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public List<Course> parsePage(String url) {

        List<Course> list = new ArrayList<>();
        Document parsedDocument = null;

        try {
            parsedDocument = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Elements calendarEvents = parsedDocument.select(".list-item"); //ArrayList
        for (Element calendarEvent : calendarEvents) {
//            Element calendarEvent = calendarEvents.get(4);
            Course newCourse = new Course();
            Attributes attributes = calendarEvent.attributes();

            newCourse.type = scrapeType(calendarEvent);

            newCourse.startDate = scrapeStartDay(calendarEvent);

            Element duration = calendarEvent.selectFirst(".day");
            newCourse.endDate = scrapeEndDay(duration);
            newCourse.startTime = scrapeHours(duration)[0];
            newCourse.endTime = scrapeHours(duration)[1];

            newCourse.topic = scrapeTopic(attributes);

            newCourse.knowledgeLevel = scrapeKnowledgeLevel(calendarEvent);

            newCourse.name = scrapeName(calendarEvent);

            newCourse.status = scrapeStatus(calendarEvent);

            newCourse.location = scrapeLocation(calendarEvent);

            newCourse.instructor = "";

            newCourse.link = scrapeLink(attributes);

            newCourse.description = scrapeDescription(calendarEvent);

            list.add(newCourse);
            System.out.println("Zpracováno: položka č. " + calendarEvents.indexOf(calendarEvent));
        }

        return list;
    }

    private static CourseType scrapeType(Element calendarEvent) {
        String typeString = calendarEvent.selectFirst(".intesity span").text();
        return CourseType.fromString(typeString);
    }

    private static Date scrapeStartDay(Element calendarEvent) {
        String string = calendarEvent.selectFirst(".date").text();
        // convert the string to a proper format for Date.valuoOf()
        return Date.valueOf(string.replaceAll("/", "-"));

    }

    private static Date scrapeEndDay(Element duration) {
        if (duration.is(".single")) {
            return null;

        } else if (duration.is(".multi")) {
            String dayString = duration.select(".dday").last().text().trim();
            String day = dayString.substring(0, dayString.length() - 1);
            String[] monthyear = duration.select(".dmonth").last().text().trim().split(" ");
            String month = monthyear[0].substring(0, monthyear[0].length() - 1);
            String complete = monthyear[1] + "-" + MonthConverter.fromString(month) + "-" + day;
            return Date.valueOf(complete);
        }

        return null;
    }

    private static String[] scrapeHours(Element duration) {
        String[] hours = new String[2];

        Element dayAndHoursDiv = duration.selectFirst(".hours");
        if (dayAndHoursDiv != null) {
            String dayAndHours[] = dayAndHoursDiv.text().trim().split(",");
            Matcher matcher = hoursPattern.matcher(dayAndHours[1]);
            matcher.find();
            hours[0] = matcher.group();
            matcher.find();
            hours[1] = matcher.group();
        }

        return hours;
    }

    private static String scrapeTopic(Attributes attributes) {
        return attributes.get("class").substring(16);
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
        return calendarEvent.selectFirst(".eventName div").text();
    }

    private static RegistrationStatus scrapeStatus(Element calendarEvent) {
        String[] registrationInfo = calendarEvent.selectFirst(".eventStatus").className().split("\\s+");
        return RegistrationStatus.fromString(registrationInfo[1]);
    }

    private static String scrapeLocation(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventPlace div").text();
    }

    private static String scrapeLink(Attributes attributes) {
        return attributes.get("href");
    }

    private static String scrapeDescription(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventDesc").text().trim();
    }
}
