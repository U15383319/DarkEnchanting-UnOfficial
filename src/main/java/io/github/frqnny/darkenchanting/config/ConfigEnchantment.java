package io.github.frqnny.darkenchanting.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ConfigEnchantment {
    public final String enchantmentId;
    public final boolean activated;
    public final float personalFactor;
    public final float personalReceiveFactor;

    private ConfigEnchantment(String enchantmentId, float personalFactor, boolean activated, float personalReceiveFactor) {
        this.enchantmentId = enchantmentId;
        this.personalFactor = personalFactor;
        this.activated = activated;
        this.personalReceiveFactor = personalReceiveFactor;
    }

    public static ConfigEnchantment of(String enchantmentId, float personalFactor, boolean activated, float personalReceiveFactor) {
        return new ConfigEnchantment(enchantmentId, personalFactor, activated, personalReceiveFactor);
    }

    /**
     * Parses a config entry of the form {@code enchantmentId,activated,personalFactor,personalReceiveFactor}.
     * Trailing values may be omitted and fall back to their defaults (true, 1.0, 1.0).
     *
     * @return the parsed entry, or {@code null} when the entry is malformed
     */
    @Nullable
    public static ConfigEnchantment parse(String entry) {
        String[] parts = entry.split(",");
        if (parts.length < 1 || parts.length > 4) {
            return null;
        }

        String id = parts[0].trim();
        if (id.isEmpty() || ResourceLocation.tryParse(id) == null) {
            return null;
        }

        try {
            boolean activated = true;
            if (parts.length > 1) {
                String value = parts[1].trim();
                if (value.equalsIgnoreCase("true")) {
                    activated = true;
                } else if (value.equalsIgnoreCase("false")) {
                    activated = false;
                } else {
                    return null;
                }
            }
            float personalFactor = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1.0F;
            float personalReceiveFactor = parts.length > 3 ? Float.parseFloat(parts[3].trim()) : 1.0F;
            return of(ResourceLocation.tryParse(id).toString(), personalFactor, activated, personalReceiveFactor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Optional<ConfigEnchantment> getConfigEnchantmentFor(ResourceLocation id) {
        if (id == null) {
            return Optional.empty();
        }

        for (ConfigEnchantment configEnchantment : DarkEnchantingConfig.configEnchantments()) {
            if (id.toString().equals(configEnchantment.enchantmentId)) {
                return Optional.of(configEnchantment);
            }
        }

        return Optional.empty();
    }

    public static Optional<ConfigEnchantment> getConfigEnchantmentFor(Enchantment enchantment) {
        return getConfigEnchantmentFor(ForgeRegistries.ENCHANTMENTS.getKey(enchantment));
    }
}
