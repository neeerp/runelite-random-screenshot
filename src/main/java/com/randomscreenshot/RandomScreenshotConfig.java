package com.randomscreenshot;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(RandomScreenshotPlugin.CONFIG_GROUP_KEY)
public interface RandomScreenshotConfig extends Config
{
	@ConfigItem(
		keyName = "sampleWeight",
		name = "Sample Weight",
		description = "The average number of ticks until a screenshot is taken. A game tick occurs every 0.6 seconds, and there are 100 ticks in a minute."
	)
	@Range(min = 1)
	default int sampleWeight() {
		return 500;
	}
}
