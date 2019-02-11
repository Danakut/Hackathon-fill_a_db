package cz.danakut.fill_a_db;

public class Location {
    int id;
    String quickName;
    String name;
    String street;
    String city;
    String postalCode;

    @Override
    public String toString() {
        if (quickName == null) {
            quickName = "[quickname]";
        }
        if (name == null) {
            name = "[name]";
        }
        if (street == null) {
            street = "[street]";
        }
        if (city == null) {
            city = "[city]";
        }
        if (postalCode == null) {
            postalCode = "[postalCode]";
        }
        return "ID: " + id + ", value: (" + quickName + ") " + name + ", " + street + ", " + city + ", " + postalCode;
    }
}
