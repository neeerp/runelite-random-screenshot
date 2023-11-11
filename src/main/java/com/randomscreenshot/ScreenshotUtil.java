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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import static net.runelite.client.RuneLite.SCREENSHOT_DIR;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ScreenshotTaken;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

@Singleton
@Slf4j
public class ScreenshotUtil
{
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

	@Inject
	private Client client;
	@Inject
	private DrawManager drawManager;
	@Inject
	private ScheduledExecutorService executor;
	@Inject
	private EventBus eventBus;
	
	public void takeScreenshot(String directory)
	{
		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			return;
		}

		Consumer<Image> imageCallback = (img) ->
		{
			// This callback is on the game thread, move to executor thread
			executor.submit(() -> saveScreenshot(ImageUtil.bufferedImageFromImage(img), directory));
		};

		drawManager.requestNextFrameListener(imageCallback);
	}

	private void saveScreenshot(BufferedImage screenshot, String directory)
	{

		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
			log.debug("Login screenshot prevented");
			return;
		}

		File screenshotFile = getScreenshotFile(directory);
		try
		{
			ImageIO.write(screenshot, "PNG", screenshotFile);
		}
		catch (IOException ex)
		{
			log.error("error writing screenshot", ex);
			return;
		}


		ScreenshotTaken screenshotTaken = new ScreenshotTaken(
			screenshotFile,
			screenshot
		);
		eventBus.post(screenshotTaken);
	}

	// TODO: Maybe it'd be best to move all this logic for getting the screenshot file
	//  to be separate from the screenshot code.
	private File getScreenshotFile(String directory) {
		String fileName = "";

		File screenshotDirectory = getOrCreateScreenshotDirectory(directory, client);
		screenshotDirectory.mkdirs();

		fileName += (fileName.isEmpty() ? "" : " ") + format(new Date());
		File screenshotFile = new File(screenshotDirectory, fileName + ".png");

		// To make sure that screenshots don't get overwritten, check if file exists,
		// and if it does create file with same name and suffix.
		int i = 1;
		while (screenshotFile.exists())
		{
			screenshotFile = new File(screenshotDirectory, fileName + String.format("(%d)", i++) + ".png");
		}

		return screenshotFile;
	}

	private File getOrCreateScreenshotDirectory(String directory, Client client) {
		File screenshotDirectory;
		if (!directory.isEmpty()) {
			screenshotDirectory = new File(directory);
		}
		else {
			screenshotDirectory = getDefaultScreenshotDirectory(client);
		}

		return screenshotDirectory;
	}

	private File getDefaultScreenshotDirectory(Client client) {
		File directory;

		if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null)
		{
			String playerDir = client.getLocalPlayer().getName();
			RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
			if (profileType != RuneScapeProfileType.STANDARD)
			{
				playerDir += "-" + Text.titleCase(profileType);
			}
			playerDir += File.separator + "Random Screenshots";

			directory = new File(SCREENSHOT_DIR, playerDir);
		}
		else
		{
			directory = SCREENSHOT_DIR;
		}

		return directory;
	}

	private static String format(Date date)
	{
		synchronized (TIME_FORMAT)
		{
			return TIME_FORMAT.format(date);
		}
	}
}
