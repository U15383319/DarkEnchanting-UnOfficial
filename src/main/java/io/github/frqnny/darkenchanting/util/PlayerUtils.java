package io.github.frqnny.darkenchanting.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

//Due to horrible vanilla methods, these should be used instead when dealing with xp
public class PlayerUtils {

    public static void syncExperience(Player player) {
        syncExperience(player, getTotalExperience(player));
    }

    public static void syncExperience(Player player, int totalExperience) {
        player.giveExperiencePoints(0);
        player.giveExperienceLevels(0);

        player.totalExperience = totalExperience;

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.giveExperiencePoints(0);
        }

    }

    public static int syncAndGetTotalExperience(Player player) {
        int totalExperience = getTotalExperience(player);
        syncExperience(player, totalExperience);

        return totalExperience;
    }

    public static int getTotalExperience(Player player) {
        int experienceLevel = player.experienceLevel;
        int experienceFromFullLevels = 0;

        //calculate all full level experience
        for (int currentLevel = (experienceLevel - 1); currentLevel >= 0; currentLevel--) {
            if (currentLevel >= 30) {
                experienceFromFullLevels += 112 + (currentLevel - 30) * 9;
            } else {
                experienceFromFullLevels += currentLevel >= 15 ? 37 + (currentLevel - 15) * 5 : 7 + currentLevel * 2;
            }
        }

        //calculate the last level, which is partial
        float nextLevelExperience = player.getXpNeededForNextLevel();
        nextLevelExperience *= player.experienceProgress;
        experienceFromFullLevels += nextLevelExperience;

        //this now contains what should be an accurate measure of total experience
        //based on experienceProgress and experienceLevel rather than desynced
        //totalExperience which tends to be bad measure of xp and leads to bugs
        return experienceFromFullLevels;
    }

    //supports negative and positive operations
    //Player#giveExperiencePoints only supports positive
    public static void modifyExperience(Player player, int experience) {

        player.increaseScore(Math.abs(experience));
        player.totalExperience = Mth.clamp(player.totalExperience + experience, 0, Integer.MAX_VALUE);

        //recalculate player level and experience progress
        int playerLevel = 0;
        int countExperience = player.totalExperience;
        int currentLevelExperience = getExperienceNeededForLevel(0);
        while (countExperience > currentLevelExperience) {
            playerLevel++;
            countExperience -= currentLevelExperience;
            currentLevelExperience = getExperienceNeededForLevel(playerLevel);
        }

        player.experienceLevel = playerLevel;
        player.experienceProgress = (float) countExperience / (float) getExperienceNeededForLevel(playerLevel);

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.giveExperiencePoints(0);
        }
    }

    //returns the xp needed to go up by 1 level from a current level, assuming experienceProgress is 0.0
    public static int getExperienceNeededForLevel(int experienceLevel) {
        if (experienceLevel >= 30) {
            return 112 + (experienceLevel - 30) * 9;
        } else {
            return experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2;
        }
    }
}
