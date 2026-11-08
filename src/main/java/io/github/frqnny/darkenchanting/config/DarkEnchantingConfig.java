package io.github.frqnny.darkenchanting.config;

import io.github.frqnny.darkenchanting.DarkEnchanting;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Forge replacement for the OmegaConfig file of the Fabric version.
 * <ul>
 *     <li>Client config ({@code config/dark_enchanting-client.toml}): visual-only options.</li>
 *     <li>Server config ({@code <world>/serverconfig/dark_enchanting-server.toml}): cost options. Forge syncs it to
 *     connecting clients, which replaces OmegaConfig's {@code @Syncing}.</li>
 * </ul>
 */
public final class DarkEnchantingConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    private static final ForgeConfigSpec.BooleanValue HAS_FANCY_SHRINE_PARTICLE;

    private static final ForgeConfigSpec.IntValue BASE_EXPERIENCE_COST;
    private static final ForgeConfigSpec.DoubleValue COST_FACTOR;
    private static final ForgeConfigSpec.DoubleValue RECEIVE_FACTOR;
    private static final ForgeConfigSpec.DoubleValue CURSE_FACTOR;
    private static final ForgeConfigSpec.DoubleValue TREASURE_FACTOR;
    private static final ForgeConfigSpec.DoubleValue WEIGHT_FACTOR;
    private static final ForgeConfigSpec.DoubleValue REPAIR_FACTOR;
    private static final ForgeConfigSpec.DoubleValue BOOKSHELVES_DISCOUNT;
    private static final ForgeConfigSpec.DoubleValue CONDUIT_DISCOUNT;
    private static final ForgeConfigSpec.BooleanValue REJECT_ABOVE_MAX_LEVEL;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CONFIG_ENCHANTMENTS;

    private static volatile List<ConfigEnchantment> parsedEnchantments;

    static {
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        HAS_FANCY_SHRINE_PARTICLE = client
                .comment("Performance: Decide whether Dark Conduits emit particles.", "True means they do emit particles.")
                .define("hasFancyShrineParticle", true);
        CLIENT_SPEC = client.build();

        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();
        BASE_EXPERIENCE_COST = server
                .comment("Base experience cost for each enchantment.", "All enchantments will be worth at least this much.")
                .defineInRange("baseExperienceCost", 30, 0, Integer.MAX_VALUE);
        COST_FACTOR = server
                .comment("Each enchantment's cost is multiplied by this value.")
                .defineInRange("costFactor", 1.0D, 0.0D, Double.MAX_VALUE);
        RECEIVE_FACTOR = server
                .comment("Removing an enchantment from gear will give XP back.", "The amount received back is multiplied by this value.")
                .defineInRange("receiveFactor", 0.49D, 0.0D, Double.MAX_VALUE);
        CURSE_FACTOR = server
                .comment("Each curse enchantment's cost is multiplied by this value.", "Curse Enchantments will show up as red on the Dark Enchanter.")
                .defineInRange("curseFactor", 3.0D, 0.0D, Double.MAX_VALUE);
        TREASURE_FACTOR = server
                .comment("Each TREASURE enchantment's cost is multiplied by this value.", "TREASURE Enchantments will show up as blue on the Dark Enchanter.")
                .defineInRange("treasureFactor", 4.0D, 0.0D, Double.MAX_VALUE);
        WEIGHT_FACTOR = server
                .comment("How much an enchantment's inverted weight should be multiplied with.",
                        "Each enchantment has a Rarity, and each Rarity has a weight.",
                        "You can check Enchantment weights on many Minecraft wikis.",
                        "Inverted weight is calculated through (11.0 - Weight),",
                        "and we multiply the inverted weight with the cost,",
                        "so that cost is affected by the rarity of the Enchantment.",
                        "This weightFactor allows you to modify the inverted weight.")
                .defineInRange("weightFactor", 3.0D, 0.0D, Double.MAX_VALUE);
        REPAIR_FACTOR = server
                .comment("Repairing any item will have its cost multiplied by this value.")
                .defineInRange("repairFactor", 1.0D, 0.0D, Double.MAX_VALUE);
        BOOKSHELVES_DISCOUNT = server
                .comment("This number represents the discount from bookshelves.", "Default: 0.4 (40%)")
                .defineInRange("bookshelvesDiscount", 0.4D, 0.0D, 1.0D);
        CONDUIT_DISCOUNT = server
                .comment("This number represents the discount from Dark Conduits.", "Default: 0.1 (10%)")
                .defineInRange("conduitDiscount", 0.1D, 0.0D, 1.0D);
        REJECT_ABOVE_MAX_LEVEL = server
                .comment("As a security feature, the client can no longer send packets to the server requesting above",
                        "maximum enchantment levels.",
                        "However, improper implementation from other mods can result in this check leading to undesirable effects.",
                        "For example, mods that remove the limit will likely want this config option off to ensure compatibility.")
                .define("shouldRejectEnchantmentAttemptsAboveMaxValue", true);
        CONFIG_ENCHANTMENTS = server
                .comment("This list can be used to configure specific enchantments.",
                        "Each entry has the format \"enchantmentId,activated,personalFactor,personalReceiveFactor\":",
                        "    enchantmentId: The In-Game identification of the Enchantment. No Default.",
                        "    activated: Determines whether the enchantment will be available in the Dark Enchanter. Default: true",
                        "    personalFactor: During transactions, the cost of the specific enchantment is multiplied by this value. Default: 1.0",
                        "    personalReceiveFactor: Removing enchantments allows you to receive back some XP. The amount received is multiplied by this value. Default: 1.0",
                        "The default list contains an example entry for Sharpness with its defaults,",
                        "and a Mending entry that gives back less XP when disenchanting, to avoid Mending XP farming.")
                .defineListAllowEmpty(List.of("configEnchantments"),
                        () -> List.of("minecraft:sharpness,true,1.0,1.0", "minecraft:mending,true,1.0,0.5"),
                        o -> o instanceof String && ConfigEnchantment.parse((String) o) != null);
        SERVER_SPEC = server.build();
    }

    private DarkEnchantingConfig() {
    }

    public static void register(IEventBus modBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        modBus.addListener(DarkEnchantingConfig::onConfigLoad);
        modBus.addListener(DarkEnchantingConfig::onConfigReload);
    }

    private static void onConfigLoad(ModConfig.Loading event) {
        invalidate(event.getConfig());
    }

    private static void onConfigReload(ModConfig.Reloading event) {
        invalidate(event.getConfig());
    }

    private static void invalidate(ModConfig config) {
        if (DarkEnchanting.MODID.equals(config.getModId()) && config.getSpec() == SERVER_SPEC) {
            parsedEnchantments = null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(ForgeConfigSpec spec, ForgeConfigSpec.ConfigValue<T> value) {
        // Server config is only loaded while a world is open; fall back to defaults otherwise.
        // (1.18.2's ConfigValue has no getDefault(), so read it from the value's spec entry.)
        if (spec.isLoaded()) {
            return value.get();
        }
        ForgeConfigSpec.ValueSpec valueSpec = spec.getSpec().get(value.getPath());
        return (T) valueSpec.getDefault();
    }

    public static boolean hasFancyShrineParticle() {
        return get(CLIENT_SPEC, HAS_FANCY_SHRINE_PARTICLE);
    }

    public static int baseExperienceCost() {
        return get(SERVER_SPEC, BASE_EXPERIENCE_COST);
    }

    public static float costFactor() {
        return get(SERVER_SPEC, COST_FACTOR).floatValue();
    }

    public static float receiveFactor() {
        return get(SERVER_SPEC, RECEIVE_FACTOR).floatValue();
    }

    public static float curseFactor() {
        return get(SERVER_SPEC, CURSE_FACTOR).floatValue();
    }

    public static float treasureFactor() {
        return get(SERVER_SPEC, TREASURE_FACTOR).floatValue();
    }

    public static float weightFactor() {
        return get(SERVER_SPEC, WEIGHT_FACTOR).floatValue();
    }

    public static float repairFactor() {
        return get(SERVER_SPEC, REPAIR_FACTOR).floatValue();
    }

    public static float bookshelvesDiscount() {
        return get(SERVER_SPEC, BOOKSHELVES_DISCOUNT).floatValue();
    }

    public static float conduitDiscount() {
        return get(SERVER_SPEC, CONDUIT_DISCOUNT).floatValue();
    }

    public static boolean shouldRejectEnchantmentAttemptsAboveMaxValue() {
        return get(SERVER_SPEC, REJECT_ABOVE_MAX_LEVEL);
    }

    public static List<ConfigEnchantment> configEnchantments() {
        List<ConfigEnchantment> cached = parsedEnchantments;
        if (cached == null) {
            List<ConfigEnchantment> list = new ArrayList<>();
            for (String entry : get(SERVER_SPEC, CONFIG_ENCHANTMENTS)) {
                ConfigEnchantment parsed = ConfigEnchantment.parse(entry);
                if (parsed != null) {
                    list.add(parsed);
                }
            }
            cached = Collections.unmodifiableList(list);
            if (SERVER_SPEC.isLoaded()) {
                parsedEnchantments = cached;
            }
        }
        return cached;
    }
}
