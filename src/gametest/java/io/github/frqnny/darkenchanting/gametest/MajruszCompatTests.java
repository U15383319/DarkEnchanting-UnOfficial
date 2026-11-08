package io.github.frqnny.darkenchanting.gametest;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Compatibility tests against Majrusz's Enchantments (dev-only, see build.gradle: devMods / runServer -PcompatTests).
 * They exercise the same code the Dark Enchanter uses: the list filter shown by the screen and the server-side
 * enchant handler. On 1.16.5 the mod is still called "Wonderful Enchantments" (mod id wonderful_enchantments).
 * 1.16.5 has no GameTest framework; {@link CompatTestRunner} runs every public static method taking a
 * {@link TestHelper}.
 */
public class MajruszCompatTests {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String MAJRUSZ = "wonderful_enchantments";

    // Looked up by id: Wonderful Enchantments replaces the vanilla shield and horse armor items in the registry.
    private static final List<String> SAMPLE_ITEM_IDS = List.of(
            "diamond_sword", "diamond_axe", "diamond_pickaxe", "diamond_shovel", "diamond_hoe",
            "golden_sword", "golden_axe", "golden_pickaxe", "golden_shovel", "golden_hoe",
            "golden_helmet", "golden_chestplate", "golden_leggings", "golden_boots",
            "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
            "bow", "crossbow", "trident", "fishing_rod", "shield", "shears",
            "flint_and_steel", "elytra", "turtle_helmet", "carrot_on_a_stick",
            "iron_horse_armor", "golden_horse_armor", "diamond_horse_armor", "book");

