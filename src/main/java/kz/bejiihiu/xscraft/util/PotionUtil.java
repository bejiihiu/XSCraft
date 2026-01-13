package kz.bejiihiu.xscraft.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class PotionUtil {

    private PotionUtil() {}

    public static ItemStack createInvisibilityPotion(Material material) {
        if (material != Material.POTION && material != Material.SPLASH_POTION && material != Material.LINGERING_POTION) {
            throw new IllegalArgumentException("Unsupported potion material: " + material);
        }

        ItemStack stack = new ItemStack(material);
        if (!(stack.getItemMeta() instanceof PotionMeta pm)) {
            throw new IllegalStateException("Expected PotionMeta for material: " + material);
        }

        pm.setBasePotionType(PotionType.INVISIBILITY);
        stack.setItemMeta(pm);

        return stack;
    }

    public static boolean isInvisibilityPotion(ItemStack stack) {
        if (stack == null) {
            Debug.spamInfo("PotionUtil:null", 1000, "PotionUtil: stack=null — не INVISIBILITY.");
            return false;
        }

        Material material = stack.getType();
        if (material != Material.POTION && material != Material.SPLASH_POTION && material != Material.LINGERING_POTION) {
            Debug.spamInfo("PotionUtil:notPotionType", 1000,
                    "PotionUtil: тип не зелье (" + material + ") — не INVISIBILITY.");
            return false;
        }

        if (!(stack.getItemMeta() instanceof PotionMeta)) {
            Debug.spamInfo("PotionUtil:notPotionMeta", 1000,
                    "PotionUtil: meta не PotionMeta (" + stack.getType() + ") — не INVISIBILITY.");
            return false;
        }

        ItemStack reference = createInvisibilityPotion(material);
        boolean match = stack.isSimilar(reference);
        Debug.spamInfo("PotionUtil:similarCheck", 500,
                "PotionUtil: сравнение с эталоном INVISIBILITY (" + material + ") — " + match);
        return match;
    }
}
