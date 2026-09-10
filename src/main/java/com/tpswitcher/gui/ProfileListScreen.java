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
	private static final int ICON_SIZE = 16;
	private static final int PANEL_WIDTH = 300;

	private final Screen parent;
	private int scrollOffset = 0;

	private final List<Profile> visibleRows = new ArrayList<>();
	private int panelLeft;
	private int panelTop;
	private int panelHeight;
	private int addBarTop;
	private int listTop;

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

		int centerX = this.width / 2;
		panelLeft = centerX - PANEL_WIDTH / 2;
		panelHeight = Math.min(320, this.height - 20);
		panelTop = (this.height - panelHeight) / 2;
		addBarTop = panelTop + 22;
		listTop = addBarTop + ADD_BAR_HEIGHT + 6;

		int listBottom = panelTop + panelHeight - 30;
		int visibleRowCount = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
		int maxScroll = Math.max(0, profiles.size() - visibleRowCount);
		if (scrollOffset > maxScroll) scrollOffset = maxScroll;
		if (scrollOffset < 0) scrollOffset = 0;

		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("+"),
				b -> this.client.setScreen(new ProfileEditScreen(this, null))
		).dimensions(panelLeft, addBarTop, PANEL_WIDTH, ADD_BAR_HEIGHT).build());

		int editDeleteWidth = 50;

		for (int i = 0; i < visibleRowCount && (i + scrollOffset) < profiles.size(); i++) {
			int index = i + scrollOffset;
			Profile profile = profiles.get(index);
			int y = listTop + i * ROW_HEIGHT;
			visibleRows.add(profile);

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal(profileLabel(profile)),
					b -> {
						ProfileManager.applyProfile(profile);
						this.close();
					}
			).dimensions(panelLeft, y, PANEL_WIDTH - (editDeleteWidth * 2) - 4, ROW_HEIGHT - 4).build());

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal("Edit"),
					b -> this.client.setScreen(new ProfileEditScreen(this, profile))
			).dimensions(panelLeft + PANEL_WIDTH - (editDeleteWidth * 2) - 2, y, editDeleteWidth, ROW_HEIGHT - 4).build());

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal("Delete"),
					b -> {
						ProfileManager.removeProfile(profile);
						rebuildWidgets();
					}
			).dimensions(panelLeft + PANEL_WIDTH - editDeleteWidth, y, editDeleteWidth, ROW_HEIGHT - 4).build());
		}

		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("Close"),
				b -> this.close()
		).dimensions(panelLeft, panelTop + panelHeight - 24, PANEL_WIDTH, 20).build());
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
		context.fill(panelLeft - 10, panelTop - 10, panelLeft + PANEL_WIDTH + 10, panelTop + panelHeight + 10, 0xC0202020);
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, panelLeft + PANEL_WIDTH / 2, panelTop - 4, 0xFFFFFF);

		if (ProfileManager.getProfiles().isEmpty()) {
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("No profiles yet. Click '+' above to get started."),
					panelLeft + PANEL_WIDTH / 2, listTop + 10, 0xAAAAAA);
		}

		super.render(context, mouseX, mouseY, delta);

		for (int i = 0; i < visibleRows.size(); i++) {
			Profile profile = visibleRows.get(i);
			int y = listTop + i * ROW_HEIGHT;
			ItemStack icon = ProfileManager.resolveIcon(profile.getIcon());
			int iconY = y + ((ROW_HEIGHT - 4) - ICON_SIZE) / 2;
			context.drawItem(icon, panelLeft + 4, iconY);
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
