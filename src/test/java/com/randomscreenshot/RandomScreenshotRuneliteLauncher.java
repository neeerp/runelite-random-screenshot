package com.randomscreenshot;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RandomScreenshotRuneliteLauncher {
  public static void main(String[] args) throws Exception {
    ExternalPluginManager.loadBuiltin(RandomScreenshotPlugin.class);
    RuneLite.main(args);
  }
}
