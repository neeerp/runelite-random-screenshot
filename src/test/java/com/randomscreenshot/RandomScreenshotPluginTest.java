package com.randomscreenshot;

import net.runelite.api.Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RandomScreenshotPluginTest
{
	@Mock
	Client client;

	@Mock
	RandomScreenshotConfig config;

	@Mock
	ScreenshotUtil screenshotUtil;

	@Mock
	ScreenshotStrategy screenshotStrategy;


	@InjectMocks
	RandomScreenshotPlugin plugin = new RandomScreenshotPlugin();


	@Test
	public void shouldTakeScreenshotWhenItShould() {
		Mockito.when(screenshotStrategy.shouldTakeScreenshot()).thenReturn(true);

		plugin.onGameTick();
		Mockito.verify(screenshotUtil, Mockito.times(1)).takeScreenshot();
	}

	@Test
	public void shouldNotTakeScreenshotWhenItShouldNot() {
		Mockito.when(screenshotStrategy.shouldTakeScreenshot()).thenReturn(false);

		plugin.onGameTick();
		Mockito.verify(screenshotUtil, Mockito.times(0)).takeScreenshot();
	}

}
