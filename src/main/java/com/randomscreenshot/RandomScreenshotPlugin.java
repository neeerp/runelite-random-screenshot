package com.randomscreenshot;

import com.google.inject.Provides;
import java.util.Random;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
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

	@Inject
	private Client client;

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

	/* TODO: Create a decider interface so that decision strategy can be made modular. */
	private boolean shouldTakeScreenshot() {
		if (isBankPinContainerVisible() || isOnLoginScreen()) {
			return false;
		}

		return rand.nextInt(config.sampleWeight()) == 0;
	}

	private void takeScreenshot() {
		screenShotUtil.takeScreenshot();
	}

	private boolean isBankPinContainerVisible()
	{
		Widget pinContainer = client.getWidget(WidgetInfo.BANK_PIN_CONTAINER);
		if (pinContainer == null) {
			return true;
		}

		return !pinContainer.isSelfHidden();
	}

	private boolean isOnLoginScreen() {
		return client.getGameState() == GameState.LOGIN_SCREEN;
	}
}
