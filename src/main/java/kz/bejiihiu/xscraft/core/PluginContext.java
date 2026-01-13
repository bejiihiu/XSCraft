// File: src/main/java/kz/bejiihiu/xscraft/core/PluginContext.java
package kz.bejiihiu.xscraft.core;

import kz.bejiihiu.xscraft.auto.AutoDiscovery;
import kz.bejiihiu.xscraft.craft.CraftRegistry;
import kz.bejiihiu.xscraft.craft.CraftingValidationListener;
import kz.bejiihiu.xscraft.craft.RecipeBookListener;
import kz.bejiihiu.xscraft.features.PluginFeature;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class PluginContext {

    private static final String FEATURES_BASE_PACKAGE = "kz.bejiihiu.xscraft.features";

    private final JavaPlugin plugin;
    private final Keys keys;
    private final CraftRegistry crafts;

    private final List<PluginFeature> loadedFeatures = new ArrayList<>();

    public PluginContext(JavaPlugin plugin) {
        this.plugin = plugin;
        this.keys = new Keys(plugin);
        this.crafts = new CraftRegistry(plugin);

        Debug.info("PluginContext создан. keys/crafts инициализированы.");
    }

    public void enable() {
        Debug.info("PluginContext.enable(): старт.");

        // Общий валидатор крафтов (один на весь плагин)
        Listener craftValidationListener = new CraftingValidationListener(this.crafts);
        Bukkit.getPluginManager().registerEvents(craftValidationListener, this.plugin);
        Debug.info("PluginContext.enable(): зарегистрирован CraftingValidationListener (общий валидатор крафтов).");

        // Автоподхват фич по пакету (без конфига)
        Debug.info("PluginContext.enable(): запускаю авто-поиск PluginFeature в пакете: " + FEATURES_BASE_PACKAGE);
        List<PluginFeature> features = AutoDiscovery.instantiateAll(
                this.plugin,
                FEATURES_BASE_PACKAGE,
                PluginFeature.class
        );

        Debug.info("PluginContext.enable(): найдено фич: " + features.size());

        if (features.isEmpty()) {
            Debug.warn("PluginContext.enable(): ВНИМАНИЕ: не найдено ни одной PluginFeature. Рецепты/листенеры не будут добавлены.");
        }

        for (PluginFeature feature : features) {
            String name = feature.getClass().getName();
            try {
                Debug.info("PluginContext.enable(): включаю фичу: " + name);
                feature.enable(this);
                loadedFeatures.add(feature);
                Debug.info("PluginContext.enable(): фича успешно включена: " + name);
            } catch (Throwable t) {
                Debug.error("PluginContext.enable(): ошибка при включении фичи: " + name, t);
            }
        }

        Listener recipeBookListener = new RecipeBookListener(this.crafts);
        Bukkit.getPluginManager().registerEvents(recipeBookListener, this.plugin);
        Debug.info("PluginContext.enable(): зарегистрирован RecipeBookListener для открытия рецептов.");

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (NamespacedKey key : crafts.getOwnedRecipes()) {
                player.discoverRecipe(key);
            }
        }
        Debug.info("PluginContext.enable(): рецепты раскрыты для текущих онлайн игроков.");

        Debug.info("PluginContext.enable(): завершено. Активных фич: " + loadedFeatures.size());
    }

    public void disable() {
        Debug.info("PluginContext.disable(): старт. Активных фич: " + loadedFeatures.size());

        // Сначала корректно останавливаем фичи (обратный порядок)
        for (int i = loadedFeatures.size() - 1; i >= 0; i--) {
            PluginFeature f = loadedFeatures.get(i);
            String name = f.getClass().getName();

            try {
                Debug.info("PluginContext.disable(): выключаю фичу: " + name);
                f.disable(this);
                Debug.info("PluginContext.disable(): фича выключена: " + name);
            } catch (Throwable t) {
                Debug.error("PluginContext.disable(): ошибка при выключении фичи: " + name, t);
            }
        }
        loadedFeatures.clear();

        Debug.info("PluginContext.disable(): снимаю все зарегистрированные рецепты этого плагина.");
        crafts.unregisterAll();

        Debug.info("PluginContext.disable(): завершено.");
    }

    public Keys keys() {
        return keys;
    }

    public CraftRegistry crafts() {
        return crafts;
    }

    public void registerEvents(Listener listener) {
        if (listener == null) {
            Debug.warn("PluginContext.registerEvents(): попытка зарегистрировать null listener — пропуск.");
            return;
        }
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        Debug.info("PluginContext.registerEvents(): зарегистрирован listener: " + listener.getClass().getName());
    }
}
