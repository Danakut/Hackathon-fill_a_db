package cz.danakut.fill_a_db;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

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
    String quickLocation;
    Location location;
    List<String> instructors;
    String link;
    String description;
    Timestamp lastUpdate;

    public Course() {

    }

    @Override
    public String toString() {
        String result = id + "|" + type + "|" + startDate + "|" + endDate + "|" + startTime + "|" + endTime + "|" + topic + "|" +
                knowledgeLevel + "|" + name + "|" + status + "|" + quickLocation + "|" + location + "|" + instructors + "|" +
                link + "|" + description + "|" + lastUpdate + "||";

        return result;
    }

    public String toLongString() {
        String result = topic + ", úroveň " + knowledgeLevel + ", název akce: ";

        result += name + "\n";


        result += type + ", ";

        if (type == CourseType.WORKSHOP || type == CourseType.AKCE) {
            result += "datum: " + startDate + ", " + startTime + " - " + endTime + "\n";
        } else {
            result += "od: " + startDate + ", do: " + endDate + "\n";
            result += "čas: " + startTime + " - " + endTime + "\n";
        }

        result += "místo konání: " + quickLocation + ", ";

        result += "lektor: " + instructors + "\n";

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
                && this.quickLocation.equals(thatObj.quickLocation)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int nameHash = name.hashCode();
        int dateHash = startDate.hashCode();
        int locationHash = quickLocation.hashCode();
        return nameHash * dateHash * locationHash;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CourseType getType() {
        return type;
    }

    public void setType(CourseType type) {
        this.type = type;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getKnowledgeLevel() {
        return knowledgeLevel;
    }

    public void setKnowledgeLevel(int knowledgeLevel) {
        this.knowledgeLevel = knowledgeLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public String getQuickLocation() {
        return quickLocation;
    }

    public void setQuickLocation(String quickLocation) {
        if (quickLocation.length() > 50) {
            this.quickLocation = quickLocation.substring(0, 50);
        } else {
            this.quickLocation = quickLocation;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<String> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<String> instructors) {
        this.instructors = instructors;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
