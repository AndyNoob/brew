package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

public abstract class InventorySwitchingComponent<Inv extends Inventory> extends StaticComponent {

    private static final NotListener LISTENER = new NotListener();
    protected final Map<HumanEntity, Inventory> openInv = new HashMap<>();
    protected final Map<HumanEntity, Inventory> dontReopen = new HashMap<>();
    protected final Map<HumanEntity, Inventory> reopens = new HashMap<>();
    protected final JavaPlugin plugin;
    protected final RegisteredListener onClose;
    protected final RegisteredListener onClick;
    protected final RegisteredListener onDrag;
    protected boolean isRemoved = false;

    public InventorySwitchingComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position) {
        super(position);
        this.plugin = plugin;
        this.onClose = makeListener(plugin, (l, e) -> handleClose((InventoryCloseEvent) e));
        this.onClick = makeListener(plugin, (l, e) -> handleClick((InventoryClickEvent) e));
        this.onDrag = makeListener(plugin, (l, e) -> ((InventoryDragEvent) e).setCancelled(true));
        InventoryClickEvent.getHandlerList().register(this.onClick);
        InventoryCloseEvent.getHandlerList().register(this.onClose);
        InventoryDragEvent.getHandlerList().register(this.onDrag);
    }

    protected void handleClose(InventoryCloseEvent e) {
        Inventory inventory = e.getView().getTopInventory();
        System.out.println("closing " + inventory);
        System.out.println("opened " + this.openInv.get(e.getPlayer()));
        if (inventory != this.openInv.get(e.getPlayer())) return;
        Player player = (Player) e.getPlayer();
        player.giveExpLevels(0);
        if (this.dontReopen.remove(e.getPlayer()) == inventory) return;
        System.out.println("reopening original from handling close");
        reopenOriginal(e.getPlayer());
    }

    protected abstract void handleClick(InventoryClickEvent e);

    protected abstract Inv getInventoryFor(HumanEntity entity);

    public void open(HumanEntity entity) {
        Inv inv = getInventoryFor(entity);
        storeReopenInfo(entity, entity.getOpenInventory().getTopInventory(), inv);
        entity.openInventory(inv);
    }

    protected void storeReopenInfo(HumanEntity entity, Inventory old, Inv opening) {
        this.reopens.put(entity, old);
        this.openInv.put(entity, opening);
    }

    public void close(HumanEntity entity) {
        this.dontReopen.put(entity, this.openInv.get(entity));
        reopenOriginal(entity);
    }

    protected void reopenOriginal(HumanEntity entity) {
        Inventory inventory = reopens.remove(entity);
        if (inventory != null) {
            entity.openInventory(inventory);
            Bukkit.getScheduler().runTaskLater(plugin, () -> entity.openInventory(inventory), 0);
        }
    }

    @NotNull
    protected RegisteredListener makeListener(@NotNull JavaPlugin plugin, EventExecutor executor) {
        return new RegisteredListener(
                LISTENER,
                executor,
                EventPriority.NORMAL,
                plugin,
                false
        );
    }

    @Override
    public void postRemoval() {
        isRemoved = true;
        InventoryCloseEvent.getHandlerList().unregister(this.onClose);
        InventoryClickEvent.getHandlerList().unregister(this.onClick);
        InventoryDragEvent.getHandlerList().unregister(this.onDrag);
    }

    @Override
    public void setRenderer(@NotNull Renderer renderer) {
        if (this.isRemoved)
            throw new IllegalStateException("This component is already out of commission post removal, recreate another.");
        super.setRenderer(renderer);
    }

    private static final class NotListener implements Listener {
    }
}
