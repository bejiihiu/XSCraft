package kz.bejiihiu.xscraft.features.invisibleframes;

import kz.bejiihiu.xscraft.core.Keys;
import kz.bejiihiu.xscraft.craft.ValidatedCraft;
import kz.bejiihiu.xscraft.util.Debug;
import kz.bejiihiu.xscraft.util.PotionUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

public record InvisibleFrameCraft(NamespacedKey key, Keys keys, Material frameType) implements ValidatedCraft {

    public InvisibleFrameCraft(NamespacedKey key, Keys keys, Material frameType) {
        this.key = key;
        this.keys = keys;
        this.frameType = frameType;

        Debug.info("InvisibleFrameCraft создан: key=" + key + ", frameType=" + frameType);
    }

    @Override
    public Recipe createRecipe() {
        Debug.info("InvisibleFrameCraft.createRecipe(): создаю рецепт key=" + key + ", frameType=" + frameType);

        ItemStack result = InvisibleFrameItems.createTaggedFrame(keys, frameType);

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(frameType);
        recipe.addIngredient(new RecipeChoice.MaterialChoice(
                Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION
        ));

        Debug.info("InvisibleFrameCraft.createRecipe(): рецепт создан (шаблон: frame + any potion). Валидация — отдельно.");
        return recipe;
    }

    @Override
    public boolean isValid(CraftingInventory inv) {
        if (inv == null) {
            Debug.warn("InvisibleFrameCraft.isValid(): inv=null — false.");
            return false;
        }

        int frames = 0;
        int potions = 0;
        ItemStack potionStack = null;

        ItemStack[] matrix = inv.getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            ItemStack it = matrix[i];
            if (it == null || it.getType() == Material.AIR) continue;

            Material t = it.getType();

            if (t == frameType) {
                frames += it.getAmount();
                continue;
            }

            if (t == Material.POTION || t == Material.SPLASH_POTION || t == Material.LINGERING_POTION) {
                potions += it.getAmount();
                potionStack = it;
                continue;
            }

            Debug.spamInfo("InvisibleFrameCraft.extraItem:" + key, 300,
                    "InvisibleFrameCraft.isValid(): лишний предмет в матрице (slot=" + i + ", type=" + t + ") — false.");
            return false;
        }

        if (frames != 1 || potions != 1) {
            Debug.spamInfo("InvisibleFrameCraft.badCounts:" + key, 300,
                    "InvisibleFrameCraft.isValid(): неверные количества. frames=" + frames + ", potions=" + potions + " — false.");
            return false;
        }

        boolean invis = PotionUtil.isInvisibilityPotion(potionStack);
        Debug.spamInfo("InvisibleFrameCraft.potionCheck:" + key, 300,
                "InvisibleFrameCraft.isValid(): проверка зелья INVISIBILITY=" + invis + " — " + (invis ? "OK" : "FAIL"));

        return invis;
    }

    @Override
    public ItemStack computeResult(CraftingInventory inv) {
        Debug.spamInfo("InvisibleFrameCraft.computeResult:" + key, 500,
                "InvisibleFrameCraft.computeResult(): выдаю tagged frame. key=" + key + ", frameType=" + frameType);
        return InvisibleFrameItems.createTaggedFrame(keys, frameType);
    }
}
