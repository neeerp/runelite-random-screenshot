package com.randomscreenshot;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(RandomScreenshotPlugin.CONFIG_GROUP_KEY)
public interface RandomScreenshotConfig extends Config
{
	@ConfigSection(
		name = "Custom Directory",
		description = "Custom setting for where to save screenshots",
		position = 99
	)
	String customDirectorySection = "Custom Directory";

	@ConfigItem(
		keyName = "sampleWeight",
		name = "Sample Weight",
		description = "The average number of ticks until a screenshot is taken. A game tick occurs every 0.6 seconds, and there are 100 ticks in a minute."
	)
	@Range(min = 1)
	default int sampleWeight() {
		return 500;
	}

	/* TODO: Ideally we'd use a file picker here, but the file picker is not available to the plugin config panel and
	    making it available requires changes to core runelite. */
	@ConfigItem(
		keyName = "screenshotDirectory",
		name = "Custom Screenshot Directory",
		description = "Custom directory to save screenshots to",
		section = customDirectorySection,
		position = 1
	)
	default String screenshotDirectory() {
		return "";
	}

	@ConfigItem(
		keyName = "useCustomDirectory",
		name = "Use Custom Screenshot Directory",
		description = "Save screenshots to the configured custom directory",
		warning =
			"When enabling this option, make sure that the directory you've configured is valid! " +
			"If it isn't, screenshots will be lost!\n\n" +
			"Also note: if you sync your RuneLite settings across multiple computers, " +
			"please make sure the given directory is valid on all your computers!\n\n" +
			"You can disregard this message if you are just disabling this option.",
		section = customDirectorySection,
		position = 2
	)
	default boolean useCustomDirectory() {
		return false;
	}
}
