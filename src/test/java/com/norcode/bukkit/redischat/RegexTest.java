package com.norcode.bukkit.redischat;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class RegexTest {

	@Test
	public void testLinkifyChat() {
		Assert.assertEquals(
				Util.timeDeltaToMillis("1h10m"),
				TimeUnit.MINUTES.toMillis(70)
		);
		Assert.assertEquals(
				Util.timeDeltaToMillis("1d2h11m"),
				TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(2) + TimeUnit.MINUTES.toMillis(11)
		);
	}
}
