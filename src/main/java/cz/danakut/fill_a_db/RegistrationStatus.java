package cz.danakut.fill_a_db;

public enum RegistrationStatus {
    OTEVRENA,
    UZAVRENA,
    POZDEJI;

    public static RegistrationStatus fromString(String statusString) {

        switch (statusString) {

            case "konecRegistrace": return UZAVRENA;
            case "registraceOtevrena": return OTEVRENA;
            case "dejteMiVedet": return POZDEJI;
            default: throw new IllegalArgumentException("Unknown status: " + statusString);
        }
    }

    }
