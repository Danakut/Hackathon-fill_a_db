package cz.danakut.fill_a_db;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Course {

    int id;
    CourseType type;
    LocalDate startDate;
    LocalDate endDate;
    LocalTime startTime;
    LocalTime endTime;
    String topic;
    int knowledgeLevel;
    String name;
    RegistrationStatus status;
    String location;
    String instructor;
    String link;
    String description;
    Date lastUpdate;

    public Course() {

    }

    @Override
    public String toString() {
        String result = topic + ", úroveň " + knowledgeLevel + ", název akce: ";

        result += name + "\n";

        if (type != null) {
            result += type + ", ";

            if (type == CourseType.WORKSHOP || type == CourseType.AKCE) {
                result += "datum: " + startDate + ", " + startTime + " - " + endTime + "\n";
            } else {
                result += "od: " + startDate + ", do: " + endDate + "\n";
                result += "čas: " + startTime + " - " + endTime + "\n";
            }
        } else {
            result += "type NENI";
        }

        result += "místo konání: " + location + ", ";

        result += "lektor: " + instructor + "\n";

        result += "odkaz na web: " + link + "\n";

        result += "stav registrace: " + status + "\n";

        result += "poslední update: " + lastUpdate + "\n \n";

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        Course thatObj = null;

        if (!(this.getClass().isInstance(obj.getClass()))) {
            return false;
        } else {
            thatObj = (Course) obj;
        }

        if ((this.name.equals(thatObj.name))
                && this.startDate.equals(thatObj.startDate)
                && (this.startTime == thatObj.startTime)
                && this.location.equals(thatObj.location)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int nameHash = name.hashCode();
        int dateHash = startDate.hashCode();
        int locationHash = location.hashCode();
        return nameHash * dateHash * locationHash;
    }

    public void setTopic(String topicFromWeb) {
        String result;
        switch (topicFromWeb) {
            case "programuju": result =  "Programuju";
            case "tvorimWeb": result =  "Tvořím web" ;
            case "akademieProgramovani": result =  "Czechitas Nová Generace" ;
            case "dalsi": result =  "Další";
            case "milujuData": result =  "DataGirls";
            case "testuju": result =  "Testuju";
            case "jsemDigitalni": result =  "Jsem digitální!" ;
            case "digitalAcademy": result =  "Digitální akademie" ;
            default: result =  "";
        }

        this.topic = result;
    }

    public void setKnowledgeLevel(String level) {
        int result;
        switch (level) {
            case "zadne": result = 0;
            case "zacatecnik": result = 1;
            case "pokrocily": result = 2 ;
            default: result = 3;
        }

        this.knowledgeLevel = result;
    }

}
