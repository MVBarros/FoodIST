package pt.ulisboa.tecnico.cmov.foodist.domain;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class FoodService {

    public enum Language {
        name_en,
        name_pt
    }

    private static String TAG = "FOOD-SERVICE-TAG";

    private Map<Language, String> names;
    private String distance;
    private String time;
    private double latitude;
    private double longitude;
    private Map<String, Map<String, String>> hours;

    public FoodService(Map<Language, String> names, String distance, String time, double latitude,
                       double longitude, Map<String, Map<String, String>> hours) {
        this.names = names;
        this.distance = distance;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hours = hours;
    }

    public String getName() {
        return names.get(Language.name_pt);
    }

    public String getName(Language language) {
        return names.get(language);
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<String, String> getHours(String role) {
        return hours.get(role);
    }

    public String getHoursForToday(String role) {
        return hours.get(role).get(weekdayIntToString(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
    }

    public boolean isFoodServiceAvailable(String role) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:MM");

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.setTime(currentDate);

        String currentHours = dateFormat.format(currentDate);
        String currentWeekday = this.weekdayIntToString(calendar.get(Calendar.DAY_OF_WEEK));

        String functioningHours = this.getHours(role).get(currentWeekday);

        if (functioningHours == null || functioningHours.equals("closed")) {
            return false;
        }
        return this.isTimeInRange(currentHours, functioningHours);
    }

    private String weekdayIntToString(int weekday) {
        switch (weekday) {
            case Calendar.SUNDAY:
                return "sunday";
            case Calendar.MONDAY:
                return "monday";
            case Calendar.TUESDAY:
                return "tuesday";
            case Calendar.WEDNESDAY:
                return "wednesday";
            case Calendar.THURSDAY:
                return "thursday";
            case Calendar.FRIDAY:
                return "friday";
            case Calendar.SATURDAY:
                return "saturday";
            default:
                Log.e(TAG, "Invalid week day inserted");
                return "sunday";
        }
    }

    private boolean isTimeInRange(String time, String range) {
        return time.compareTo(range.substring(0, 5)) >= 0
                && time.compareTo(range.substring(6)) <= 0;
    }


}
