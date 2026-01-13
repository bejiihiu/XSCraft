package kz.bejiihiu.xscraft.core;

import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {

    private final Plugin plugin;

    // Tags
    private final NamespacedKey tagInvisibleFrame;

    // Recipes
    private final NamespacedKey rInvisibleItemFrame;
    private final NamespacedKey rInvisibleGlowItemFrame;
    private final NamespacedKey rLightCraft;
    private final NamespacedKey rLightRecycle;

    public Keys(Plugin plugin) {
        this.plugin = plugin;

        this.tagInvisibleFrame = new NamespacedKey(plugin, "invisible_frame");

        this.rInvisibleItemFrame = new NamespacedKey(plugin, "craft_invisible_item_frame");
        this.rInvisibleGlowItemFrame = new NamespacedKey(plugin, "craft_invisible_glow_item_frame");
        this.rLightCraft = new NamespacedKey(plugin, "craft_light_block");
        this.rLightRecycle = new NamespacedKey(plugin, "recycle_light_block");

        Debug.info("Keys: создано NamespacedKey. tagInvisibleFrame=" + tagInvisibleFrame
                + ", rInvisibleItemFrame=" + rInvisibleItemFrame
                + ", rInvisibleGlowItemFrame=" + rInvisibleGlowItemFrame
                + ", rLightCraft=" + rLightCraft
                + ", rLightRecycle=" + rLightRecycle);
    }

    public Plugin plugin() {
        return plugin;
    }

    public NamespacedKey tagInvisibleFrame() {
        return tagInvisibleFrame;
    }

    public NamespacedKey recipeInvisibleItemFrame() {
        return rInvisibleItemFrame;
    }

    public NamespacedKey recipeInvisibleGlowItemFrame() {
        return rInvisibleGlowItemFrame;
    }

    public NamespacedKey recipeLightCraft() {
        return rLightCraft;
    }

    public NamespacedKey recipeLightRecycle() {
        return rLightRecycle;
    }
}
