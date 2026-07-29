package com.tpswitcher.gui;

import com.tpswitcher.Profile;
import com.tpswitcher.ProfileManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ProfileListScreen extends Screen {

	private static final int ROW_HEIGHT = 26;
	private static final int ADD_BAR_HEIGHT = 20;
	private static final int ADD_BAR_TOP = 28;
	private static final int LIST_TOP = ADD_BAR_TOP + ADD_BAR_HEIGHT + 8;
	private static final int ICON_SIZE = 16;

	private final Screen parent;
	private int scrollOffset = 0;

	private final List<Profile> visibleRows = new ArrayList<>();
	private int rowX;
	private int rowWidth;

	public ProfileListScreen(Screen parent) {
		super(Text.literal("Texture Pack Switcher - Profiles"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		rebuildWidgets();
	}

	private void rebuildWidgets() {
		clearChildren();
		visibleRows.clear();

		List<Profile> profiles = ProfileManager.getProfiles();

		int listBottom = this.height - 34;
		int visibleRowCount = Math.max(1, (listBottom - LIST_TOP) / ROW_HEIGHT);
		int maxScroll = Math.max(0, profiles.size() - visibleRowCount);
		if (scrollOffset > maxScroll) scrollOffset = maxScroll;
		if (scrollOffset < 0) scrollOffset = 0;

		rowWidth = Math.min(420, this.width - 40);
		rowX = (this.width - rowWidth) / 2;

		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("+"),
				b -> this.client.setScreen(new ProfileEditScreen(this, null))
		).dimensions(rowX, ADD_BAR_TOP, rowWidth, ADD_BAR_HEIGHT).build());

		int editDeleteWidth = 50;

		for (int i = 0; i < visibleRowCount && (i + scrollOffset) < profiles.size(); i++) {
			int index = i + scrollOffset;
			Profile profile = profiles.get(index);
			int y = LIST_TOP + i * ROW_HEIGHT;
			visibleRows.add(profile);

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal(profileLabel(profile)),
					b -> {
						ProfileManager.applyProfile(profile);
						this.close();
					}
			).dimensions(rowX, y, rowWidth - (editDeleteWidth * 2) - 4, ROW_HEIGHT - 4).build());

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal("Edit"),
					b -> this.client.setScreen(new ProfileEditScreen(this, profile))
			).dimensions(rowX + rowWidth - (editDeleteWidth * 2) - 2, y, editDeleteWidth, ROW_HEIGHT - 4).build());

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal("Delete"),
					b -> {
						ProfileManager.removeProfile(profile);
						rebuildWidgets();
					}
			).dimensions(rowX + rowWidth - editDeleteWidth, y, editDeleteWidth, ROW_HEIGHT - 4).build());
		}

		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("Close"),
				b -> this.close()
		).dimensions(rowX, this.height - 26, rowWidth, 20).build());
	}

	private String profileLabel(Profile profile) {
		return "     " + profile.getName() + "  (" + profile.getPacks().size() + " packs)";
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scrollOffset -= (int) Math.signum(verticalAmount);
		rebuildWidgets();
		return true;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0x90000000);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);

		if (ProfileManager.getProfiles().isEmpty()) {
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("No profiles yet. Click '+' above to get started."),
					this.width / 2, LIST_TOP + 10, 0xAAAAAA);
		}

		super.render(context, mouseX, mouseY, delta);

		for (int i = 0; i < visibleRows.size(); i++) {
			Profile profile = visibleRows.get(i);
			int y = LIST_TOP + i * ROW_HEIGHT;
			ItemStack icon = ProfileManager.resolveIcon(profile.getIcon());
			int iconY = y + ((ROW_HEIGHT - 4) - ICON_SIZE) / 2;
			context.drawItem(icon, rowX + 4, iconY);
		}
	}

	@Override
	public void close() {
		this.client.setScreen(parent);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}