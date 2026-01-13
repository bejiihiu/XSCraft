package kz.bejiihiu.xscraft.features.invisibleframes;

import kz.bejiihiu.xscraft.auto.LoadOrder;
import kz.bejiihiu.xscraft.core.PluginContext;
import kz.bejiihiu.xscraft.features.PluginFeature;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Material;

@LoadOrder(10)
public final class InvisibleFramesFeature implements PluginFeature {

    public InvisibleFramesFeature() {
        Debug.info("InvisibleFramesFeature: создан объект фичи (конструктор).");
    }

    @Override
    public void enable(PluginContext ctx) {
        Debug.info("InvisibleFramesFeature.enable(): старт.");

        Debug.info("InvisibleFramesFeature: регистрирую валидируемый рецепт для ITEM_FRAME.");
        ctx.crafts().registerValidated(new InvisibleFrameCraft(
                ctx.keys().recipeInvisibleItemFrame(),
                ctx.keys(),
                Material.ITEM_FRAME
        ));

        Debug.info("InvisibleFramesFeature: регистрирую валидируемый рецепт для GLOW_ITEM_FRAME.");
        ctx.crafts().registerValidated(new InvisibleFrameCraft(
                ctx.keys().recipeInvisibleGlowItemFrame(),
                ctx.keys(),
                Material.GLOW_ITEM_FRAME
        ));

        Debug.info("InvisibleFramesFeature: регистрирую listener поведения рамок.");
        ctx.registerEvents(new InvisibleFramesListener(ctx.keys()));

        Debug.info("InvisibleFramesFeature.enable(): завершено.");
    }

    @Override
    public void disable(PluginContext ctx) {
        Debug.info("InvisibleFramesFeature.disable(): выключение (специальных ресурсов нет).");
    }
}
