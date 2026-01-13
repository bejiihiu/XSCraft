package kz.bejiihiu.xscraft.craft;

import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Recipe;

public record CraftingValidationListener(CraftRegistry registry) implements Listener {

    public CraftingValidationListener(CraftRegistry registry) {
        this.registry = registry;
        Debug.info("CraftingValidationListener создан.");
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed keyed)) return;

        NamespacedKey key = keyed.getKey();
        ValidatedCraft craft = registry.getValidated(key);
        if (craft == null) return;

        // Частое событие: логируем rate-limited
        Debug.spamInfo("CraftPrepare:" + key, 250,
                "PrepareItemCraftEvent: обнаружен валидируемый рецепт: " + key);

        CraftingInventory inv = event.getInventory();
        boolean valid;

        try {
            valid = craft.isValid(inv);
        } catch (Throwable t) {
            Debug.error("PrepareItemCraftEvent: ошибка isValid() у рецепта " + key + " — сбрасываю результат.", t);
            inv.setResult(null);
            return;
        }

        if (!valid) {
            Debug.spamInfo("CraftPrepareInvalid:" + key, 250,
                    "PrepareItemCraftEvent: рецепт " + key + " НЕ валиден — результат null.");
            inv.setResult(null);
            return;
        }

        try {
            inv.setResult(craft.computeResult(inv));
            Debug.spamInfo("CraftPrepareValid:" + key, 250,
                    "PrepareItemCraftEvent: рецепт " + key + " валиден — подставлен результат.");
        } catch (Throwable t) {
            Debug.error("PrepareItemCraftEvent: ошибка computeResult() у рецепта " + key + " — сбрасываю результат.", t);
            inv.setResult(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof Keyed keyed)) return;

        NamespacedKey key = keyed.getKey();
        ValidatedCraft craft = registry.getValidated(key);
        if (craft == null) return;

        CraftingInventory inv = event.getInventory();

        // Реже, чем prepare, но всё равно может быть часто при автокрафте/спаме
        Debug.spamInfo("CraftClick:" + key, 500,
                "CraftItemEvent: попытка закрафтить по валидируемому рецепту: " + key);

        boolean valid;
        try {
            valid = craft.isValid(inv);
        } catch (Throwable t) {
            Debug.error("CraftItemEvent: ошибка isValid() у " + key + " — отменяю крафт.", t);
            event.setCancelled(true);
            return;
        }

        if (!valid) {
            Debug.info("CraftItemEvent: рецепт " + key + " НЕ валиден — отменяю крафт.");
            event.setCancelled(true);
            return;
        }

        Debug.spamInfo("CraftOk:" + key, 500,
                "CraftItemEvent: рецепт " + key + " валиден — крафт разрешён.");
    }
}
