package kz.bejiihiu.xscraft.craft;

import kz.bejiihiu.xscraft.util.Debug;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public record RecipeBookListener(CraftRegistry registry) implements Listener {

    public RecipeBookListener(CraftRegistry registry) {
        this.registry = registry;
        Debug.info("RecipeBookListener создан.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (NamespacedKey key : registry.getOwnedRecipes()) {
            player.discoverRecipe(key);
        }
    }
}
