package cz.danakut.fill_a_db;

public enum CourseType {
    WORKSHOP,
    DLOUHODOBY,
    INTENZIVNI,
    AKCE;


    public static CourseType fromString(String typeString) {

        switch (typeString) {
            case "Jednodenní": return WORKSHOP;
            case "Pravidelný": return DLOUHODOBY;
            case "Intenzivní": return INTENZIVNI;
            default: throw new IllegalArgumentException("Unknown status: " + typeString);
        }
    }

    public String toDatabaseString() {

        switch (this) {
            case WORKSHOP: return "workshop";
            case DLOUHODOBY: return "dlouhodoby";
            case INTENZIVNI: return "intenzivni";
            default: return null;
        }
    }

}
