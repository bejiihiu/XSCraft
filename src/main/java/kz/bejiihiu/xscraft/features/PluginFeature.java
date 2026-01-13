package kz.bejiihiu.xscraft.features;

import kz.bejiihiu.xscraft.core.PluginContext;

/**
 * Требование для автоподхвата:
 * - public класс
 * - public no-args конструктор
 * - расположен в пакете kz.bejiihiu.xscraft.features.*
 */
public interface PluginFeature {
    void enable(PluginContext ctx);
    default void disable(PluginContext ctx) {}
}
