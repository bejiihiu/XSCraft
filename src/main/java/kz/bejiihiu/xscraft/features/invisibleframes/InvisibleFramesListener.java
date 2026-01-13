package kz.bejiihiu.xscraft.features.invisibleframes;

import kz.bejiihiu.xscraft.core.Keys;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public record InvisibleFramesListener(Keys keys) implements Listener {

    public InvisibleFramesListener(Keys keys) {
        this.keys = keys;
        Debug.info("InvisibleFramesListener создан.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;

        Player player = event.getPlayer();
        if (player == null) {
            Debug.spamWarn("InvisibleFrames.place.noPlayer", 2000,
                    "HangingPlaceEvent: player=null (плейс рамки без игрока?) — пропуск.");
            return;
        }

        // Логируем место установки, но без координатных “миллионов” — rate-limit
        Debug.spamInfo("InvisibleFrames.place", 250,
                "HangingPlaceEvent: игрок " + player.getName() + " ставит рамку. entity=" + frame.getType());

        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        boolean taggedMain = InvisibleFrameItems.isTaggedFrameItem(keys, main);
        boolean taggedOff = InvisibleFrameItems.isTaggedFrameItem(keys, off);

        if (!taggedMain && !taggedOff) {
            Debug.spamInfo("InvisibleFrames.place.notTagged", 250,
                    "HangingPlaceEvent: рамка не tagged в руках — не делаю её невидимой.");
            return;
        }

        Debug.info("HangingPlaceEvent: обнаружена tagged рамка в руке (" + (taggedMain ? "MAIN" : "OFF") + "). Делаю entity невидимой.");

        frame.setVisible(false);
        frame.getPersistentDataContainer().set(keys.tagInvisibleFrame(), PersistentDataType.BYTE, (byte) 1);

        Debug.info("HangingPlaceEvent: entity рамки помечена PDC и сделана невидимой. key=" + keys.tagInvisibleFrame());
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        handleTaggedFrameBreak(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        handleTaggedFrameBreak(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDropItem(EntityDropItemEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;

        PersistentDataContainer pdc = frame.getPersistentDataContainer();
        Byte flag = pdc.get(keys.tagInvisibleFrame(), PersistentDataType.BYTE);

        if (flag == null || flag != (byte) 1) {
            Debug.spamInfo("InvisibleFrames.drop.notTaggedEntity", 500,
                    "EntityDropItemEvent: рамка не помечена как invisible_frame — не подменяю дроп.");
            return;
        }

        Debug.info("EntityDropItemEvent: рамка помечена invisible_frame — подменяю дроп на tagged предмет.");

        ItemStack drop = event.getItemDrop().getItemStack();

        Material t = drop.getType();
        if (t == Material.ITEM_FRAME || t == Material.GLOW_ITEM_FRAME) {
            event.getItemDrop().setItemStack(InvisibleFrameItems.createTaggedFrame(keys, t));
            Debug.info("EntityDropItemEvent: дроп подменён на tagged " + t);
        } else {
            Debug.warn("EntityDropItemEvent: неожиданный type дропа от рамки: " + t + " — не трогаю.");
        }
    }

    private void handleTaggedFrameBreak(HangingBreakEvent event) {
        if (!(event.getEntity() instanceof ItemFrame frame)) return;

        PersistentDataContainer pdc = frame.getPersistentDataContainer();
        Byte flag = pdc.get(keys.tagInvisibleFrame(), PersistentDataType.BYTE);

        if (flag == null || flag != (byte) 1) {
            Debug.spamInfo("InvisibleFrames.break.notTaggedEntity", 500,
                    "HangingBreakEvent: рамка не помечена как invisible_frame — не подменяю дроп.");
            return;
        }

        Debug.info("HangingBreakEvent: рамка помечена invisible_frame — отменяю стандартный дроп и выбрасываю tagged предмет.");

        event.setCancelled(true);
        frame.remove();

        Material dropType = frame.getType() == EntityType.GLOW_ITEM_FRAME
                ? Material.GLOW_ITEM_FRAME
                : Material.ITEM_FRAME;

        frame.getWorld().dropItemNaturally(
                frame.getLocation(),
                InvisibleFrameItems.createTaggedFrame(keys, dropType)
        );

        Debug.info("HangingBreakEvent: дроп подменён на tagged " + dropType);
    }
}