    private static Item item(String id) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
    }

    private static List<Item> sampleItems() {
        return SAMPLE_ITEM_IDS.stream().map(MajruszCompatTests::item).collect(Collectors.toList());
    }

    private static List<Enchantment> majruszEnchantments() {
        return ForgeRegistries.ENCHANTMENTS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(MAJRUSZ))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
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
    private static ServerPlayerEntity survivalPlayer(TestHelper helper) {
        ServerWorld level = helper.getLevel();
        ServerPlayerEntity player = new ServerPlayerEntity(level.getServer(), level, new GameProfile(UUID.randomUUID(), "dark-enchanting-test"),
                new PlayerInteractionManager(level));
        new ServerPlayNetHandler(level.getServer(), new NetworkManager(PacketDirection.SERVERBOUND), player);
        return player;
    }

    public static void majruszEnchantmentsAreRegistered(TestHelper helper) {
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
    public static void applicableMajruszEnchantmentsAreListed(TestHelper helper) {
        List<Enchantment> majrusz = majruszEnchantments();
        Set<Enchantment> offeredSomewhere = new LinkedHashSet<>();
        List<String> problems = new ArrayList<>();

        for (Item item : sampleItems()) {
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

        List<String> notOffered = majrusz.stream().filter(e -> !offeredSomewhere.contains(e)).map(MajruszCompatTests::id).collect(Collectors.toList());
        LOGGER.info("[compat] Majrusz enchantments not applicable to any sample item: {}", notOffered);

        for (Item mixinItem : List.of(item("shield"), item("diamond_horse_armor"))) {
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
    public static void incompatibleEnchantmentsAreHidden(TestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (Item item : sampleItems()) {
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
    public static void enchantAndDisenchantThroughMenu(TestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, ModBlocks.DARK_ENCHANTER.get());
        BlockPos pos = helper.absolutePos(rel);
        ServerWorld level = helper.getLevel();
        ServerPlayerEntity player = survivalPlayer(helper);
        // The server only accepts enchant requests from players within reach of the block (menu#stillValid).
        player.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        List<String> problems = new ArrayList<>();
        int itemsTested = 0;

        for (Item item : sampleItems()) {
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
            player.inventory.clearContent();
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
            player.inventory.clearContent();
            ItemStack cleared = runEnchant(player, level, pos, result, remove);
            int refunded = PlayerUtils.getTotalExperience(player) - xpBeforeRemove;

            if (cleared.isEmpty() || !EnchantmentHelper.getEnchantments(cleared).isEmpty()) {
                problems.add(item + ": enchantments were not removed");
            }
            if (expectedRefund <= 0 || Math.abs(refunded - expectedRefund) > 1) {
                problems.add(item + ": expected to refund " + expectedRefund + " XP but refunded " + refunded);
            }

            LOGGER.info("[compat] {}: applied {} for {} XP, removed for {} XP back", item,
                    chosen.object2IntEntrySet().stream().map(e -> id(e.getKey()) + " " + e.getIntValue()).collect(Collectors.toList()), charged, refunded);
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

    private static ItemStack runEnchant(ServerPlayerEntity player, ServerWorld level, BlockPos pos, ItemStack stack, Object2IntMap<Enchantment> enchantments) {
        DarkEnchanterMenu menu = new DarkEnchanterMenu(1, player.inventory, IWorldPosCallable.create(level, pos));
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
    public static void repairKeepsMenuOpenAndOutOfReachIsIgnored(TestHelper helper) {
        BlockPos rel = new BlockPos(1, 1, 1);
        helper.setBlock(rel, ModBlocks.DARK_ENCHANTER.get());
        BlockPos pos = helper.absolutePos(rel);
        ServerWorld level = helper.getLevel();
        ServerPlayerEntity player = survivalPlayer(helper);
        player.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
        player.experienceProgress = 0.0F;
        player.setExperienceLevels(100);

        // --- repair
        ItemStack damaged = new ItemStack(item("diamond_pickaxe"));
        damaged.setDamageValue(500);
        int expectedCost = BookcaseUtils.applyDiscount(CostUtils.getRepairCost(damaged), level, pos);
        DarkEnchanterMenu menu = new DarkEnchanterMenu(1, player.inventory, IWorldPosCallable.create(level, pos));
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
        ItemStack sword = new ItemStack(item("diamond_sword"));
        Enchantment enchantment = EnchantingUtils.getAvailableEnchantments(sword, none(), none()).get(0);
        Object2IntMap<Enchantment> request = none();
        request.put(enchantment, 1);
        DarkEnchanterMenu farMenu = new DarkEnchanterMenu(2, player.inventory, IWorldPosCallable.create(level, pos));
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
    public static void enchantmentsDisabledInMajruszConfigAreHidden(TestHelper helper) {
        ItemStack sword = new ItemStack(item("diamond_sword"));
        Enchantment target = EnchantingUtils.getAvailableEnchantments(sword, none(), none()).stream()
                .filter(majruszEnchantments()::contains)
                .findFirst()
                .orElse(null);
        if (target == null) {
            helper.fail("No Majrusz enchantment available for a diamond sword");
            return;
        }

        try {
            ForgeConfigSpec.BooleanValue availability = availabilitySwitch(target);
            boolean original = availability.get();
            availability.set(false);
            try {
                if (EnchantingUtils.getAvailableEnchantments(sword, none(), none()).contains(target)) {
                    helper.fail(id(target) + " is still listed after being disabled in Majrusz's config");
                    return;
                }
            } finally {
                availability.set(original);
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

    /**
     * Wonderful Enchantments (1.16.5) decides availability with an AvailabilityConfig ("is_enabled" in its config),
     * checked inside canEnchant via isDisabled(). Return that config's Forge value so the test can switch it.
     */
    private static ForgeConfigSpec.BooleanValue availabilitySwitch(Enchantment enchantment) throws ReflectiveOperationException {
        for (Class<?> type = enchantment.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField("availabilityConfig");
                field.setAccessible(true);
                Object availabilityConfig = field.get(enchantment);
                return (ForgeConfigSpec.BooleanValue) availabilityConfig.getClass().getField("booleanValue").get(availabilityConfig);
            } catch (NoSuchFieldException ignored) {
                // keep looking in the superclass
            }
        }
        throw new NoSuchFieldException("availabilityConfig in " + enchantment.getClass().getName());
    }
}
