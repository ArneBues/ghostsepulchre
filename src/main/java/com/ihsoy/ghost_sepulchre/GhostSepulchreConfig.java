package com.ihsoy.ghost_sepulchre;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup("ghostsepulchre")
public interface GhostSepulchreConfig extends Config
{
	@ConfigSection(
			name = "Ghost Sepulchres",
			description = "",
			position = 1
	)
	String ghostSection = "ghostSection";
	@ConfigItem(
			keyName = "ghostColor",
			name = "Ghost Color",
			section = ghostSection,
			description = "Configures the color of the ghost tile",
			position = 1
	)
	default Color ghostColor() {
		return Color.GRAY;
	}


	@ConfigSection(
			name = "Reset",
			description = "Remove all stored recordings",
			position = 2
	)
	String resetSection = "reset";

	@ConfigItem(
			keyName = "reset",
			name = "Reset?",
			section = resetSection,
			description = "Remove all stored recordings",
			warning = "This will remove all recorded Ghosts",
			position = 1
	)
	default boolean reset() {
		return true;
	}
}
