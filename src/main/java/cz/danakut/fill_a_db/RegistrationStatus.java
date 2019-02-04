package cz.danakut.fill_a_db;

public enum RegistrationStatus {
    OTEVRENA,
    UZAVRENA,
    POZDEJI,
    NETREBA,
    NEZJISTENO;

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
