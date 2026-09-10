package com.tpswitcher;

import java.util.ArrayList;
import java.util.List;

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
