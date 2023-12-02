package com.randomscreenshot;

import static net.runelite.client.RuneLite.SCREENSHOT_DIR;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.RuneScapeProfileType;
import net.runelite.client.util.Text;

@Singleton
@Slf4j
public class FileFactory {
  private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  @Inject private Client client;

  @Inject private RandomScreenshotConfig config;

  public File createScreenshotFile() throws IOException {
    File screenshotDirectory = createScreenshotDirectory();
    String fileName = format(new Date());

    File screenshotFile = new File(screenshotDirectory, fileName + ".png");

    // To make sure that screenshots don't get overwritten, check if file exists,
    // and if it does create file with same name and suffix.
    int i = 1;
    while (screenshotFile.exists()) {
      screenshotFile =
          new File(screenshotDirectory, fileName + String.format("(%d)", i++) + ".png");
    }

    return screenshotFile;
  }

  /**
   * Create a directory at `path` if one does not exist, and return the corresponding `File` object.
   *
   * <p>If path is an empty string, the player profile directory is used instead.
   */
  public File createScreenshotDirectory() throws IOException {
    File screenshotDirectory;
    if (config.useCustomDirectory()) {
      screenshotDirectory = new File(config.screenshotDirectory());
    } else {
      screenshotDirectory = getPlayerScreenshotDirectory();
    }

    if (!screenshotDirectory.mkdirs() && !screenshotDirectory.exists()) {
      throw new IOException(
          "Could not create screenshot directory at " + screenshotDirectory.getAbsolutePath());
    }

    return screenshotDirectory;
  }

  private File getPlayerScreenshotDirectory() {
    File directory;

    if (client.getLocalPlayer() != null && client.getLocalPlayer().getName() != null) {
      String playerDir = client.getLocalPlayer().getName();
      RuneScapeProfileType profileType = RuneScapeProfileType.getCurrent(client);
      if (profileType != RuneScapeProfileType.STANDARD) {
        playerDir += "-" + Text.titleCase(profileType);
      }
      playerDir += File.separator + "Random Screenshots";

      directory = new File(SCREENSHOT_DIR, playerDir);
    } else {
      directory = SCREENSHOT_DIR;
    }

    return directory;
  }

  private static String format(Date date) {
    synchronized (TIME_FORMAT) {
      return TIME_FORMAT.format(date);
    }
  }
}
