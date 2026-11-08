package io.github.frqnny.darkenchanting.gametest;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import io.github.frqnny.darkenchanting.DarkEnchanting;
import io.github.frqnny.darkenchanting.init.ModBlocks;
import io.github.frqnny.darkenchanting.menu.DarkEnchanterMenu;
import io.github.frqnny.darkenchanting.network.ApplyEnchantmentsPacket;
import io.github.frqnny.darkenchanting.network.ApplyRepairPacket;
import io.github.frqnny.darkenchanting.util.BookcaseUtils;
import io.github.frqnny.darkenchanting.util.CostUtils;
import io.github.frqnny.darkenchanting.util.EnchantingUtils;
import io.github.frqnny.darkenchanting.util.PlayerUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Compatibility tests against Majrusz's Enchantments (dev-only, see build.gradle: devMods / gameTestServer).
 * They exercise the same code the Dark Enchanter uses: the list filter shown by the screen and the server-side
 * enchant handler.
 */
@GameTestHolder(DarkEnchanting.MODID)
@PrefixGameTestTemplate(false)
public class MajruszCompatTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MAJRUSZ = "majruszsenchantments";
    private static final String TEMPLATE = "empty";

    private static final List<Item> SAMPLE_ITEMS = List.of(
            Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE,
            Items.GOLDEN_SWORD, Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_HOE,
            Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS,
            Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
            Items.BOW, Items.CROSSBOW, Items.TRIDENT, Items.FISHING_ROD, Items.SHIELD, Items.SHEARS,
            Items.FLINT_AND_STEEL, Items.ELYTRA, Items.TURTLE_HELMET, Items.CARROT_ON_A_STICK,
            Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND_HORSE_ARMOR, Items.BOOK);

    private static List<Enchantment> majruszEnchantments() {
        return ForgeRegistries.ENCHANTMENTS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(MAJRUSZ))
                .map(Map.Entry::getValue)
                .toList();
    }

    private static String id(Enchantment enchantment) {
        ResourceLocation key = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return key == null ? enchantment.toString() : key.getPath();
    }

    private static Object2IntMap<Enchantment> none() {
        return new Object2IntOpenHashMap<>();
    }

    /**
     * A survival (non-creative) server player, so XP is really checked and charged. It gets a packet listener on a
     * channel-less connection (outgoing packets are just queued) instead of PlayerList#placeNewPlayer, which Forge
     * patches to access the network pipeline.
     */
    private static ServerPlayer survivalPlayer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = new ServerPlayer(level.getServer(), level, new GameProfile(UUID.randomUUID(), "dark-enchanting-test"));
        new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player);
        return player;
    }

    @GameTest(template = TEMPLATE)
    public static void majruszEnchantmentsAreRegistered(GameTestHelper helper) {
        List<Enchantment> majrusz = majruszEnchantments();
        if (majrusz.isEmpty()) {
            helper.fail("No enchantments from " + MAJRUSZ + " are registered");
            return;
        }
        LOGGER.info("[compat] {} Majrusz enchantments registered: {}", majrusz.size(),
                majrusz.stream().map(e -> id(e) + (e.isCurse() ? "(curse)" : "") + " max=" + e.getMaxLevel()).collect(Collectors.joining(", ")));
        helper.succeed();
    }

    /**
     * Every Majrusz enchantment that can go on an item must be offered by the Dark Enchanter for that item, and the
     * enchanter slot must accept every item Majrusz makes enchantable (shields and horse armor via mixins).
     */
    @GameTest(template = TEMPLATE)
    public static void applicableMajruszEnchantmentsAreListed(GameTestHelper helper) {
        List<Enchantment> majrusz = majruszEnchantments();
        Set<Enchantment> offeredSomewhere = new LinkedHashSet<>();
        List<String> problems = new ArrayList<>();

        for (Item item : SAMPLE_ITEMS) {
            ItemStack stack = new ItemStack(item);
            List<Enchantment> available = EnchantingUtils.getAvailableEnchantments(stack, none(), none());
            List<String> offered = new ArrayList<>();
            for (Enchantment enchantment : majrusz) {
                if (!enchantment.canEnchant(stack)) {
                    continue;
                }
                if (!DarkEnchanterMenu.isValidItem(stack)) {
                    problems.add(item + " accepts " + id(enchantment) + " but the enchanter slot rejects the item");
                }
                if (!available.contains(enchantment)) {
                    problems.add(id(enchantment) + " can enchant " + item + " but is not listed");
                } else {
                    offeredSomewhere.add(enchantment);
                    offered.add(id(enchantment));
                }
            }
            if (!offered.isEmpty()) {
                LOGGER.info("[compat] {} -> {}", item, offered);
            }
        }

        List<String> notOffered = majrusz.stream().filter(e -> !offeredSomewhere.contains(e)).map(MajruszCompatTests::id).toList();
        LOGGER.info("[compat] Majrusz enchantments not applicable to any sample item: {}", notOffered);

        for (Item mixinItem : List.of(Items.SHIELD, Items.DIAMOND_HORSE_ARMOR)) {
            ItemStack stack = new ItemStack(mixinItem);
            boolean anyMajrusz = EnchantingUtils.getAvailableEnchantments(stack, none(), none()).stream().anyMatch(majrusz::contains);
            if (!anyMajrusz) {
                problems.add("No Majrusz enchantment offered for " + mixinItem);
            }
        }

        if (problems.isEmpty()) {
            helper.succeed();
        } else {
            helper.fail(String.join("; ", problems));
        }
    }

    /** Selecting a Majrusz enchantment must hide every enchantment that is incompatible with it (both directions). */
    @GameTest(template = TEMPLATE)
    public static void incompatibleEnchantmentsAreHidden(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (Item item : SAMPLE_ITEMS) {
            ItemStack stack = new ItemStack(item);
            for (Enchantment selected : EnchantingUtils.getAvailableEnchantments(stack, none(), none())) {
                if (!majruszEnchantments().contains(selected)) {
                    continue;
                }
                Object2IntMap<Enchantment> toApply = none();
                toApply.put(selected, selected.getMaxLevel());
                List<Enchantment> remaining = EnchantingUtils.getAvailableEnchantments(stack, toApply, none());
                if (!remaining.contains(selected)) {
                    problems.add(id(selected) + " disappeared after selecting it on " + item);
                }
                for (Enchantment other : remaining) {
                    if (other != selected && !(selected.isCompatibleWith(other) && other.isCompatibleWith(selected))) {
                        problems.add(id(other) + " still listed next to incompatible " + id(selected) + " on " + item);
                    }
                }
            }
        }

        if (problems.isEmpty()) {
            helper.succeed();
        } else {
            helper.fail(String.join("; ", problems));
        }
    }

    /**
     * End-to-end server path: put an item in a Dark Enchanter menu, apply a compatible set of Majrusz enchantments at
     * max level through the packet handler, and check the returned item and the charged XP. Then remove them again
     * and check that XP is refunded.
     */
    @GameTest(template = TEMPLATE)
    public static void enchantAndDisenchantThroughMenu(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, ModBlocks.DARK_ENCHANTER.get());
        BlockPos pos = helper.absolutePos(rel);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = survivalPlayer(helper);
        // The server only accepts enchant requests from players within reach of the block (menu#stillValid).
        player.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        List<String> problems = new ArrayList<>();
        int itemsTested = 0;

        for (Item item : SAMPLE_ITEMS) {
            ItemStack stack = new ItemStack(item);
            Object2IntMap<Enchantment> chosen = none();
            for (Enchantment enchantment : EnchantingUtils.getAvailableEnchantments(stack, none(), none())) {
                if (majruszEnchantments().contains(enchantment)
                        && EnchantingUtils.getAvailableEnchantments(stack, chosen, none()).contains(enchantment)) {
                    chosen.put(enchantment, enchantment.getMaxLevel());
                }
            }
            if (chosen.isEmpty()) {
                continue;
            }
            itemsTested++;

            // --- enchant
            player.getInventory().clearContent();
            player.experienceProgress = 0.0F;
            player.setExperienceLevels(1000);
            int expectedCost = BookcaseUtils.applyDiscount(CostUtils.getExperienceCost(chosen, none()), level, pos);
            int xpBefore = PlayerUtils.getTotalExperience(player);

            ItemStack result = runEnchant(player, level, pos, stack, chosen);
            int charged = xpBefore - PlayerUtils.getTotalExperience(player);
            Map<Enchantment, Integer> applied = result.isEmpty() ? Map.of() : EnchantmentHelper.getEnchantments(result);

            if (result.isEmpty()) {
                problems.add(item + ": menu closed or item missing from the slot after enchanting");
                continue;
            }
            for (var entry : chosen.object2IntEntrySet()) {
                Integer level1 = applied.get(entry.getKey());
                if (level1 == null || level1 != entry.getIntValue()) {
                    problems.add(item + ": expected " + id(entry.getKey()) + " " + entry.getIntValue() + " but got " + level1);
                }
            }
            if (expectedCost <= 0 || Math.abs(charged - expectedCost) > 1) {
                problems.add(item + ": expected to charge " + expectedCost + " XP but charged " + charged);
            }

            // --- disenchant everything again (refund)
            Object2IntMap<Enchantment> onStack = new Object2IntOpenHashMap<>(applied);
            Object2IntMap<Enchantment> remove = none();
            for (Enchantment enchantment : onStack.keySet()) {
                remove.put(enchantment, 0);
            }
            int expectedRefund = -BookcaseUtils.applyDiscount(CostUtils.getExperienceCost(remove, onStack), level, pos);
            int xpBeforeRemove = PlayerUtils.getTotalExperience(player);
            player.getInventory().clearContent();
            ItemStack cleared = runEnchant(player, level, pos, result, remove);
            int refunded = PlayerUtils.getTotalExperience(player) - xpBeforeRemove;

            if (cleared.isEmpty() || !EnchantmentHelper.getEnchantments(cleared).isEmpty()) {
                problems.add(item + ": enchantments were not removed");
            }
            if (expectedRefund <= 0 || Math.abs(refunded - expectedRefund) > 1) {
                problems.add(item + ": expected to refund " + expectedRefund + " XP but refunded " + refunded);
            }

            LOGGER.info("[compat] {}: applied {} for {} XP, removed for {} XP back", item,
                    chosen.object2IntEntrySet().stream().map(e -> id(e.getKey()) + " " + e.getIntValue()).toList(), charged, refunded);
        }

        if (itemsTested == 0) {
            problems.add("no sample item accepted any Majrusz enchantment");
        }
        if (problems.isEmpty()) {
            helper.succeed();
        } else {
            helper.fail(String.join("; ", problems));
        }
    }

    private static ItemStack runEnchant(ServerPlayer player, ServerLevel level, BlockPos pos, ItemStack stack, Object2IntMap<Enchantment> enchantments) {
        DarkEnchanterMenu menu = new DarkEnchanterMenu(1, player.getInventory(), ContainerLevelAccess.create(level, pos));
        player.containerMenu = menu;
        menu.getSlot(0).set(stack.copy());

        ApplyEnchantmentsPacket.apply(player, enchantments);

        // Like the vanilla enchanting table, the menu stays open and the item stays in its slot.
        if (player.containerMenu != menu) {
            return ItemStack.EMPTY;
        }
        return menu.getEnchantStack().copy();
    }

    /**
     * Repairing keeps the menu open with the repaired item in the slot, and requests from a player out of reach of
     * the block are ignored.
     */
    @GameTest(template = TEMPLATE)
    public static void repairKeepsMenuOpenAndOutOfReachIsIgnored(GameTestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, ModBlocks.DARK_ENCHANTER.get());
        BlockPos pos = helper.absolutePos(rel);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = survivalPlayer(helper);
        player.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        player.experienceProgress = 0.0F;
        player.setExperienceLevels(100);

        // --- repair
        ItemStack damaged = new ItemStack(Items.DIAMOND_PICKAXE);
        damaged.setDamageValue(500);
        int expectedCost = BookcaseUtils.applyDiscount(CostUtils.getRepairCost(damaged), level, pos);
        DarkEnchanterMenu menu = new DarkEnchanterMenu(1, player.getInventory(), ContainerLevelAccess.create(level, pos));
        player.containerMenu = menu;
        menu.getSlot(0).set(damaged.copy());
        int xpBefore = PlayerUtils.getTotalExperience(player);

        ApplyRepairPacket.apply(player);

        int charged = xpBefore - PlayerUtils.getTotalExperience(player);
        if (player.containerMenu != menu) {
            helper.fail("Menu was closed after repairing");
            return;
        }
        if (menu.getEnchantStack().isDamaged()) {
            helper.fail("Item in the slot was not repaired");
            return;
        }
        if (Math.abs(charged - expectedCost) > 1) {
            helper.fail("Expected repair to cost " + expectedCost + " XP but charged " + charged);
            return;
        }

        // --- out of reach: nothing happens
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        Enchantment enchantment = EnchantingUtils.getAvailableEnchantments(sword, none(), none()).get(0);
        Object2IntMap<Enchantment> request = none();
        request.put(enchantment, 1);
        DarkEnchanterMenu farMenu = new DarkEnchanterMenu(2, player.getInventory(), ContainerLevelAccess.create(level, pos));
        player.containerMenu = farMenu;
        farMenu.getSlot(0).set(sword.copy());
        player.moveTo(pos.getX() + 50.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        int xpBeforeFar = PlayerUtils.getTotalExperience(player);

        ApplyEnchantmentsPacket.apply(player, request);

        if (farMenu.getEnchantStack().isEnchanted() || PlayerUtils.getTotalExperience(player) != xpBeforeFar) {
            helper.fail("Enchant request from a player out of reach was applied");
            return;
        }

        LOGGER.info("[compat] repair kept the menu open ({} XP); out-of-reach request ignored", charged);
        helper.succeed();
    }

    /** Enchantments switched off in Majrusz's own config must disappear from the Dark Enchanter. */
    @GameTest(template = TEMPLATE)
    public static void enchantmentsDisabledInMajruszConfigAreHidden(GameTestHelper helper) {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        Enchantment target = EnchantingUtils.getAvailableEnchantments(sword, none(), none()).stream()
                .filter(majruszEnchantments()::contains)
                .findFirst()
                .orElse(null);
        if (target == null) {
            helper.fail("No Majrusz enchantment available for a diamond sword");
            return;
        }

        try {
            registerAvailabilityOverride();
            disabledForTest = target;
            try {
                if (EnchantingUtils.getAvailableEnchantments(sword, none(), none()).contains(target)) {
                    helper.fail(id(target) + " is still listed after being disabled in Majrusz's config");
                    return;
                }
            } finally {
                disabledForTest = null;
            }
        } catch (ReflectiveOperationException e) {
            helper.fail("Could not disable " + id(target) + ": " + e);
            return;
        }

        if (!EnchantingUtils.getAvailableEnchantments(sword, none(), none()).contains(target)) {
            helper.fail(id(target) + " did not come back after being re-enabled");
            return;
        }

        LOGGER.info("[compat] {} hidden while disabled in Majrusz's config", id(target));
        helper.succeed();
    }

    // Majrusz Library 5.x (1.18.2) has no CustomEnchantment#setEnabled. Whether an enchantment is enabled is decided
    // by OnEnchantmentAvailabilityCheck listeners (its config switch is one of them), checked inside canEnchant.
    // Register one extra listener, run last, that switches off the enchantment in disabledForTest.
    private static volatile Enchantment disabledForTest;
    private static boolean availabilityOverrideRegistered;

    private static synchronized void registerAvailabilityOverride() throws ReflectiveOperationException {
        if (availabilityOverrideRegistered) {
            return;
        }
        Class<?> check = Class.forName("com.mlib.contexts.OnEnchantmentAvailabilityCheck");
        Class<?> data = Class.forName("com.mlib.contexts.OnEnchantmentAvailabilityCheck$Data");
        Class<?> priority = Class.forName("com.mlib.contexts.base.Priority");
        Field enchantmentField = data.getField("enchantment");
        Field availableField = data.getField("isAvailable");

        Consumer<Object> listener = event -> {
            try {
                Enchantment disabled = disabledForTest;
                if (disabled != null && enchantmentField.get(event) == disabled) {
                    availableField.setBoolean(event, false);
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        };
        Object context = check.getMethod("listen", Consumer.class).invoke(null, listener);
        context.getClass().getMethod("priority", priority).invoke(context, priority.getField("LOWEST").get(null));
        availabilityOverrideRegistered = true;
    }
}
