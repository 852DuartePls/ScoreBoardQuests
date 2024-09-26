package me.duart.scoreBoardQuests.manager;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class MultiplierHandler {

    public boolean isMultiplier(ItemStack item) {
        if (item.getType() != Material.ORANGE_DYE) return false;

        for (int i = 2; i <= 4; i++) {
            NamespacedKey key = new NamespacedKey("fancyitemslite", "moneymultiplierby" + i);
            if (item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                return true;
            }
        }
        return false;
    }

    public int getMultiplierFromItem(ItemStack item) {
        for (int i = 2; i <= 4; i++) {
            NamespacedKey key = new NamespacedKey("fancyitemslite", "moneymultiplierby" + i);
            String multiplierString = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);

            if (multiplierString != null && multiplierString.contains("x")) {
                return switch (multiplierString) {
                    case "x2MoneyMultiplier" -> 2;
                    case "x3MoneyMultiplier" -> 3;
                    case "x4MoneyMultiplier" -> 4;
                    default -> 1;
                };
            }
        }
        return 1;
    }

    public int getItemsTotalMultiplier(Player player) {
        Set<Integer> uniqueMultipliers = new HashSet<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isMultiplier(item)) {
                uniqueMultipliers.add(getMultiplierFromItem(item));
            }
        }

        return uniqueMultipliers.stream().mapToInt(Integer::intValue).sum();
    }

    public int getQuestsMultiplier(int completedMissions) {
        return CustomScoreboardManager.getMultiplier(completedMissions);
    }

    public int getTotalMultiplier(Player player, int completedQuests) {
        int itemMultiplier = getItemsTotalMultiplier(player);
        int questMultiplier = getQuestsMultiplier(completedQuests);
        if (questMultiplier == 1 && itemMultiplier == 1) return 1;
        if (questMultiplier == 1 && itemMultiplier > 1) return itemMultiplier;
        if (questMultiplier > 1 && itemMultiplier == 1) return questMultiplier;
        return itemMultiplier + questMultiplier;
    }
}