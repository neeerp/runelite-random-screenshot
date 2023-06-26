package com.randomscreenshot;

import com.google.inject.Provides;
import java.util.Random;
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
	private RandomScreenshotConfig config;

	@Inject
	private ScreenshotUtil screenShotUtil;

	private final Random rand = new Random(System.currentTimeMillis());


	@Provides
	RandomScreenshotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomScreenshotConfig.class);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (shouldTakeScreenshot())
		{
			takeScreenshot();
		}
	}

	private boolean shouldTakeScreenshot() {
		return rand.nextInt(config.sampleWeight()) == 0;
	}

	private void takeScreenshot() {
		screenShotUtil.takeScreenshot("", "Random Screenshots");
	}
}
