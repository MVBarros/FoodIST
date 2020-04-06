package foodist.server.thread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Cleanup implements Runnable {
	
	private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	private int cleanup_hour;
	private int cleanup_minute;
	private int cleanup_second;
	private int cleanup_milli;
	
	public Cleanup(int cleanup_hour, int cleanup_minute, int cleanup_second, int cleanup_milli) {
		this.cleanup_hour = cleanup_hour;
		this.cleanup_minute = cleanup_minute;
		this.cleanup_second = cleanup_second;
		this.cleanup_milli = cleanup_milli;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		long delete_wait = 0;
		
		while(true) {						
			try {
				Thread.sleep(delete_wait);
				
				Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				int second = calendar.get(Calendar.SECOND);
				int millisecond = calendar.get(Calendar.MILLISECOND);
				
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;
				int day = calendar.get(Calendar.DAY_OF_MONTH);				
				
				String cDateString = this.determineDeleteDate(year, month, day,
						hour, minute, second, millisecond);
				Date cdate = this.parseDate(cDateString);								
				
				String dDateString = this.determineDeleteDate(year, month, day,
						this.cleanup_hour, this.cleanup_minute, this.cleanup_second, this.cleanup_second);
				Date ddate = this.parseDate(dDateString);
				
				delete_wait = ddate.getTime() - cdate.getTime();
			} catch (InterruptedException ie) {
				System.out.println("Cleanup thread has been interrupted abruptly");
			} catch (ParseException pe) {
				System.out.println("Unable to parse current date");
			}		
		}
		
	}	
	
	private Date parseDate(String date) throws ParseException {		
		return new SimpleDateFormat(PATTERN).parse(date);
	}
	
	private String determineDeleteDate(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds) {
		return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds + ":" + milliseconds;
	}
	
}
