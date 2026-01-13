package kz.bejiihiu.xscraft.features.invisibleframes;

import kz.bejiihiu.xscraft.core.Keys;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public final class InvisibleFrameItems {

    private InvisibleFrameItems() {}

    public static ItemStack createTaggedFrame(Keys keys, Material type) {
        Objects.requireNonNull(keys, "keys");
        Objects.requireNonNull(type, "type");

        Debug.info("InvisibleFrameItems.createTaggedFrame(): создаю tagged frame. type=" + type);

        if (type != Material.ITEM_FRAME && type != Material.GLOW_ITEM_FRAME) {
            Debug.warn("InvisibleFrameItems.createTaggedFrame(): неподдерживаемый type=" + type + " — выбрасываю исключение.");
            throw new IllegalArgumentException("Unsupported frame type: " + type);
        }

        ItemStack out = new ItemStack(type, 1);
        ItemMeta meta = out.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(getDisplayName(type));
            meta.getPersistentDataContainer().set(keys.tagInvisibleFrame(), PersistentDataType.BYTE, (byte) 1);
            out.setItemMeta(meta);
            Debug.info("InvisibleFrameItems.createTaggedFrame(): PDC tag установлен: " + keys.tagInvisibleFrame());
        } else {
            Debug.warn("InvisibleFrameItems.createTaggedFrame(): meta=null — тег НЕ установлен (это странно).");
        }

        return out;
    }

    public static boolean isTaggedFrameItem(Keys keys, ItemStack stack) {
        if (stack == null) return false;

        Material t = stack.getType();
        if (t != Material.ITEM_FRAME && t != Material.GLOW_ITEM_FRAME) return false;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        Byte flag = meta.getPersistentDataContainer().get(keys.tagInvisibleFrame(), PersistentDataType.BYTE);
        boolean tagged = (flag != null && flag == (byte) 1);

        // Очень горячая функция: спамим с rate-limit
        Debug.spamInfo("InvisibleFrameItems.isTagged", 500,
                "InvisibleFrameItems.isTaggedFrameItem(): type=" + t + ", tagged=" + tagged);

        return tagged;
    }

    private static String getDisplayName(Material type) {
        if (type == Material.GLOW_ITEM_FRAME) {
            return "Невидимая светящаяся рамка";
        }
        return "Невидимая рамка";
    }
}
