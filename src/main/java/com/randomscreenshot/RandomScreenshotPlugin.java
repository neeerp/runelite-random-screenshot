package com.randomscreenshot;

import com.google.inject.Provides;
import java.util.Random;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import static net.runelite.client.RuneLite.SCREENSHOT_DIR;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Random Screenshot",
	description = "Randomly take screenshots as you go about your adventures",
	tags = {"screenshot", "images", "random"}
)
@Slf4j
public class RandomScreenshotPlugin extends Plugin
{
	@Inject
	private RandomScreenshotConfig config;

	@Inject
	private ScreenShotUtil screenShotUtil;

	private Random rand = new Random(0);


	@Provides
	RandomScreenshotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomScreenshotConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		SCREENSHOT_DIR.mkdirs();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (rand.nextInt(1000) > 900)
		{
			screenShotUtil.takeScreenshot("", null);
		}
	}
}
