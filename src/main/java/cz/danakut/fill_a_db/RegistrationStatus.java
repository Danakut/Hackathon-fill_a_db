package cz.danakut.fill_a_db;

public enum RegistrationStatus {
    OTEVRENA,
    UZAVRENA,
    POZDEJI,
    NETREBA;

    public static RegistrationStatus fromString(String statusString) {

        switch (statusString) {

            case "konecRegistrace": return UZAVRENA;
            case "registraceOtevrena": return OTEVRENA;
            case "dejteMiVedet": return POZDEJI;
            case "bezRegistrace": return NETREBA;
            default: throw new IllegalArgumentException("Unknown status: " + statusString);
        }
    }

    public String toDatabaseString() {

        switch (this) {
            case OTEVRENA: return "otevrena";
            case UZAVRENA: return "uzavrena";
            case POZDEJI: return "pozdeji";
            case NETREBA: return "netreba";
            default: return null;
        }
    }

}
