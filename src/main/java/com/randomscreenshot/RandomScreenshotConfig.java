package com.randomscreenshot;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(RandomScreenshotPlugin.CONFIG_GROUP_KEY)
public interface RandomScreenshotConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "sampleWeight",
		name = "Sample Weight",
		description = "The average number of ticks until a screenshot is taken. A game tick occurs every 0.6 seconds, and there are 100 ticks in a minute."
	)
	@Range(min = 1)
	default int sampleWeight() {
		return 500;
	}

	@ConfigItem(
		position = 1,
		keyName = "useDiscordWebhook",
		name = "Post to Discord Webhook",
		description = "Send screenshots to a Discord Webhook"
	)
	default boolean useDiscordWebhook() {
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "discordWebhookUrl",
		name = "Discord Webhook URL",
		description = "A Discord Webhook URL to post screenshots to as they're taken"
	)
	default String discordWebhookUrl() {
		return "";
	}
}
