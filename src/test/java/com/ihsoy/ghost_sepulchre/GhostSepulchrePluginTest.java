package com.ihsoy.ghost_sepulchre;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class GhostSepulchrePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GhostSepulchrePlugin.class);
		RuneLite.main(args);
	}
}