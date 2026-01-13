package kz.bejiihiu.xscraft.craft;

import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CraftRegistry {

    private final Map<NamespacedKey, ValidatedCraft> validatedCrafts = new ConcurrentHashMap<>();
    private final Set<NamespacedKey> ownedRecipes = ConcurrentHashMap.newKeySet();

    public CraftRegistry(JavaPlugin plugin) {
        Debug.info("CraftRegistry создан.");
    }

    public void registerRecipe(Recipe recipe) {
        if (recipe == null) {
            Debug.warn("CraftRegistry.registerRecipe(): recipe=null — пропуск.");
            return;
        }

        NamespacedKey key = (recipe instanceof Keyed keyed) ? keyed.getKey() : null;
        Debug.info("CraftRegistry.registerRecipe(): попытка регистрации рецепта. key=" + (key != null ? key : "NULL"));

        if (key != null) {
            Debug.info("CraftRegistry.registerRecipe(): удаляю старый рецепт с этим ключом (если был): " + key);
            Bukkit.removeRecipe(key);
        } else {
            Debug.warn("CraftRegistry.registerRecipe(): рецепт не Keyed — потом удалить на disable не смогу.");
        }

        boolean ok = Bukkit.addRecipe(recipe);
        if (!ok) {
            Debug.warn("CraftRegistry.registerRecipe(): Bukkit отклонил рецепт. key=" + (key != null ? key : "NULL"));
            return;
        }

        Debug.info("CraftRegistry.registerRecipe(): рецепт зарегистрирован успешно. key=" + (key != null ? key : "NULL"));

        if (key != null) {
            ownedRecipes.add(key);
            Debug.info("CraftRegistry.registerRecipe(): key добавлен в ownedRecipes: " + key);
        }
    }

    public void registerValidated(ValidatedCraft craft) {
        if (craft == null) {
            Debug.warn("CraftRegistry.registerValidated(): craft=null — пропуск.");
            return;
        }

        NamespacedKey key = craft.key();
        Debug.info("CraftRegistry.registerValidated(): регистрирую валидируемый крафт: " + key);

        validatedCrafts.put(key, craft);
        Debug.info("CraftRegistry.registerValidated(): craft добавлен в validatedCrafts. Теперь их: " + validatedCrafts.size());

        Recipe recipe = craft.createRecipe();
        Debug.info("CraftRegistry.registerValidated(): createRecipe() выполнен для " + key + ". Регистрирую recipe в Bukkit.");
        registerRecipe(recipe);
    }

    public ValidatedCraft getValidated(NamespacedKey key) {
        if (key == null) return null;
        return validatedCrafts.get(key);
    }

    public void unregisterAll() {
        Debug.info("CraftRegistry.unregisterAll(): старт. ownedRecipes=" + ownedRecipes.size() + ", validatedCrafts=" + validatedCrafts.size());

        for (NamespacedKey key : ownedRecipes) {
            Debug.info("CraftRegistry.unregisterAll(): удаляю рецепт: " + key);
            Bukkit.removeRecipe(key);
        }

        ownedRecipes.clear();
        validatedCrafts.clear();

        Debug.info("CraftRegistry.unregisterAll(): завершено. Теперь ownedRecipes=" + ownedRecipes.size() + ", validatedCrafts=" + 0);
    }
}
