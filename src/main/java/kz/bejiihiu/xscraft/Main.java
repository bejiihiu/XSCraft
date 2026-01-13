package kz.bejiihiu.xscraft;

import kz.bejiihiu.xscraft.core.PluginContext;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private PluginContext context;

    @Override
    public void onLoad() {
        // onLoad тоже полезно логировать: видно порядок загрузки
        Debug.bootstrap(this);
        Debug.info("onLoad(): плагин загружается (ещё до onEnable).");
    }

    @Override
    public void onEnable() {
        Debug.bootstrap(this);
        Debug.info("onEnable(): старт инициализации плагина.");

        try {
            this.context = new PluginContext(this);
            this.context.enable();
            Debug.info("onEnable(): плагин успешно включён.");
        } catch (Throwable t) {
            Debug.error("onEnable(): критическая ошибка при включении плагина.", t);
            // Если упало на enable — лучше выключить плагин полностью
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        Debug.info("onDisable(): начало остановки плагина.");

        try {
            if (this.context != null) {
                this.context.disable();
                this.context = null;
            }
        } catch (Throwable t) {
            Debug.error("onDisable(): ошибка при остановке.", t);
        }

        Debug.info("onDisable(): плагин остановлен.");
    }
}
