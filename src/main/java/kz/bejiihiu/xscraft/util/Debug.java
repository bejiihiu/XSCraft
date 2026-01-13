package kz.bejiihiu.xscraft.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Жёсткий дебаг без конфига.
 * Если хочешь выключить глобально — поставь ENABLED=false.
 */
public final class Debug {

    private Debug() {}

    private static volatile JavaPlugin plugin;

    // Меняй на false, если нужно глушить дебаг полностью (без конфига, просто сборкой).
    private static final boolean ENABLED = true;

    // Rate-limit для очень частых мест (craft/move/etc), чтобы не убить консоль.
    private static final ConcurrentMap<String, Long> LAST_LOG = new ConcurrentHashMap<>();

    public static void bootstrap(JavaPlugin pl) {
        if (pl == null) return;
        plugin = pl;
    }

    public static void info(String msg) {
        if (!ENABLED) return;
        JavaPlugin pl = plugin;
        if (pl == null) return;
        pl.getLogger().info(prefix(msg));
    }

    public static void warn(String msg) {
        if (!ENABLED) return;
        JavaPlugin pl = plugin;
        if (pl == null) return;
        pl.getLogger().warning(prefix(msg));
    }

    public static void error(String msg, Throwable t) {
        if (!ENABLED) return;
        JavaPlugin pl = plugin;
        if (pl == null) return;
        pl.getLogger().severe(prefix(msg));
        if (t != null) {
            pl.getLogger().severe(t.getMessage());
        }
    }

    public static void spamInfo(String key, long intervalMs, String msg) {
        if (!ENABLED) return;
        if (shouldLog(key, intervalMs)) return;
        info(msg);
    }

    public static void spamWarn(String key, long intervalMs, String msg) {
        if (!ENABLED) return;
        if (shouldLog(key, intervalMs)) return;
        warn(msg);
    }

    private static boolean shouldLog(String key, long intervalMs) {
        Objects.requireNonNull(key, "key");
        long now = System.currentTimeMillis();
        Long prev = LAST_LOG.put(key, now);
        if (prev == null) return false;
        return (now - prev) < intervalMs;
    }

    private static String prefix(String msg) {
        String thread = Thread.currentThread().getName();
        return "[XSCraft] [" + thread + "] " + msg;
    }
}
