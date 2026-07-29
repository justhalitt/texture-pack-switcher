package com.tpswitcher.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Lets the user pick any item/block in the game as a profile icon instead of
 * typing its id by hand. Has a search box and a scrollable grid of icons.
 */
public class IconPickerScreen extends Screen {

    private static final int COLS = 9;
    private static final int CELL = 24;
    private static final int GRID_TOP = 45;

    private final Screen parent;
    private final Consumer<String> onSelect;

    private final List<Identifier> allItemIds = new ArrayList<>();
    private List<Identifier> filtered = new ArrayList<>();

    private TextFieldWidget searchField;
    private int scrollOffsetRows = 0;
    private int rows;
    private int gridX;

    private final List<ButtonWidget> slotButtons = new ArrayList<>();

    public IconPickerScreen(Screen parent, Consumer<String> onSelect) {
        super(Text.literal("Choose Icon"));
        this.parent = parent;
        this.onSelect = onSelect;
    }

    @Override
    protected void init() {
        allItemIds.clear();
        for (Item item : Registries.ITEM) {
            if (item == Items.AIR) continue;
            allItemIds.add(Registries.ITEM.getId(item));
        }
        allItemIds.sort((a, b) -> a.toString().compareTo(b.toString()));

        int gridWidth = COLS * CELL;
        gridX = (this.width - gridWidth) / 2;

        searchField = new TextFieldWidget(this.textRenderer, gridX, 18, gridWidth, 20, Text.literal("Search"));
        searchField.setMaxLength(64);
        searchField.setChangedListener(s -> applyFilter());
        this.addDrawableChild(searchField);
        this.setInitialFocus(searchField);

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), b -> this.client.setScreen(parent))
                .dimensions(gridX, this.height - 26, gridWidth, 20).build());

        int listBottom = this.height - 34;
        rows = Math.max(1, (listBottom - GRID_TOP) / CELL);

        slotButtons.clear();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                final int rowIndex = r;
                final int colIndex = c;
                int x = gridX + c * CELL;
                int y = GRID_TOP + r * CELL;
                ButtonWidget btn = ButtonWidget.builder(Text.literal(""), b -> selectSlot(rowIndex, colIndex))
                        .dimensions(x, y, CELL - 2, CELL - 2).build();
                slotButtons.add(btn);
                this.addDrawableChild(btn);
            }
        }

        applyFilter();
    }

    private void applyFilter() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        filtered = new ArrayList<>();
        for (Identifier id : allItemIds) {
            if (query.isEmpty() || id.getPath().toLowerCase().contains(query)) {
                filtered.add(id);
            }
        }
        scrollOffsetRows = 0;
        updateSlotActiveStates();
    }

    private void updateSlotActiveStates() {
        for (int i = 0; i < slotButtons.size(); i++) {
            int index = scrollOffsetRows * COLS + i;
            slotButtons.get(i).active = index < filtered.size();
        }
    }

    private void selectSlot(int rowIndex, int colIndex) {
        int index = (scrollOffsetRows + rowIndex) * COLS + colIndex;
        if (index < 0 || index >= filtered.size()) {
            return;
        }
        Identifier id = filtered.get(index);
        onSelect.accept(id.toString());
        this.client.setScreen(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalRows = Math.max(1, (filtered.size() + COLS - 1) / COLS);
        int maxScroll = Math.max(0, totalRows - rows);
        scrollOffsetRows -= (int) Math.signum(verticalAmount);
        if (scrollOffsetRows < 0) scrollOffsetRows = 0;
        if (scrollOffsetRows > maxScroll) scrollOffsetRows = maxScroll;
        updateSlotActiveStates();
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, 0x90000000);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                int index = (scrollOffsetRows + r) * COLS + c;
                if (index >= filtered.size()) continue;
                Identifier id = filtered.get(index);
                Item item = Registries.ITEM.get(id);
                ItemStack stack = new ItemStack(item);
                int x = gridX + c * CELL + 3;
                int y = GRID_TOP + r * CELL + 3;
                context.drawItem(stack, x, y);
            }
        }

        if (filtered.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("No items match."),
                    this.width / 2, GRID_TOP + 10, 0xFF5555);
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