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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuickName() {
        return quickName;
    }

    public void setQuickName(String quickName) {
        this.quickName = quickName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
