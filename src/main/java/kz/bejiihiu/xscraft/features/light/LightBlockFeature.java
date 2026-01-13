package kz.bejiihiu.xscraft.features.light;

import kz.bejiihiu.xscraft.auto.LoadOrder;
import kz.bejiihiu.xscraft.core.PluginContext;
import kz.bejiihiu.xscraft.features.PluginFeature;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

@LoadOrder(20)
public final class LightBlockFeature implements PluginFeature {

    public LightBlockFeature() {
        Debug.info("LightBlockFeature: создан объект фичи (конструктор).");
    }

    @Override
    public void enable(PluginContext ctx) {
        Debug.info("LightBlockFeature.enable(): старт.");

        Debug.info("LightBlockFeature: регистрирую рецепт GLASS + GLOWSTONE_DUST -> 4x LIGHT");
        ShapelessRecipe light = new ShapelessRecipe(
                ctx.keys().recipeLightCraft(),
                new ItemStack(Material.LIGHT, 4)
        );
        light.addIngredient(Material.GLASS);
        light.addIngredient(Material.GLOWSTONE_DUST);
        ctx.crafts().registerRecipe(light);

        Debug.info("LightBlockFeature: регистрирую listener на ломание блока LIGHT.");
        ctx.registerEvents(new LightBlockListener());

        Debug.info("LightBlockFeature.enable(): завершено.");
    }

    @Override
    public void disable(PluginContext ctx) {
        Debug.info("LightBlockFeature.disable(): выключение (специальных ресурсов нет).");
    }
}
