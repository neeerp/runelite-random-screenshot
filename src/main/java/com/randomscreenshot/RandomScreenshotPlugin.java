/*
 * Copyright (c) 2018, Lotto <https://github.com/devLotto>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.randomscreenshot;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.SpriteID;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetID;
import static net.runelite.client.RuneLite.SCREENSHOT_DIR;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Screenshot",
	description = "Enable the manual and automatic taking of screenshots",
	tags = {"external", "images", "imgur", "integration", "notifications"}
)
@Slf4j
public class RandomScreenshotPlugin extends Plugin
{
	@Inject
	private RandomScreenshotConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RandomScreenshotOverlay randomScreenshotOverlay;

	@Inject
	private Client client;

	@Inject
	private DrawManager drawManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ImageCapture imageCapture;

	@Getter(AccessLevel.PACKAGE)
	private BufferedImage reportButton;

	private Random rand = new Random(0);


	@Provides
	RandomScreenshotConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RandomScreenshotConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(randomScreenshotOverlay);
		SCREENSHOT_DIR.mkdirs();

		spriteManager.getSpriteAsync(SpriteID.CHATBOX_REPORT_BUTTON, 0, s -> reportButton = s);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(randomScreenshotOverlay);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (rand.nextInt(1000) > 900)
		{
			takeScreenshot("", null);
		}
	}

	/**
	 * Saves a screenshot of the client window to the screenshot folder as a PNG,
	 * and optionally uploads it to an image-hosting service.
	 *
	 * @param fileName Filename to use, without file extension.
	 * @param subDir   Subdirectory to store the captured screenshot in.
	 */
	@VisibleForTesting
	void takeScreenshot(String fileName, String subDir)
	{
		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			// Prevent the screenshot from being captured
			log.info("Login screenshot prevented");
			return;
		}

		Consumer<Image> imageCallback = (img) ->
		{
			// This callback is on the game thread, move to executor thread
			executor.submit(() -> takeScreenshot(fileName, subDir, img));
		};

		drawManager.requestNextFrameListener(imageCallback);
	}

	private void takeScreenshot(String fileName, String subDir, Image image)
	{
		imageCapture.takeScreenshot(ImageUtil.bufferedImageFromImage(image), fileName, subDir, false, config.uploadScreenshot());
	}
}
