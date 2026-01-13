package kz.bejiihiu.xscraft.features.light;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LightBlockViewListener implements Listener {

    private static final int VIEW_RADIUS = 7;
    private static final int VIEW_RADIUS_SQUARED = VIEW_RADIUS * VIEW_RADIUS;

    private final ProtocolManager protocolManager;
    private final Plugin plugin;
    private final Map<UUID, Set<BlockVector>> cachedBlocks = new HashMap<>();

    public LightBlockViewListener() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.plugin = JavaPlugin.getProvidingPlugin(LightBlockViewListener.class);
        Debug.info("LightBlockViewListener создан.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scheduleRefresh(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        clearFakeBlocks(event.getPlayer());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        scheduleRefresh(event.getPlayer());
    }

    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        scheduleRefresh(event.getPlayer());
    }

    private void scheduleRefresh(Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> refreshForPlayer(player));
    }

    private void refreshForPlayer(Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (!isHoldingLight(player)) {
            clearFakeBlocks(player);
            return;
        }
        Set<BlockVector> nearbyLightBlocks = findNearbyLightBlocks(player);
        Set<BlockVector> cached = cachedBlocks.computeIfAbsent(player.getUniqueId(), key -> new HashSet<>());

        if (nearbyLightBlocks.equals(cached)) {
            return;
        }

        Set<BlockVector> toAdd = new HashSet<>(nearbyLightBlocks);
        toAdd.removeAll(cached);

        Set<BlockVector> toRemove = new HashSet<>(cached);
        toRemove.removeAll(nearbyLightBlocks);

        if (!toAdd.isEmpty()) {
            sendBlockChanges(player, toAdd, WrappedBlockData.createData(Material.GLOWSTONE));
        }
        if (!toRemove.isEmpty()) {
            sendRealBlockChanges(player, toRemove);
        }

        cached.clear();
        cached.addAll(nearbyLightBlocks);
    }

    private boolean isHoldingLight(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        return (mainHand != null && mainHand.getType() == Material.LIGHT)
                || (offHand != null && offHand.getType() == Material.LIGHT);
    }

    private Set<BlockVector> findNearbyLightBlocks(Player player) {
        Set<BlockVector> result = new HashSet<>();
        World world = player.getWorld();
        int baseX = player.getLocation().getBlockX();
        int baseY = player.getLocation().getBlockY();
        int baseZ = player.getLocation().getBlockZ();

        for (int dx = -VIEW_RADIUS; dx <= VIEW_RADIUS; dx++) {
            int x = baseX + dx;
            for (int dy = -VIEW_RADIUS; dy <= VIEW_RADIUS; dy++) {
                int y = baseY + dy;
                for (int dz = -VIEW_RADIUS; dz <= VIEW_RADIUS; dz++) {
                    if (dx * dx + dy * dy + dz * dz > VIEW_RADIUS_SQUARED) {
                        continue;
                    }
                    int z = baseZ + dz;
                    Block block = world.getBlockAt(x, y, z);
                    if (block.getType() == Material.LIGHT) {
                        result.add(new BlockVector(x, y, z));
                    }
                }
            }
        }

        return result;
    }

    private void clearFakeBlocks(Player player) {
        Set<BlockVector> cached = cachedBlocks.remove(player.getUniqueId());
        if (cached == null || cached.isEmpty()) {
            return;
        }
        sendRealBlockChanges(player, cached);
    }

    private void sendRealBlockChanges(Player player, Set<BlockVector> positions) {
        for (BlockVector position : positions) {
            Block block = player.getWorld().getBlockAt(position.getBlockX(), position.getBlockY(), position.getBlockZ());
            sendBlockChange(player, position, WrappedBlockData.createData(block.getBlockData()));
        }
    }

    private void sendBlockChanges(Player player, Set<BlockVector> positions, WrappedBlockData data) {
        for (BlockVector position : positions) {
            sendBlockChange(player, position, data);
        }
    }

    private void sendBlockChange(Player player, BlockVector position, WrappedBlockData data) {
        PacketContainer packet = protocolManager.createPacket(com.comphenix.protocol.PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new BlockPosition(position.getBlockX(), position.getBlockY(), position.getBlockZ()));
        packet.getBlockData().write(0, data);
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            Debug.error("LightBlockViewListener: ошибка отправки BLOCK_CHANGE пакета.", e);
        }
    }
}
