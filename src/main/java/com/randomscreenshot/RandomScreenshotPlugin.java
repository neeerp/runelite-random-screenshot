package com.randomscreenshot;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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

	// TODO: Instead of injection, this could be chosen dynamically via config.
	@Inject
	private ScreenshotStrategy screenshotStrategy;

	@Provides
	RandomScreenshotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomScreenshotConfig.class);
	}

	@Subscribe
	public void onGameTick()
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
