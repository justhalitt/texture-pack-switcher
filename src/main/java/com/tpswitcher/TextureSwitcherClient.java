package com.tpswitcher;

import com.tpswitcher.gui.ProfileListScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class TextureSwitcherClient implements ClientModInitializer {

	private static final KeyBinding.Category CATEGORY =
			KeyBinding.Category.create(Identifier.of("tpswitcher", "general"));

	// The key that opens the mod's main UI (the profile list). Default: "I".
	private static KeyBinding openMenuKey;

	@Override
	public void onInitializeClient() {
		ProfileManager.load();

		openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.tpswitcher.open_menu",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_I,
				CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openMenuKey.wasPressed()) {
				if (client.currentScreen == null) {
					client.setScreen(new ProfileListScreen(null));
				}
			}
		});
	}
}