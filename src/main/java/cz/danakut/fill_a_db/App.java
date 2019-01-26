package cz.danakut.fill_a_db;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    static String url = "https://www.czechitas.cz/cs/kalendar-akci#views:view=jplist-list-view";
    static Pattern pattern = Pattern.compile("\\d{1,2}:\\d{1,2}");

    public static void main(String[] args) {

        Document document = null;

        try {
            document = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Elements calendarEvents = document.select(".list-item"); //ArrayList
        for (Element calendarEvent : calendarEvents) {
//        Element calendarEvent = calendarEvents.get(31);
            Attributes attributes = calendarEvent.attributes();
            Course newCourse = new Course();

            String typeString = calendarEvent.selectFirst(".intesity span").text();
            newCourse.type = CourseType.fromString(typeString);

            String date = calendarEvent.selectFirst(".date").text();
            String[] dateArray = date.split("/");
            newCourse.startDate = LocalDate.of
                    (Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]));

            Element duration = calendarEvent.selectFirst(".day");
            if (duration.is(".single")) {
                newCourse.endDate = null;

            } else if (duration.is(".multi")) {
                String dayString = duration.select(".dday").last().text().trim();
                int day = Integer.parseInt(dayString.substring(0, dayString.length() - 1));
                String[] monthyear = duration.select(".dmonth").last().text().trim().split(" ");
                int month = MonthConverter.fromString(monthyear[0].substring(0, monthyear[0].length() - 1));
                int year = Integer.parseInt(monthyear[1]);
                newCourse.endDate = LocalDate.of(year, month, day);
            }

            Element dayAndHoursDiv = duration.selectFirst(".hours");
        if (dayAndHoursDiv != null) {
            String dayAndHours[] = dayAndHoursDiv.text().trim().split(",");
            Matcher matcher = pattern.matcher(dayAndHours[1]);
            matcher.find();
            String[] startTime = matcher.group().split(":");
            newCourse.startTime = LocalTime.of(Integer.parseInt(startTime[0]), Integer.parseInt(startTime[1]));
            matcher.find();
            String[] endTime = matcher.group().split(":");
            newCourse.endTime = LocalTime.of(Integer.parseInt(endTime[0]), Integer.parseInt(endTime[1]));
        } else {
            newCourse.startTime = null;
            newCourse.endTime = null;
        }

        String topic = attributes.get("class").substring(16);
        newCourse.setTopic(topic);

            String levelInfo = calendarEvent.selectFirst(".eventDifficulty").className();
            String levelString = levelInfo.substring(16,levelInfo.length() - 6 );
            newCourse.setKnowledgeLevel(levelString);

            newCourse.name = calendarEvent.selectFirst(".eventName div").text();

            String[] registrationInfo = calendarEvent.selectFirst(".eventStatus").className().split("\\s+");
            newCourse.status = RegistrationStatus.fromString(registrationInfo[1]);

            newCourse.location = calendarEvent.selectFirst(".eventPlace div").text();

            newCourse.instructor = "";

            newCourse.link = attributes.get("href");

            String courseDescription = calendarEvent.selectFirst(".eventDesc").text();
            courseDescription.trim();

            System.out.println("Zpracováno: položka č. " + calendarEvents.indexOf(calendarEvent));
        }



    }

}
