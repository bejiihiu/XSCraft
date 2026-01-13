package kz.bejiihiu.xscraft.auto;

import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class AutoDiscovery {

    private AutoDiscovery() {}

    public static <T> List<T> instantiateAll(JavaPlugin plugin, String basePackage, Class<T> type) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(basePackage, "basePackage");
        Objects.requireNonNull(type, "type");

        Debug.info("AutoDiscovery.instantiateAll(): старт. basePackage=" + basePackage + ", type=" + type.getName());

        List<Class<? extends T>> classes = findImplementations(plugin, basePackage, type);

        Debug.info("AutoDiscovery.instantiateAll(): кандидатов на инстанцирование: " + classes.size());

        classes.sort((a, b) -> {
            int oa = orderOf(a);
            int ob = orderOf(b);
            if (oa != ob) return Integer.compare(oa, ob);
            return a.getName().compareTo(b.getName());
        });

        List<T> out = new ArrayList<>();
        for (Class<? extends T> clazz : classes) {
            String n = clazz.getName();
            try {
                Debug.info("AutoDiscovery.instantiateAll(): создаю экземпляр: " + n);
                T instance = clazz.getDeclaredConstructor().newInstance();
                out.add(instance);
                Debug.info("AutoDiscovery.instantiateAll(): успешно создан: " + n);
            } catch (NoSuchMethodException nsme) {
                Debug.error("AutoDiscovery.instantiateAll(): нет public no-args конструктора у " + n
                        + ". Этот класс НЕ будет автоподхвачен.", nsme);
            } catch (Throwable t) {
                Debug.error("AutoDiscovery.instantiateAll(): ошибка при создании " + n, t);
            }
        }

        Debug.info("AutoDiscovery.instantiateAll(): завершено. Успешно создано: " + out.size());
        return out;
    }

    private static int orderOf(Class<?> c) {
        LoadOrder ann = c.getAnnotation(LoadOrder.class);
        return ann != null ? ann.value() : 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> findImplementations(JavaPlugin plugin, String basePackage, Class<T> type) {
        Debug.info("AutoDiscovery.findImplementations(): сканирую jar. basePackage=" + basePackage);

        File jarFile = resolvePluginJar(plugin);
        if (jarFile == null) {
            Debug.error("AutoDiscovery.findImplementations(): не удалось определить путь до jar (CodeSource null).", null);
            return List.of();
        }

        Debug.info("AutoDiscovery.findImplementations(): jar=" + jarFile.getAbsolutePath());

        String basePath = basePackage.replace('.', '/') + "/";

        List<Class<? extends T>> found = new ArrayList<>();
        ClassLoader cl = plugin.getClass().getClassLoader();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            int total = 0;
            int matchedPath = 0;

            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                total++;

                String name = e.getName();
                if (e.isDirectory()) continue;
                if (!name.endsWith(".class")) continue;
                if (!name.startsWith(basePath)) continue;

                matchedPath++;

                // Скипаем inner/анонимные/синтетические классы
                if (name.contains("$")) {
                    Debug.spamInfo("AutoDiscovery.skipInner", 2000,
                            "AutoDiscovery: пропускаю inner/anon class: " + name);
                    continue;
                }

                String className = name.substring(0, name.length() - 6).replace('/', '.');

                Class<?> raw;
                try {
                    raw = Class.forName(className, false, cl);
                } catch (Throwable t) {
                    Debug.warn("AutoDiscovery: не смог загрузить класс " + className + " (" + t.getClass().getSimpleName() + ")");
                    continue;
                }

                if (!type.isAssignableFrom(raw)) {
                    Debug.spamInfo("AutoDiscovery.notAssignable", 2000,
                            "AutoDiscovery: класс " + className + " не реализует " + type.getSimpleName() + " — пропуск.");
                    continue;
                }

                if (raw.isInterface()) {
                    Debug.warn("AutoDiscovery: " + className + " это интерфейс — пропуск.");
                    continue;
                }

                int mod = raw.getModifiers();
                if (Modifier.isAbstract(mod)) {
                    Debug.warn("AutoDiscovery: " + className + " abstract — пропуск.");
                    continue;
                }

                AutoLoad auto = raw.getAnnotation(AutoLoad.class);
                if (auto != null && !auto.value()) {
                    Debug.info("AutoDiscovery: " + className + " помечен @AutoLoad(false) — пропуск.");
                    continue;
                }

                Debug.info("AutoDiscovery: найден подходящий класс: " + className);
                found.add((Class<? extends T>) raw);
            }

            Debug.info("AutoDiscovery.findImplementations(): статистика: totalEntries=" + total
                    + ", matchedPath=" + matchedPath
                    + ", foundImplementations=" + found.size());

        } catch (Throwable t) {
            Debug.error("AutoDiscovery.findImplementations(): ошибка сканирования jar: " + t.getMessage(), t);
        }

        return found;
    }

    private static File resolvePluginJar(JavaPlugin plugin) {
        try {
            URL url = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
            if (url == null) return null;
            URI uri = url.toURI();
            return new File(uri);
        } catch (Throwable t) {
            Debug.error("AutoDiscovery.resolvePluginJar(): не удалось определить jar: " + t.getMessage(), t);
            return null;
        }
    }
}
