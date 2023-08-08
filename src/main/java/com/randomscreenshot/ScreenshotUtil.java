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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUploadStyle;
import net.runelite.client.util.ImageUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Singleton
@Slf4j
public class ScreenshotUtil
{
	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private Client client;
	@Inject
	private DrawManager drawManager;
	@Inject
	private ScheduledExecutorService executor;
	@Inject
	private ImageCapture imageCapture;

	@Inject
	private RandomScreenshotConfig config;

	/**
	 * Saves a screenshot of the client window to the screenshot folder as a PNG,
	 * and optionally uploads it to an image-hosting service.
	 *
	 * @param fileName Filename to use, without file extension.
	 * @param subDir   Subdirectory to store the captured screenshot in.
	 */
	public void takeScreenshot(String fileName, String subDir)
	{
		if (client.getGameState() == GameState.LOGIN_SCREEN)
		{
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
		BufferedImage bufferedImage = ImageUtil.bufferedImageFromImage(image);

		imageCapture.takeScreenshot(bufferedImage, fileName, subDir, false, ImageUploadStyle.NEITHER);

		if (config.useDiscordWebhook()) {
			postToDiscord(bufferedImage);
		}
	}

	private void postToDiscord(BufferedImage image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "png", baos);
		} catch (IOException e) {
			log.error("failed to write image to output stream: ", e);
			return;
		}

		RequestBody body = new MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("image", "myfile.png", RequestBody.create(MediaType.parse("image/*png"), baos.toByteArray())).build();

		okHttpClient.newCall(new Request.Builder()
			.url(config.discordWebhookUrl())
			.post(body).build()
		).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.error("error uploading screenshot", e);
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					log.error("post to discord webhook was unsuccessful! HTTP Status: {}", response.code());
				}
				response.close();
			}
		});

	}
}
