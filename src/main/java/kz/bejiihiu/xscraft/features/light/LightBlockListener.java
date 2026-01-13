// File: src/main/java/kz/bejiihiu/xscraft/features/light/LightBlockListener.java
package kz.bejiihiu.xscraft.features.light;

import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class LightBlockListener implements Listener {

    public LightBlockListener() {
        Debug.info("LightBlockListener создан.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onLightBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() != Material.LIGHT) return;

        // Частое событие на серверах с “световыми” декорациями — rate-limit
        Debug.spamInfo("LightBlock.break", 250,
                "BlockBreakEvent: игрок ломает LIGHT. dropItems=false (ванильно).");

        event.setDropItems(false);
        event.setExpToDrop(0);
    }
}
