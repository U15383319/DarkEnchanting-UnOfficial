package io.github.frqnny.darkenchanting.client.gui;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import io.github.frqnny.darkenchanting.network.ApplyEnchantmentsPacket;
import io.github.frqnny.darkenchanting.network.ApplyRepairPacket;
import io.github.frqnny.darkenchanting.network.ModPackets;
import io.github.frqnny.darkenchanting.util.BookcaseUtils;
import io.github.frqnny.darkenchanting.util.CostUtils;
import io.github.frqnny.darkenchanting.util.EnchantingUtils;
import io.github.frqnny.darkenchanting.util.PlayerUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client UI of the Dark Enchanter. Ports the LibGui DarkEnchanterGUI to vanilla widgets:
 * an item slot, a scrollable list of enchantment sliders, Enchant (E) / Repair (R) buttons and a cost summary
 * drawn as a tooltip to the left of the window.
 */
public class DarkEnchanterScreen extends AbstractContainerScreen<DarkEnchanterMenu> {
    private static final ResourceLocation TABLE_BACKGROUND = DarkEnchanting.id("textures/gui/background_table.png");
    private static final int TABLE_BG_WIDTH = 143;
    private static final int TABLE_BG_HEIGHT = 138;
    private static final int TABLE_BG_TOP_ROWS = 114;
    private static final int TABLE_BG_SKIPPED_ROWS = 12;

    // The cost summary is drawn as a tooltip panel left of the window, with this gap to the window edge.
    private static final int COST_PANEL_GAP = 4;
    private static final int COST_PANEL_Y = 43;
    // Tooltips are drawn 12 px right of the given x and get a 3 px frame on each side.
    private static final int TOOLTIP_OFFSET = 12;
    private static final int TOOLTIP_FRAME = 3;
    // Room reserved for growing numbers (XP amounts) in the summary.
    private static final int COST_PANEL_SLACK = 24;

    // Layout (relative to leftPos/topPos), taken from the LibGui version
    private static final int LIST_X = 65;
    private static final int LIST_Y = 17;
    private static final int LIST_WIDTH = 142;
    private static final int LIST_HEIGHT = 123;
    private static final int SCROLLBAR_X = LIST_X + LIST_WIDTH;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SLIDER_X = LIST_X + 1;
    private static final int SLIDER_WIDTH = 140;
    private static final int SLIDER_HEIGHT = 18;
    private static final int SLIDER_SPACING = 2;
    private static final int ROW_HEIGHT = SLIDER_HEIGHT + SLIDER_SPACING;

    private final Player player;
    private final Object2IntMap<Enchantment> enchantmentsToApply = new Object2IntOpenHashMap<>(15);
    private final Object2IntMap<Enchantment> enchantmentsOnStack = new Object2IntOpenHashMap<>(15);
    private final Map<Enchantment, EnchantmentSlider> sliderCache = new HashMap<>();
    private final List<EnchantmentSlider> enchantmentSliders = new ArrayList<>();

    private Button enchantButton;
    private Button repairButton;
    private boolean enchantEnabled;
    private boolean repairEnabled;

    private ItemStack lastStack = ItemStack.EMPTY;
    private int enchantCost = 0;
    private int repairCost = 0;
    private int bookshelfDiscount = 0;
    private String bookcaseStats1 = "";
    private String bookcaseStats2 = "";
    private String bookcaseStats3 = "";

    private double scrollAmount;
    private EnchantmentSlider draggingSlider;
    private boolean draggingScrollbar;

