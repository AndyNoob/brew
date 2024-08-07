package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

public abstract class InventorySwitchingComponent<Inv extends Inventory> extends StaticComponent {

    protected final Map<HumanEntity, Inventory> openInv = new HashMap<>();
    protected final Map<HumanEntity, Inventory> dontReopen = new HashMap<>();
    protected final Map<HumanEntity, Inventory> reopens = new HashMap<>();
    protected final JavaPlugin plugin;

    public InventorySwitchingComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position) {
        super(position);
        this.plugin = plugin;
    }

    protected abstract Inv open(HumanEntity entity);

    protected void reopenOriginal(HumanEntity entity) {
        Inventory inventory = reopens.remove(entity);
        if (inventory != null) {
            entity.openInventory(inventory);
            Bukkit.getScheduler().runTaskLater(plugin, () -> entity.openInventory(inventory), 0);
        }
    }

}
