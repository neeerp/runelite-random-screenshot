package com.randomscreenshot;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Random Screenshot",
	description = "Randomly take screenshots as you go about your adventures",
	tags = {"screenshot", "images", "random", "memories"}
)
@Slf4j
public class RandomScreenshotPlugin extends Plugin
{
	static final String CONFIG_GROUP_KEY = "randomscreenshot";

	@Inject
	private ScreenshotUtil screenShotUtil;

	// TODO: Ideally, this should be chosen via a configuration, and not rely on the concrete implementation.
	@Inject
	private DefaultScreenshotStrategy screenshotStrategy;

	@Provides
	RandomScreenshotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomScreenshotConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (screenshotStrategy.shouldTakeScreenshot())
		{
			takeScreenshot();
		}
	}

	private void takeScreenshot() {
		screenShotUtil.takeScreenshot();
	}
}
