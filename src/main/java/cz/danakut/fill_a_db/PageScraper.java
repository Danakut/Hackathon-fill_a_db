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

    Elements scrapedElements;

    public PageScraper() {

    }

    public PageScraper(String urlToParse) {
        scrapedElements = getElementsforScraping(urlToParse);
    }

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
        Element duration = calendarEvent.selectFirst(".day");

        Course newCourse = new Course();
        newCourse.name = scrapeName(calendarEvent);
        newCourse.startDate = scrapeStartDay(calendarEvent);
        newCourse.startTime = scrapeHours(duration)[0];
        /*status is moved to partial scraping phase so that reg status can be checked and updated without needing to scrape
        a course as a whole when it is already stored in db*/
        newCourse.status = scrapeStatus(calendarEvent);
        newCourse.quickLocation = scrapeQuickLocation(calendarEvent);


        return newCourse;
    }

    public Course scrapeCourseWhole(Course partiallyScrapedCourse, Element calendarEvent) {
        Attributes attributes = calendarEvent.attributes();
        Element duration = calendarEvent.selectFirst(".day");

        Course newCourse = partiallyScrapedCourse;

        newCourse.type = scrapeType(calendarEvent);
        newCourse.endDate = scrapeEndDay(duration);
        newCourse.endTime = scrapeHours(duration)[1];
        newCourse.topic = scrapeTopic(calendarEvent);
        newCourse.knowledgeLevel = scrapeKnowledgeLevel(calendarEvent);
        newCourse.link = scrapeLink(attributes);

        Document parsedDocument = null;
        try {
            parsedDocument = Jsoup.connect(newCourse.link).get();
            newCourse.location = scrapeLocation(parsedDocument, newCourse.quickLocation);
            newCourse.instructors = scrapeInstructor(parsedDocument);
            newCourse.description = scrapeDescription(parsedDocument);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Scraped: " + newCourse.name + ", " + newCourse.startDate);
        return newCourse;
    }

    private CourseType scrapeType(Element calendarEvent) {
        String type = calendarEvent.selectFirst(".intesity").text();

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

    private Date scrapeStartDay(Element calendarEvent) {
        String string = calendarEvent.selectFirst(".date").text();
        // convert the string to a proper format for Date.valuoOf()
        return Date.valueOf(string.replaceAll("/", "-"));
    }

    private Date scrapeEndDay(Element duration) {
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

    private String[] scrapeHours(Element duration) {
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

    private String scrapeTopic(Element calendarEvent) {
        return calendarEvent.selectFirst(".grid-icon span").text().trim();
    }

    private int scrapeKnowledgeLevel(Element calendarEvent) {
        String levelInfo = calendarEvent.selectFirst(".eventDifficulty").className();
        String level = levelInfo.substring(16,levelInfo.length() - 6 );
        int result;
        switch (level) {
            case "zadne":
                result = 0;
                break;
            case "zacatecnik":
                result = 1;
                break;
            case "pokrocily":
                result = 2 ;
                break;
            default: result = 3;
        }

        return result;
    }

    private String scrapeName(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventName div").text().trim();
    }

    private RegistrationStatus scrapeStatus(Element calendarEvent) {
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

    private String scrapeQuickLocation(Element calendarEvent) {
        return calendarEvent.selectFirst(".eventPlace div").text().trim();
    }

    private String scrapeLink(Attributes attributes) {
        return attributes.get("href");
    }

    private String scrapeDescription(Document parsedDocument) {
        Element desc = parsedDocument.selectFirst(".event-descp");
        if (desc != null) {
            return desc.text().trim();
        } else return "";
    }

    private Location scrapeLocation(Document parsedDocument, String quickLocation) {
        Location location = new Location();
        location.quickName = quickLocation;

        Element addressDiv = parsedDocument.getElementsByAttributeValue("itemtype", "http://schema.org/Organization").first();

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

    private List<String> scrapeInstructor(Document parsedDocument) {
        List<String> list = new ArrayList<>();

        Element section = parsedDocument.selectFirst(".tym");
        if (section != null) {
            Elements instructorsP = section.select(".user-name");
            if (instructorsP != null) {
                for (Element instructor : instructorsP) {
                    Element function = instructor.selectFirst(".user-funkce");
                    if (function != null) {
                        String functionString = function.text().trim();
                        if ( (functionString.contains("Lecturer")) || (functionString.contains("Lektorka") || (functionString.contains("Lektor")) )) {
                            list.add(instructor.ownText().trim());
                        }
                    }
                }
            }
        }

        return list;
    }
}
