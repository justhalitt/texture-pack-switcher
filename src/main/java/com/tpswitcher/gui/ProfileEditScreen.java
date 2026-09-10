package com.tpswitcher.gui;

import com.tpswitcher.Profile;
import com.tpswitcher.ProfileManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProfileEditScreen extends Screen {

	private static final int ROW_HEIGHT = 20;
	private static final int ICON_BUTTON_SIZE = 24;
	private static final int PANEL_WIDTH = 270;

	private final Screen parent;
	private final Profile editingProfile;
	private final List<String> availablePacks = new ArrayList<>();
	private List<String> filteredPacks = new ArrayList<>();
	private final Set<String> selectedPacks = new LinkedHashSet<>();
	private final List<ButtonWidget> packButtons = new ArrayList<>();

	private TextFieldWidget nameField;
	private TextFieldWidget searchField;
	private ButtonWidget iconButton;
	private ButtonWidget sortButton;
	private String selectedIcon;
	private String pendingName;
	private String importStatus = "";
	private boolean initialized = false;
	private boolean recentMode = false;
	private int scrollOffset = 0;

	private int panelLeft;
	private int panelTop;
	private int panelHeight;
	private int listTop;

	public ProfileEditScreen(Screen parent, Profile editingProfile) {
		super(Text.literal(editingProfile == null ? "New Texture Pack Profile" : "Edit Profile"));
		this.parent = parent;
		this.editingProfile = editingProfile;
		this.selectedIcon = editingProfile != null ? editingProfile.getIcon() : "minecraft:chest";
		this.pendingName = editingProfile != null ? editingProfile.getName() : "New Profile";
	}

	@Override
	protected void init() {
		this.client.getResourcePackManager().scanPacks();
		availablePacks.clear();
		availablePacks.addAll(ProfileManager.getAvailablePackIds(this.client));
		availablePacks.sort((a, b) -> stripPrefix(a).compareToIgnoreCase(stripPrefix(b)));

		if (!initialized) {
			selectedPacks.clear();
			if (editingProfile != null) {
				selectedPacks.addAll(editingProfile.getPacks());
			}
			initialized = true;
		}

		int centerX = this.width / 2;
		panelLeft = centerX - PANEL_WIDTH / 2;
		panelHeight = Math.min(360, this.height - 20);
		panelTop = (this.height - panelHeight) / 2;
		listTop = panelTop + 66;

		int rowY = panelTop + 10;
		iconButton = ButtonWidget.builder(Text.literal(""), b ->
				this.client.setScreen(new IconPickerScreen(this, icon -> selectedIcon = icon))
		).dimensions(panelLeft, rowY, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE).build();
		this.addDrawableChild(iconButton);

		int nameX = panelLeft + ICON_BUTTON_SIZE + 6;
		int nameWidth = PANEL_WIDTH - ICON_BUTTON_SIZE - 6;
		nameField = new TextFieldWidget(this.textRenderer, nameX, rowY + 2, nameWidth, 20, Text.literal("Profile Name"));
		nameField.setMaxLength(32);
		nameField.setText(pendingName);
		nameField.setChangedListener(s -> pendingName = s);
		this.addDrawableChild(nameField);
		this.setInitialFocus(nameField);

		int searchY = panelTop + 42;
		int importWidth = 50;
		int sortWidth = 60;
		int gap = 4;
		int searchWidth = PANEL_WIDTH - importWidth - sortWidth - gap * 2;

		searchField = new TextFieldWidget(this.textRenderer, panelLeft, searchY, searchWidth, 18, Text.literal("Search texture packs"));
		searchField.setMaxLength(64);
		searchField.setPlaceholder(Text.literal("Search texture packs..."));
		searchField.setChangedListener(s -> applyPackFilter());
		this.addDrawableChild(searchField);

		sortButton = ButtonWidget.builder(sortButtonText(), b -> {
			recentMode = !recentMode;
			sortButton.setMessage(sortButtonText());
			applyPackFilter();
		}).dimensions(panelLeft + searchWidth + gap, searchY, sortWidth, 18).build();
		this.addDrawableChild(sortButton);

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Import"), b -> importCurrentlyActivePacks())
				.dimensions(panelLeft + searchWidth + gap + sortWidth + gap, searchY, importWidth, 18).build());

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> save())
				.dimensions(panelLeft, panelTop + panelHeight - 24, (PANEL_WIDTH / 2) - 3, 20).build());

		this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> this.client.setScreen(parent))
				.dimensions(panelLeft + (PANEL_WIDTH / 2) + 3, panelTop + panelHeight - 24, (PANEL_WIDTH / 2) - 3, 20).build());

		packButtons.clear();
		int listBottom = panelTop + panelHeight - 34;
		int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
		for (int i = 0; i < visibleRows; i++) {
			final int rowIndex = i;
			int y = listTop + i * ROW_HEIGHT;
			ButtonWidget btn = ButtonWidget.builder(Text.literal(""), b -> togglePack(rowIndex))
					.dimensions(panelLeft, y, PANEL_WIDTH, ROW_HEIGHT - 2).build();
			packButtons.add(btn);
			this.addDrawableChild(btn);
		}

		applyPackFilter();
	}

	private Text sortButtonText() {
		return Text.literal(recentMode ? "Sort: Recent" : "Sort: A-Z");
	}

	private void applyPackFilter() {
		String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
		List<String> matches = new ArrayList<>();
		for (String packId : availablePacks) {
			String displayName = stripPrefix(packId);
			if (query.isEmpty() || displayName.toLowerCase().contains(query)) {
				matches.add(packId);
			}
		}

		if (recentMode) {
			List<String> ordered = new ArrayList<>();
			for (String packId : ProfileManager.getRecentPacks()) {
				if (matches.contains(packId) && !ordered.contains(packId)) {
					ordered.add(packId);
				}
			}
			for (String packId : matches) {
				if (!ordered.contains(packId)) {
					ordered.add(packId);
				}
			}
			filteredPacks = ordered;
		} else {
			filteredPacks = matches;
		}

		scrollOffset = 0;
		updatePackButtonLabels();
	}

	private void togglePack(int rowIndex) {
		int index = rowIndex + scrollOffset;
		if (index < 0 || index >= filteredPacks.size()) {
			return;
		}
		String packId = filteredPacks.get(index);
		if (selectedPacks.contains(packId)) {
			selectedPacks.remove(packId);
		} else {
			selectedPacks.add(packId);
		}
		updatePackButtonLabels();
	}

	private void importCurrentlyActivePacks() {
		java.util.Collection<String> enabled = this.client.getResourcePackManager().getEnabledIds();
		int imported = 0;
		for (String packId : enabled) {
			if (packId.startsWith("file/") && !selectedPacks.contains(packId)) {
				selectedPacks.add(packId);
				imported++;
			}
		}

		importStatus = imported > 0
				? "Imported " + imported + " currently active pack(s)."
				: "No active texture packs to import.";
		updatePackButtonLabels();
	}

	private void updatePackButtonLabels() {
		for (int i = 0; i < packButtons.size(); i++) {
			int index = i + scrollOffset;
			ButtonWidget btn = packButtons.get(i);
			if (index < filteredPacks.size()) {
				String packId = filteredPacks.get(index);
				String displayName = stripPrefix(packId);
				btn.setMessage(Text.literal(displayName));
				btn.active = true;
				btn.visible = true;
			} else {
				btn.setMessage(Text.literal(""));
				btn.active = false;
				btn.visible = false;
			}
		}
	}

	private static String stripPrefix(String packId) {
		int slash = packId.lastIndexOf('/');
		return slash >= 0 ? packId.substring(slash + 1) : packId;
	}

	private void save() {
		String name = nameField.getText().trim();
		if (name.isEmpty()) {
			name = "Unnamed Profile";
		}
		String icon = selectedIcon == null || selectedIcon.isBlank() ? "minecraft:chest" : selectedIcon;

		if (editingProfile != null) {
			editingProfile.setName(name);
			editingProfile.setIcon(icon);
			editingProfile.getPacks().clear();
			editingProfile.getPacks().addAll(selectedPacks);
			ProfileManager.save();
		} else {
			Profile newProfile = new Profile(name, icon);
			newProfile.getPacks().addAll(selectedPacks);
			ProfileManager.addProfile(newProfile);
		}

		this.client.setScreen(new ProfileListScreen(null));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0x90000000);
		context.fill(panelLeft - 10, panelTop - 10, panelLeft + PANEL_WIDTH + 10, panelTop + panelHeight + 10, 0xC0202020);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, panelLeft + PANEL_WIDTH / 2, panelTop - 4, 0xFFFFFF);

		if (availablePacks.isEmpty()) {
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("No texture packs found in the resourcepacks folder."),
					panelLeft + PANEL_WIDTH / 2, listTop + 10, 0xFF5555);
		} else if (filteredPacks.isEmpty()) {
			context.drawCenteredTextWithShadow(this.textRenderer,
					Text.literal("No texture packs match your search."),
					panelLeft + PANEL_WIDTH / 2, listTop + 10, 0xFF5555);
		}

		super.render(context, mouseX, mouseY, delta);

		for (int i = 0; i < packButtons.size(); i++) {
			int index = i + scrollOffset;
			if (index >= filteredPacks.size()) continue;
			String packId = filteredPacks.get(index);
			if (selectedPacks.contains(packId)) {
				ButtonWidget btn = packButtons.get(i);
				context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), 0x8040C040);
			}
		}

		ItemStack previewIcon = ProfileManager.resolveIcon(selectedIcon);
		int iconRenderX = iconButton.getX() + (ICON_BUTTON_SIZE - 16) / 2;
		int iconRenderY = iconButton.getY() + (ICON_BUTTON_SIZE - 16) / 2;
		context.drawItem(previewIcon, iconRenderX, iconRenderY);

		if (!importStatus.isEmpty()) {
			context.drawTextWithShadow(this.textRenderer, importStatus, panelLeft, panelTop + 62, 0xAAAAAA);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int listBottom = panelTop + panelHeight - 34;
		int visibleRows = Math.max(1, (listBottom - listTop) / ROW_HEIGHT);
		int maxScroll = Math.max(0, filteredPacks.size() - visibleRows);
		scrollOffset -= (int) Math.signum(verticalAmount);
		if (scrollOffset < 0) scrollOffset = 0;
		if (scrollOffset > maxScroll) scrollOffset = maxScroll;
		updatePackButtonLabels();
		return true;
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
