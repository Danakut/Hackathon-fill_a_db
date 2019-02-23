package cz.danakut.fill_a_db;

public class MonthConverter {

    public static String fromString(String monthString) {

        switch (monthString) {
            case "led": return "1";
            case "úno": return "2";
            case "bře": return "3";
            case "dub": return "4";
            case "kvě": return "5";
            case "čvn": return "6";
            case "čvc": return "7";
            case "srp": return "8";
            case "zář": return "9";
            case "říj": return "10";
            case "lis": return "11";
            case "pro": return "12";
            default: return "0";
        }
    }
}
