package kz.bejiihiu.xscraft.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public final class PotionUtil {

    private PotionUtil() {}

    public static boolean isInvisibilityPotion(ItemStack stack) {
        if (stack == null) {
            Debug.spamInfo("PotionUtil:null", 1000, "PotionUtil: stack=null — не INVISIBILITY.");
            return false;
        }

        if (!(stack.getItemMeta() instanceof PotionMeta pm)) {
            Debug.spamInfo("PotionUtil:notPotionMeta", 1000,
                    "PotionUtil: meta не PotionMeta (" + stack.getType() + ") — не INVISIBILITY.");
            return false;
        }

        PotionType base = pm.getBasePotionType();
        if (base == PotionType.INVISIBILITY) {
            Debug.spamInfo("PotionUtil:baseOk", 500,
                    "PotionUtil: базовый тип зелья INVISIBILITY — подходит.");
            return true;
        }

        boolean custom = pm.hasCustomEffect(PotionEffectType.INVISIBILITY);
        Debug.spamInfo("PotionUtil:customCheck", 500,
                "PotionUtil: base=" + base + ", customInvisibility=" + custom);

        return custom;
    }
}