    public DarkEnchanterScreen(DarkEnchanterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;
        this.imageWidth = DarkEnchanterMenu.WIDTH;
        this.imageHeight = DarkEnchanterMenu.HEIGHT;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = DarkEnchanterMenu.INVENTORY_X - 1;
        this.inventoryLabelY = DarkEnchanterMenu.INVENTORY_LABEL_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = this.computeLeftPos();

        this.enchantButton = this.addRenderableWidget(Button.builder(Component.literal("E"), b -> this.enchant())
                .bounds(this.leftPos + 35, this.topPos + 60, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.dark_enchanting.enchant")))
                .build());
        this.repairButton = this.addRenderableWidget(Button.builder(Component.literal("R"), b -> this.repair())
                .bounds(this.leftPos + 35, this.topPos + 85, 20, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.dark_enchanting.repair")))
                .build());
        this.enchantButton.active = this.enchantEnabled;
        this.repairButton.active = this.repairEnabled;

        for (EnchantmentSlider slider : this.sliderCache.values()) {
            slider.setX(this.leftPos + SLIDER_X);
        }

        ItemStack current = this.menu.getEnchantStack();
        if (!ItemStack.matches(this.lastStack, current) || this.lastStack.isEmpty()) {
            this.onStackUpdate(current);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack current = this.menu.getEnchantStack();
        if (!ItemStack.matches(this.lastStack, current)) {
            this.onStackUpdate(current);
        }
    }

    // ---------------------------------------------------------------- logic (ported from DarkEnchanterGUI)

    private void onStackUpdate(ItemStack stack) {
        this.lastStack = stack.copy();
        this.enchantmentsOnStack.clear();
        this.enchantmentsToApply.clear();
        this.sliderCache.clear();
        this.draggingSlider = null;
        this.scrollAmount = 0;

        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            this.enchantmentsToApply.putIfAbsent(entry.getKey(), entry.getValue().intValue());
            this.enchantmentsOnStack.put(entry.getKey(), entry.getValue().intValue());
        }

        this.fillBox();
        this.recalculateEnchantmentCost();
        this.recalculateRepairCost();
    }

    private void fillBox() {
        this.populateList();
        this.clampScroll();
    }

    private void populateList() {
        this.enchantmentSliders.clear();
        ItemStack stack = this.menu.getEnchantStack();
        for (Enchantment enchantment : EnchantingUtils.getAvailableEnchantments(stack, this.enchantmentsToApply, this.enchantmentsOnStack)) {
            this.enchantmentSliders.add(this.sliderCache.computeIfAbsent(enchantment, this::createSlider));
        }
    }

    private EnchantmentSlider createSlider(Enchantment enchantment) {
        int value = this.enchantmentsToApply.containsKey(enchantment) ? this.enchantmentsToApply.getInt(enchantment) : 0;
        return new EnchantmentSlider(this.leftPos + SLIDER_X, 0, SLIDER_WIDTH, SLIDER_HEIGHT, enchantment, Math.max(0, value),
                level -> this.onSliderValueChange(enchantment, level));
    }

    private void onSliderValueChange(Enchantment enchantment, int level) {
        this.enchantmentsToApply.put(enchantment, level);
        this.fillBox();
        this.recalculateEnchantmentCost();
    }

    private void recalculateEnchantmentCost() {
        this.menu.access.execute((level, pos) -> {
            int totalExperience = PlayerUtils.syncAndGetTotalExperience(this.player);
            this.enchantCost = BookcaseUtils.applyDiscount(CostUtils.getExperienceCost(this.enchantmentsToApply, this.enchantmentsOnStack), level, pos);

            this.bookcaseStats1 = BookcaseUtils.getObsidianCount(level, pos) ? "☆" : "";
            this.bookcaseStats2 = BookcaseUtils.getObsidianCount2(level, pos) ? "☆" : "";
            this.bookcaseStats3 = BookcaseUtils.getConduits(level, pos) ? "☆" : "";
            this.bookshelfDiscount = (int) (BookcaseUtils.getDiscount(level, pos) * 100);

            boolean enchantmentsUnchanged = true;
            for (var entry : this.enchantmentsToApply.object2IntEntrySet()) {
                Enchantment enchantment = entry.getKey();
                if (!this.enchantmentsOnStack.containsKey(enchantment) || this.enchantmentsOnStack.getInt(enchantment) != entry.getIntValue()) {
                    enchantmentsUnchanged = false;
                }
            }

            if (enchantmentsUnchanged) {
                for (var entry : this.enchantmentsOnStack.object2IntEntrySet()) {
                    Enchantment enchantment = entry.getKey();
                    if (!this.enchantmentsToApply.containsKey(enchantment) || this.enchantmentsToApply.getInt(enchantment) != entry.getIntValue()) {
                        enchantmentsUnchanged = false;
                    }
                }
            }

            boolean hasItem = !this.menu.getEnchantStack().isEmpty();
            this.enchantEnabled = hasItem && ((totalExperience >= this.enchantCost && !enchantmentsUnchanged) || this.player.isCreative());
        });
        if (this.enchantButton != null) {
            this.enchantButton.active = this.enchantEnabled;
        }
    }

    private void recalculateRepairCost() {
        ItemStack stack = this.menu.getEnchantStack();
        this.menu.access.execute((level, pos) -> this.repairCost = BookcaseUtils.applyDiscount(CostUtils.getRepairCost(stack), level, pos));
        this.repairEnabled = stack.isDamaged() || (!stack.isEmpty() && this.player.isCreative());
        if (this.repairButton != null) {
            this.repairButton.active = this.repairEnabled;
        }
    }

    private void enchant() {
        ModPackets.CHANNEL.sendToServer(new ApplyEnchantmentsPacket(new Object2IntOpenHashMap<>(this.enchantmentsToApply)));
    }

    private void repair() {
        ModPackets.CHANNEL.sendToServer(new ApplyRepairPacket());
    }

    private List<Component> getCostSummary() {
        Component enchantLine = this.enchantCost > 0
                ? Component.translatable("gui.dark_enchanting.pay", this.enchantCost)
                : Component.translatable("gui.dark_enchanting.receive", -this.enchantCost);

        return List.of(
                Component.translatable("gui.dark_enchanting.enchant_cost").withStyle(ChatFormatting.DARK_GREEN),
                enchantLine,
                Component.empty(),
                Component.empty(),
                Component.translatable("gui.dark_enchanting.repair_cost").withStyle(ChatFormatting.BLUE),
                Component.translatable("gui.dark_enchanting.pay", this.repairCost),
                Component.empty(),
                Component.empty(),
                Component.translatable("gui.dark_enchanting.shrine_discount").withStyle(ChatFormatting.DARK_PURPLE),
                Component.literal(this.bookcaseStats1 + this.bookcaseStats2 + this.bookcaseStats3 + " " + this.bookshelfDiscount + " %"),
                Component.empty(),
                Component.empty(),
                Component.translatable("gui.dark_enchanting.you_have", PlayerUtils.getTotalExperience(this.player)).withStyle(ChatFormatting.GOLD),
                Component.empty(),
                Component.empty());
    }

    private int costPanelTextWidth(List<Component> summary) {
        int width = 0;
        for (Component line : summary) {
            width = Math.max(width, this.font.width(line));
        }
        return width;
    }

    /**
     * The window is centred as usual when the cost panel fits to its left. On small screens (e.g. "Auto" GUI scale
     * at 426x240 or 384x240) the window is shifted right just enough for the panel, so neither gets cut off.
     */
    private int computeLeftPos() {
        int panelSpace = this.costPanelTextWidth(this.getCostSummary()) + COST_PANEL_SLACK + 2 * TOOLTIP_FRAME + COST_PANEL_GAP + 2;
        int centered = (this.width - this.imageWidth) / 2;
        int maxLeft = Math.max(0, this.width - this.imageWidth);
        return Math.max(centered, Math.min(panelSpace, maxLeft));
    }

    private void renderCostPanel(GuiGraphics graphics) {
        List<Component> summary = this.getCostSummary();
        int textWidth = this.costPanelTextWidth(summary);
        // Right-align the panel to the window so longer translations grow to the left instead of covering the slot.
        int x = this.leftPos - COST_PANEL_GAP - TOOLTIP_FRAME - textWidth - TOOLTIP_OFFSET;
        x = Math.max(x, TOOLTIP_FRAME + 1 - TOOLTIP_OFFSET);
        graphics.renderComponentTooltip(this.font, summary, x, this.topPos + COST_PANEL_Y);
    }

    // ---------------------------------------------------------------- scrolling list

    private int contentHeight() {
        return this.enchantmentSliders.isEmpty() ? 0 : this.enchantmentSliders.size() * ROW_HEIGHT - SLIDER_SPACING + 2;
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight() - LIST_HEIGHT);
    }

    private void clampScroll() {
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0, this.maxScroll());
    }

