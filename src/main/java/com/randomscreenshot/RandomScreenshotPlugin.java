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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Point;
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
import net.runelite.client.util.OSType;

@PluginDescriptor(
	name = "Screenshot",
	description = "Enable the manual and automatic taking of screenshots",
	tags = {"external", "images", "imgur", "integration", "notifications"}
)
@Slf4j
public class RandomScreenshotPlugin extends Plugin
{
	private static final Set<Integer> REPORT_BUTTON_TLIS = ImmutableSet.of(
		WidgetID.FIXED_VIEWPORT_GROUP_ID,
		WidgetID.RESIZABLE_VIEWPORT_OLD_SCHOOL_BOX_GROUP_ID,
		WidgetID.RESIZABLE_VIEWPORT_BOTTOM_LINE_GROUP_ID);

	@Inject
	private RandomScreenshotConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private RandomScreenshotOverlay randomScreenshotOverlay;

	@Inject
	private Client client;

	@Inject
	private ClientUI clientUi;

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
		String screenshotSubDir = null;
		String fileName = "foo";

		if (rand.nextInt(1000) > 900)
		{
			takeScreenshot(fileName, screenshotSubDir);
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

		if (config.displayDate() && REPORT_BUTTON_TLIS.contains(client.getTopLevelInterfaceId()))
		{
			randomScreenshotOverlay.queueForTimestamp(imageCallback);
		}
		else
		{
			drawManager.requestNextFrameListener(imageCallback);
		}
	}

	private static int getScaledValue(final double scale, final int value)
	{
		return (int) (value * scale + .5);
	}

	private void takeScreenshot(String fileName, String subDir, Image image)
	{
		final BufferedImage screenshot;
		if (!config.includeFrame())
		{
			// just simply copy the image
			screenshot = ImageUtil.bufferedImageFromImage(image);
		}
		else
		{
			// create a new image, paint the client ui to it, and then draw the screenshot to that
			final AffineTransform transform = OSType.getOSType() == OSType.MacOS ? new AffineTransform() :
				clientUi.getGraphicsConfiguration().getDefaultTransform();

			// scaled client dimensions
			int clientWidth = getScaledValue(transform.getScaleX(), clientUi.getWidth());
			int clientHeight = getScaledValue(transform.getScaleY(), clientUi.getHeight());

			screenshot = new BufferedImage(clientWidth, clientHeight, BufferedImage.TYPE_INT_ARGB);

			Graphics2D graphics = (Graphics2D) screenshot.getGraphics();
			AffineTransform originalTransform = graphics.getTransform();
			// scale g2d for the paint() call
			graphics.setTransform(transform);

			// Draw the client frame onto the screenshot
			try
			{
				SwingUtilities.invokeAndWait(() -> clientUi.paint(graphics));
			}
			catch (InterruptedException | InvocationTargetException e)
			{
				log.warn("unable to paint client UI on screenshot", e);
			}

			// Find the position of the canvas inside the frame
			final Point canvasOffset = clientUi.getCanvasOffset();
			final int gameOffsetX = getScaledValue(transform.getScaleX(), canvasOffset.getX());
			final int gameOffsetY = getScaledValue(transform.getScaleY(), canvasOffset.getY());

			// Draw the original screenshot onto the new screenshot
			graphics.setTransform(originalTransform); // the original screenshot is already scaled
			graphics.drawImage(image, gameOffsetX, gameOffsetY, null);
			graphics.dispose();
		}

		imageCapture.takeScreenshot(screenshot, fileName, subDir, false, config.uploadScreenshot());
	}
}
