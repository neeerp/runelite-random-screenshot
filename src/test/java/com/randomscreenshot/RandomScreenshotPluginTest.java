package com.randomscreenshot;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.widgets.ComponentID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RandomScreenshotPluginTest {
  @Mock Client client;

  @Mock RandomScreenshotConfig config;

  @Mock ScreenshotUtil screenshotUtil;

  @Mock RandomUtil rand;

  @BeforeEach
  void setup() {
    Mockito.lenient().when(config.sampleWeight()).thenReturn(100);
    Mockito.lenient().when(rand.randInt(Mockito.any(Integer.class))).thenReturn(1);
    Mockito.lenient().doNothing().when(screenshotUtil).takeScreenshot();

    Mockito.lenient().when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
    Mockito.lenient()
        .when(client.getWidget(Mockito.eq(ComponentID.BANK_PIN_CONTAINER)))
        .thenReturn(null);
  }

  @InjectMocks RandomScreenshotPlugin plugin = new RandomScreenshotPlugin();

  @Test
  public void testShouldTakeScreenshotIfBankPinContainerNull() {
    Mockito.when(client.getWidget(Mockito.eq(ComponentID.BANK_PIN_CONTAINER))).thenReturn(null);
    Mockito.lenient().when(rand.randInt(Mockito.any(Integer.class))).thenReturn(0);
    Assertions.assertTrue(plugin.shouldTakeScreenshot());
  }
}