    private void layoutSliders() {
        int y = this.topPos + LIST_Y + 1 - (int) this.scrollAmount;
        for (EnchantmentSlider slider : this.enchantmentSliders) {
            slider.setX(this.leftPos + SLIDER_X);
            slider.setY(y);
            y += ROW_HEIGHT;
        }
    }

    private boolean isInList(double mouseX, double mouseY) {
        int x = this.leftPos + LIST_X;
        int y = this.topPos + LIST_Y;
        return mouseX >= x && mouseX < x + LIST_WIDTH && mouseY >= y && mouseY < y + LIST_HEIGHT;
    }

    private boolean isOnScrollbar(double mouseX, double mouseY) {
        int x = this.leftPos + SCROLLBAR_X;
        int y = this.topPos + LIST_Y;
        return this.maxScroll() > 0 && mouseX >= x && mouseX < x + SCROLLBAR_WIDTH && mouseY >= y && mouseY < y + LIST_HEIGHT;
    }

    private int scrollbarThumbHeight() {
        int content = Math.max(1, this.contentHeight());
        return Mth.clamp(LIST_HEIGHT * LIST_HEIGHT / content, 16, LIST_HEIGHT);
    }

    private void scrollToMouse(double mouseY) {
        int thumb = this.scrollbarThumbHeight();
        double track = LIST_HEIGHT - thumb;
        double relative = mouseY - (this.topPos + LIST_Y) - thumb / 2.0D;
        this.scrollAmount = track <= 0 ? 0 : relative / track * this.maxScroll();
        this.clampScroll();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isOnScrollbar(mouseX, mouseY)) {
            this.draggingScrollbar = true;
            this.scrollToMouse(mouseY);
            return true;
        }

