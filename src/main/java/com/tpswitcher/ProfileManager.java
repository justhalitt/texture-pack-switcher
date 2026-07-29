package com.tpswitcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.util.Identifier;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for loading, saving and applying all texture pack profiles.
 */
public class ProfileManager {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("tpswitcher_profiles.json");

	private static final List<Profile> PROFILES = new ArrayList<>();

	private ProfileManager() {
	}

	public static List<Profile> getProfiles() {
		return PROFILES;
	}

	public static void addProfile(Profile profile) {
		PROFILES.add(profile);
		save();
	}

	public static void removeProfile(Profile profile) {
		PROFILES.remove(profile);
		save();
	}

	public static void load() {
		PROFILES.clear();
		if (!Files.exists(CONFIG_FILE)) {
			return;
		}
		try (FileReader reader = new FileReader(CONFIG_FILE.toFile())) {
			Type listType = new TypeToken<ArrayList<Profile>>() {}.getType();
			List<Profile> loaded = GSON.fromJson(reader, listType);
			if (loaded != null) {
				PROFILES.addAll(loaded);
			}
		} catch (IOException e) {
			System.err.println("[TextureSwitcher] Failed to read profiles: " + e.getMessage());
		}
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_FILE.getParent());
			try (FileWriter writer = new FileWriter(CONFIG_FILE.toFile())) {
				GSON.toJson(PROFILES, writer);
			}
		} catch (IOException e) {
			System.err.println("[TextureSwitcher] Failed to save profiles: " + e.getMessage());
		}
	}

	/**
	 * Enables all texture packs belonging to the given profile and disables every
	 * other currently active texture pack that came from the user's resourcepacks
	 * folder. Does not touch packs provided by mods or vanilla itself.
	 */
	public static void applyProfile(Profile profile) {
		MinecraftClient client = MinecraftClient.getInstance();
		ResourcePackManager manager = client.getResourcePackManager();

		List<String> newEnabled = new ArrayList<>();

		for (String id : manager.getEnabledIds()) {
			if (!id.startsWith("file/")) {
				newEnabled.add(id);
			}
		}

		for (String packId : profile.getPacks()) {
			if (manager.getProfile(packId) != null && !newEnabled.contains(packId)) {
				newEnabled.add(packId);
			}
		}

		manager.setEnabledProfiles(newEnabled);

		if (client.options != null) {
			client.options.resourcePacks = new ArrayList<>(newEnabled);
			client.options.write();
		}

		client.reloadResources();

		if (client.player != null) {
			client.player.sendMessage(
					net.minecraft.text.Text.literal("[TextureSwitcher] Applied profile: " + profile.getName()),
					true
			);
		}
	}

	public static ItemStack resolveIcon(String iconId) {
		if (iconId != null) {
			Identifier id = Identifier.tryParse(iconId.trim());
			if (id != null) {
				Item item = Registries.ITEM.get(id);
				if (item != Items.AIR) {
					return new ItemStack(item);
				}
			}
		}
		return new ItemStack(Items.CHEST);
	}

	public static List<String> getAvailablePackIds(MinecraftClient client) {
		List<String> ids = new ArrayList<>();
		client.getResourcePackManager().getProfiles().forEach(p -> {
			if (p.getId().startsWith("file/")) {
				ids.add(p.getId());
			}
		});
		return ids;
	}
}