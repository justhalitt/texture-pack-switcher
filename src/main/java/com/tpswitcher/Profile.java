package com.tpswitcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single texture pack profile.
 * name: the name the user gave this profile
 * icon: an item/block id used as the icon shown next to the profile in the list
 *       (e.g. "minecraft:diamond_sword", "minecraft:grass_block")
 * packs: the resource pack ids belonging to this profile (the id used by
 *        ResourcePackManager, e.g. "file/MyPack.zip")
 */
public class Profile {

	private String name;
	private String icon;
	private final List<String> packs = new ArrayList<>();

	public Profile(String name) {
		this(name, "minecraft:chest");
	}

	public Profile(String name, String icon) {
		this.name = name;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon == null || icon.isBlank() ? "minecraft:chest" : icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public List<String> getPacks() {
		return packs;
	}
}