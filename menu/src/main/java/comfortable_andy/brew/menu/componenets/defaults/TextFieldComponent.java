package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TextFieldComponent extends StaticComponent {

    private static final NotListener LISTENER = new NotListener();

    protected final Map<HumanEntity, Inventory> openAnvils = new HashMap<>();
    protected final Map<HumanEntity, Inventory> dontReopen = new HashMap<>();
    private boolean isRemoved = false;
    private final RegisteredListener onPrepare;
    private final RegisteredListener onClick;
    private final RegisteredListener onClose;

    @SuppressWarnings("deprecation")
    public TextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position) {
        super(position);
        final AtomicReference<ItemStack> resultReference = new AtomicReference<>(null);
        this.onPrepare = makeListener(plugin, (l, e) -> {
            PrepareAnvilEvent event = (PrepareAnvilEvent) e;
            if (event.getInventory() != this.openAnvils.get(event.getView().getPlayer()))
                return;
            resultReference.set(event.getResult());
            event.getView().setProperty(InventoryView.Property.REPAIR_COST, 0);
        });
        this.onClick = makeListener(plugin, (l, e) -> {
            InventoryClickEvent event = (InventoryClickEvent) e;
            Inventory inventory = event.getClickedInventory();
            if (inventory == null) return;
            if (inventory != this.openAnvils.get(event.getWhoClicked())) return;
            event.setCancelled(true);
            if (event.getSlot() == 2) {
                ItemStack result = resultReference.get();
                String str = null;
                if (result != null && result.hasItemMeta())
                    str = result.getItemMeta().getDisplayName();
                onEnterText(
                        event.getWhoClicked(),
                        Objects.requireNonNullElse(str, "")
                );
                reopenOriginal(event.getWhoClicked());
                this.dontReopen.put(event.getWhoClicked(), inventory);
            }
        });
        this.onClose = makeListener(plugin, (l, e) -> {
            InventoryCloseEvent event = (InventoryCloseEvent) e;
            Inventory inventory = event.getView().getTopInventory();
            if (inventory != this.openAnvils.remove(event.getPlayer())) return;
            resultReference.set(null);
            Player player = (Player) event.getPlayer();
            player.giveExpLevels(0);
            if (this.dontReopen.remove(event.getPlayer()) == inventory) return;
            reopenOriginal(event.getPlayer());
        });
        PrepareAnvilEvent.getHandlerList().register(this.onPrepare);
        InventoryClickEvent.getHandlerList().register(this.onClick);
        InventoryCloseEvent.getHandlerList().register(this.onClose);
    }

    public Inventory open(HumanEntity entity) {
        final InventoryView view = entity.openAnvil(entity.getLocation(), true);
        assert view != null;
        final Inventory anvil = view.getTopInventory();
        this.openAnvils.put(entity, anvil);
        return anvil;
    }

    protected abstract void onEnterText(HumanEntity entity, String str);

    protected abstract void reopenOriginal(HumanEntity entity);

    @NotNull
    private RegisteredListener makeListener(@NotNull JavaPlugin plugin, EventExecutor executor) {
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
        System.out.println("remove");
        this.isRemoved = true;
        PrepareAnvilEvent.getHandlerList().unregister(this.onPrepare);
        InventoryClickEvent.getHandlerList().unregister(this.onClick);
        InventoryCloseEvent.getHandlerList().unregister(this.onClose);
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
