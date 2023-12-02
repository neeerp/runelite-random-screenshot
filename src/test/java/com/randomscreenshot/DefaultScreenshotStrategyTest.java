package com.randomscreenshot;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultScreenshotStrategyTest
{
	@Mock
	Client client;

	@Mock
	RandomScreenshotConfig config;

	@Mock
	RandomUtil rand;

	@Mock
	Widget widget;

	@InjectMocks
	DefaultScreenshotStrategy strategy;


	@Test
	public void testShouldTakeScreenshotIfBankPinContainerNull() {
		Mockito.when(client.getWidget(Mockito.eq(ComponentID.BANK_PIN_CONTAINER))).thenReturn(null);
		Mockito.when(rand.randInt(Mockito.any(Integer.class))).thenReturn(0);

		Assertions.assertTrue(strategy.shouldTakeScreenshot());
	}

	@Test
	public void testShouldTakeScreenshotIfBankPinContainerNotVisible() {
		Mockito.when(client.getWidget(Mockito.eq(ComponentID.BANK_PIN_CONTAINER))).thenReturn(widget);
		Mockito.when(widget.isSelfHidden()).thenReturn(true);
		Mockito.lenient().when(rand.randInt(Mockito.any(Integer.class))).thenReturn(0);

		Assertions.assertTrue(strategy.shouldTakeScreenshot());
	}

	@Test
	public void testShouldNotTakeScreenshotIfBankPinContainerVisible() {
		Mockito.when(client.getWidget(Mockito.eq(ComponentID.BANK_PIN_CONTAINER))).thenReturn(widget);
		Mockito.when(widget.isSelfHidden()).thenReturn(false);
		Mockito.lenient().when(rand.randInt(Mockito.any(Integer.class))).thenReturn(0);

		Assertions.assertFalse(strategy.shouldTakeScreenshot());
	}

	@Test
	public void testShouldNotTakeScreenshotIfOnLoginScreen() {
		Mockito.when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		Mockito.lenient().when(rand.randInt(Mockito.any(Integer.class))).thenReturn(0);

		Assertions.assertFalse(strategy.shouldTakeScreenshot());
	}
}
