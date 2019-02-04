package cz.danakut.fill_a_db;

public enum CourseType {
    WORKSHOP,
    DLOUHODOBY,
    INTENZIVNI,
    AKCE,
    NEURCENO;

    public String toDatabaseString() {

        switch (this) {
            case WORKSHOP: return "workshop";
            case DLOUHODOBY: return "dlouhodoby";
            case INTENZIVNI: return "intenzivni";
            default: return null;
        }
    }

}
