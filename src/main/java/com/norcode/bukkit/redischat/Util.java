package com.norcode.bukkit.redischat;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
	public static Pattern TIMEDELTA_PATTERN = Pattern.compile("(\\d+)\\s?([dhms])", Pattern.CASE_INSENSITIVE);
	public static long timeDeltaToMillis(String s) {
		Matcher m = TIMEDELTA_PATTERN.matcher(s);
		long millis = 0;
		TimeUnit unit;
		while (m.find()) {
			if (m.group(2).toLowerCase().equals("d")) {
				unit = TimeUnit.DAYS;
			} else if (m.group(2).toLowerCase().equals("h")) {
				unit = TimeUnit.HOURS;
			} else if (m.group(2).toLowerCase().equals("m")) {
				unit = TimeUnit.MINUTES;
			} else if (m.group(2).toLowerCase().equals("s")) {
				unit = TimeUnit.SECONDS;
			} else {
				continue;
			}
			millis += unit.toMillis(Long.parseLong(m.group(1)));
		}
		return millis;
	}
}
