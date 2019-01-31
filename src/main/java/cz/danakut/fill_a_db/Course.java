package cz.danakut.fill_a_db;

import java.sql.Date;

public class Course {

    int id;
    CourseType type;
    Date startDate;
    Date endDate;
    String startTime;
    String endTime;
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


        result += type + ", ";

        if (type == CourseType.WORKSHOP || type == CourseType.AKCE) {
            result += "datum: " + startDate + ", " + startTime + " - " + endTime + "\n";
        } else {
            result += "od: " + startDate + ", do: " + endDate + "\n";
            result += "čas: " + startTime + " - " + endTime + "\n";
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
                && this.startTime.equals(thatObj.startTime)
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

}
