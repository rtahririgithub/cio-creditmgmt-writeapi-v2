package com.telus.credit.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTimeUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);
    private DateTimeUtils() {
        // Utils
    }

    private static final DateTimeFormatter ISO_INSTANT = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendInstant(3).toFormatter();

    /**
     * Convert isoDate String to UTC timestamp value (the value aligned with UTC)
     *
     * @param isoDate
     * @return
     */
    public static Date toUtcDate(String isoDate) {
        if (StringUtils.isBlank(isoDate)) {
            return null;
        }
        try {
			TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_DATE.parse(isoDate);
			return Date.from(LocalDate.from(temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant());
		} catch (Exception e) {
			return toUtcDate2(isoDate);
		}
    }
    public static Date toUtcDate2(String isoDate) {
    	Date utcDate=null;
    	try {
    		utcDate = new SimpleDateFormat("yyyy-mm-dd").parse(isoDate);
		} catch (Exception e1) {
			try {
				utcDate = new SimpleDateFormat("dd-MMM-yy").parse(isoDate);
			} catch (Exception e) {}
		}
		return utcDate;
    }
    
    /**
     * Convert isodatetime String to UTC timestamp value (the value aligned with UTC)
     *
     * @param isoDatetime
     * @return
     */
    public static Timestamp toUtcTimestamp(String isoDatetime) {
        if (StringUtils.isBlank(isoDatetime)) {
            return null;
        }
        
        try {
			Instant instant = Instant.parse(isoDatetime);
			return new Timestamp(instant.minusSeconds(ZoneId.systemDefault().getRules().getOffset(instant).getTotalSeconds()).toEpochMilli());
		} catch (Exception e) {
    		try {
				Date dt = DateTimeUtils.toUtcDate2(isoDatetime);
				Timestamp ts=new Timestamp(dt.getTime());
				return ts;
			} catch (Exception e1) {
				return Timestamp.from(Instant.now());
			}
		}
    }

    /**
     * Convert datetime to String value in UTC timezone
     *
     * @param utcDatetime
     * @return
     */
    public static String toUtcString(Timestamp utcDatetime) {
        if (utcDatetime == null) {
            return null;
        }

        Instant instant = Instant.ofEpochMilli(utcDatetime.getTime());
        return ISO_INSTANT.format(instant.plusSeconds(ZoneId.systemDefault().getRules().getOffset(instant).getTotalSeconds()).atZone(ZoneId.of("UTC")));
    }
    
    /**
     * Convert datetime to String value in UTC timezone
     *
     * @param utcDatetime
     * @return
     */
    public static String toUtcString(Date utcDatetime) {
        if (utcDatetime == null) {
            return null;
        }

        Instant instant = Instant.ofEpochMilli(utcDatetime.getTime());
        return ISO_INSTANT.format(instant.plusSeconds(ZoneId.systemDefault().getRules().getOffset(instant).getTotalSeconds()).atZone(ZoneId.of("UTC")));
    }

    /**
     * Convert date to String value in UTC timezone
     *
     * @param date
     * @return
     */
    public static String toUtcDateString(Date date) {
        if (date == null) {
            return null;
        }

        return DateTimeFormatter.ISO_DATE.format(Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.of("UTC"))).replace("Z", "");
    }

	/**
	 * get the timestamp in milliseconds for incoming request.
	 **/
    public static long getRequestReceivedTimestampInMillis() {
    	long receivedTime = System.currentTimeMillis();
        return receivedTime;
    }    

    public static long getRequestReceivedEventTimeInMillis(String eventTimeAsStr) {
	  long currentTimeInMillis = System.currentTimeMillis();
	  SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	   // eventTimeAsStr example: 2021-05-26T00:34:20.274Z
	   // convert eventTime to long
	  long eventTimeInMillis = currentTimeInMillis;
	  if (!StringUtils.isBlank(eventTimeAsStr)) {
	     try {
	        eventTimeInMillis = df.parse(eventTimeAsStr).getTime();
	        if (eventTimeInMillis < 1) {
	           eventTimeInMillis = currentTimeInMillis;
	        }
	     } catch (Exception e) {
	        eventTimeInMillis = currentTimeInMillis;
	        LOGGER.warn("Unable to parse eventTimeAsStr {}, {}, using {}", eventTimeAsStr, e.getMessage(), eventTimeInMillis);
	     }   
	  }
	   return eventTimeInMillis;
	}   
}
