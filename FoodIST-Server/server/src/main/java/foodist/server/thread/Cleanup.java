package foodist.server.thread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Cleanup implements Runnable {
	
	private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
	public Cleanup() {
		
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
				
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				
				String cDateString = this.determineDeleteDate(year, month, day,
						hour, minute, second);
				Date cdate = this.parseDate(cDateString);
				
				String dDateString = this.determineDeleteDate(year, month, day,
						23, 59, 59);
				Date ddate = this.parseDate(dDateString);
				
				delete_wait = ddate.getTime() - cdate.getTime();
			} catch (InterruptedException ie) {
				
			} catch (ParseException pe) {				
			}
			
		}
	}	
	
	private Date parseDate(String date) throws ParseException {		
		return new SimpleDateFormat(PATTERN).parse(date);
	}
	
	private String determineDeleteDate(int year, int month, int day, int hours, int minutes, int seconds) {
		return year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds;
	}
	
}
