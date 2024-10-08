package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
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

import java.lang.ref.Cleaner;
import java.util.HashMap;
import java.util.Map;

public abstract class InventorySwitchingComponent<Inv extends Inventory> extends StaticComponent {

    private static final Cleaner CLEANER = Cleaner.create();

    private record CleaningAction(Map<RegisteredListener, HandlerList> listeners) implements Runnable {

        @Override
        public void run() {
            for (Map.Entry<RegisteredListener, HandlerList> entry : listeners.entrySet()) {
                entry.getValue().unregister(entry.getKey());
            }
        }

    }

    private static final NotListener LISTENER = new NotListener();
    private final Cleaner.Cleanable cleanable;
    protected final Map<RegisteredListener, HandlerList> listeners;
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
        this.onDrag = makeListener(plugin, (l, e) -> handleDrag((InventoryDragEvent) e));
        InventoryClickEvent.getHandlerList().register(this.onClick);
        InventoryCloseEvent.getHandlerList().register(this.onClose);
        InventoryDragEvent.getHandlerList().register(this.onDrag);
        this.listeners = new HashMap<>() {{
            put(onClick, InventoryClickEvent.getHandlerList());
            put(onClose, InventoryCloseEvent.getHandlerList());
            put(onDrag, InventoryDragEvent.getHandlerList());
        }};
        this.cleanable = CLEANER.register(this, new CleaningAction(this.listeners));
    }

    protected void handleClose(InventoryCloseEvent e) {
        Inventory inventory = e.getView().getTopInventory();
        if (inventory != this.openInv.get(e.getPlayer())) return;
        Player player = (Player) e.getPlayer();
        player.giveExpLevels(0);
        if (this.dontReopen.remove(e.getPlayer()) == inventory) return;
        reopenOriginal(e.getPlayer());
    }

    protected abstract void handleClick(InventoryClickEvent e);

    protected abstract void handleDrag(InventoryDragEvent e);

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
        this.cleanable.clean();
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
