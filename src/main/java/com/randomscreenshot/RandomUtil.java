package com.randomscreenshot;

import java.util.Random;
import javax.inject.Singleton;

@Singleton
public class RandomUtil
{
	private final Random rand = new Random(System.currentTimeMillis());

	public int randInt(int weight)
	{
		return rand.nextInt(weight);
	}
}
