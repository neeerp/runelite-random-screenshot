package com.randomscreenshot;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;

@Singleton
public class DefaultScreenshotStrategy implements ScreenshotStrategy
{
	@Inject
	private RandomScreenshotConfig config;

	@Inject
	private Client client;

	@Inject
	private RandomUtil rand;

	public boolean shouldTakeScreenshot() {
		if (isBankPinContainerVisible() || isOnLoginScreen()) {
			return false;
		}

		return rand.randInt(config.sampleWeight()) == 0;
	}

	private boolean isBankPinContainerVisible()
	{
		Widget pinContainer = client.getWidget(ComponentID.BANK_PIN_CONTAINER);
		if (pinContainer == null) {
			return false;
		}

		return !pinContainer.isSelfHidden();
	}

	private boolean isOnLoginScreen()
	{
		return client.getGameState() == GameState.LOGIN_SCREEN;
	}
}
