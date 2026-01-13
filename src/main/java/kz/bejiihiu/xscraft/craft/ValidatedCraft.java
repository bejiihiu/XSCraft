package kz.bejiihiu.xscraft.craft;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public interface ValidatedCraft {
    NamespacedKey key();
    Recipe createRecipe();
    boolean isValid(CraftingInventory inv);
    ItemStack computeResult(CraftingInventory inv);
}