        if (this.isInList(mouseX, mouseY)) {
            this.layoutSliders();
            for (EnchantmentSlider slider : this.enchantmentSliders) {
                if (slider.mouseClicked(mouseX, mouseY, button)) {
                    this.draggingSlider = slider;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            this.scrollToMouse(mouseY);
            return true;
        }

        if (this.draggingSlider != null) {
            this.draggingSlider.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }

        if (this.draggingSlider != null) {
            EnchantmentSlider slider = this.draggingSlider;
            this.draggingSlider = null;
            slider.mouseReleased(mouseX, mouseY, button);
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.isInList(mouseX, mouseY) || this.isOnScrollbar(mouseX, mouseY)) {
            this.scrollAmount -= delta * ROW_HEIGHT;
            this.clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    // ---------------------------------------------------------------- rendering

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderSliders(graphics, mouseX, mouseY, partialTick);
        this.renderCostPanel(graphics);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderSliders(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.clampScroll();
        this.layoutSliders();

        int x = this.leftPos + LIST_X;
        int y = this.topPos + LIST_Y;
        boolean mouseInList = this.isInList(mouseX, mouseY) || this.draggingSlider != null;
        int sliderMouseX = mouseInList ? mouseX : -1000;
        int sliderMouseY = mouseInList ? mouseY : -1000;

        graphics.enableScissor(x, y, x + LIST_WIDTH, y + LIST_HEIGHT);
        for (EnchantmentSlider slider : this.enchantmentSliders) {
            if (slider.getY() + SLIDER_HEIGHT >= y && slider.getY() <= y + LIST_HEIGHT) {
                slider.render(graphics, sliderMouseX, sliderMouseY, partialTick);
            }
        }
        graphics.disableScissor();

        int maxScroll = this.maxScroll();
        if (maxScroll > 0) {
            int barX = this.leftPos + SCROLLBAR_X;
            graphics.fill(barX, y, barX + SCROLLBAR_WIDTH, y + LIST_HEIGHT, 0xFF000000);
            int thumb = this.scrollbarThumbHeight();
            int thumbY = y + (int) (this.scrollAmount * (LIST_HEIGHT - thumb) / maxScroll);
            graphics.fill(barX, thumbY, barX + SCROLLBAR_WIDTH, thumbY + thumb, 0xFF808080);
            graphics.fill(barX, thumbY, barX + SCROLLBAR_WIDTH - 1, thumbY + thumb - 1, 0xFFC0C0C0);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawPanel(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        for (Slot slot : this.menu.slots) {
            drawSlot(graphics, this.leftPos + slot.x - 1, this.topPos + slot.y - 1);
        }
        // The texture is a 143x138 framed box (rest transparent). Draw it 12 px shorter by skipping
        // rows of its uniform interior, matching the shortened list.
        int bgX = this.leftPos + 64;
        int bgY = this.topPos + 16;
        int bottomRows = TABLE_BG_HEIGHT - TABLE_BG_TOP_ROWS - TABLE_BG_SKIPPED_ROWS;
        graphics.blit(TABLE_BACKGROUND, bgX, bgY, 0, 0, TABLE_BG_WIDTH, TABLE_BG_TOP_ROWS, 208, 160);
        graphics.blit(TABLE_BACKGROUND, bgX, bgY + TABLE_BG_TOP_ROWS, 0, TABLE_BG_TOP_ROWS + TABLE_BG_SKIPPED_ROWS,
                TABLE_BG_WIDTH, bottomRows, 208, 160);
    }

    /**
     * Vanilla-style light gray panel, like LibGui's default root panel background.
     */
    private static void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        int black = 0xFF000000;
        int white = 0xFFFFFFFF;
        int shadow = 0xFF555555;
        int fill = 0xFFC6C6C6;

        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
        // outline
        g.fill(x + 2, y, x + w - 2, y + 1, black);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, black);
        g.fill(x, y + 2, x + 1, y + h - 2, black);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, black);
        g.fill(x + 1, y + 1, x + 2, y + 2, black);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, black);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, black);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, black);
        // highlight (top/left) and shadow (bottom/right)
        g.fill(x + 2, y + 1, x + w - 3, y + 3, white);
        g.fill(x + 1, y + 2, x + 3, y + h - 3, white);
        g.fill(x + 3, y + h - 3, x + w - 2, y + h - 1, shadow);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 2, shadow);
        g.fill(x + 2, y + 2, x + 3, y + 3, white);
    }

    private static void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        g.fill(x, y, x + 17, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + 17, 0xFF373737);
        g.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        g.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }
}
