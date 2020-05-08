package pt.ulisboa.tecnico.cmov.foodist.domain;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import foodist.server.grpc.contract.Contract;

public class FoodService {


    private static String TAG = "FOOD-SERVICE-TAG";

    private Map<String, String> names;
    private String distance;
    private String time;
    private double latitude;
    private double longitude;
    private Map<String, Map<String, List<String>>> hours;
    private Set<String> constraints;
    private String beacon;

    public FoodService(Map<String, String> names, String distance, String time, double latitude,
                       double longitude, Map<String, Map<String, List<String>>> hours, Set<String> constraints, String beacon) {
        this.names = names;
        this.distance = distance;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hours = hours;
        this.constraints = constraints;
        this.beacon = beacon;
    }

    public String getName() {
        return names.get("pt");
    }

    public String getName(String language) {
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

    public Map<String, List<String>> getHours(String role) {
        return hours.get(role);
    }

    public String getHoursForToday(String role) {
        return this.hoursToString(hours.get(role).get(weekdayIntToString(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))));
    }

    public Set<String> getConstraints() {
        return constraints;
    }

    public String getBeacon() {
        return beacon;
    }

    public boolean isFoodServiceAvailable(String role) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.US);

        Calendar calendar = Calendar.getInstance();
        Date currentDate = calendar.getTime();

        calendar.setTime(currentDate);

        String currentHours = dateFormat.format(currentDate);
        String currentWeekday = this.weekdayIntToString(calendar.get(Calendar.DAY_OF_WEEK));

        List<String> functioningHours = this.getHours(role).get(currentWeekday);

        if (functioningHours == null || functioningHours.equals("closed")) {
            return false;
        }
        return functioningHours.stream()
                .anyMatch(hour -> this.isTimeInRange(currentHours, hour));
    }

    public boolean isFoodServiceConstrained(Map<Contract.FoodType, Boolean> constraints) {
        return constraints.entrySet().stream().filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .map(Contract.FoodType::name)
                .anyMatch(this.constraints::contains);
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


    public void setTime(String time) {
        this.time = time;
    }

    private String hoursToString(List<String> hours) {
        StringBuilder builder = new StringBuilder();
        hours.forEach(hour -> builder.append(hour).append(" "));
        return builder.toString();
    }

}
