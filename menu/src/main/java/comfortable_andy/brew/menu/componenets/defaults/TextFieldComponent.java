package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class TextFieldComponent extends StaticComponent {

    private static final NotListener LISTENER = new NotListener();

    protected final Inventory anvil;
    private boolean isRemoved = false;
    private final RegisteredListener onPrepare;
    private final RegisteredListener onClick;
    private final RegisteredListener onClose;

    @SuppressWarnings("deprecation")
    public TextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, @NotNull String> onEnter, Consumer<HumanEntity> onExit) {
        super(position);
        this.anvil = Bukkit.createInventory(null, InventoryType.ANVIL);
        this.anvil.setItem(0, new ItemStack(Material.PAPER));
        final AtomicReference<ItemStack> resultReference = new AtomicReference<>(null);
        this.onPrepare = makeListener(plugin, (l, e) -> {
            PrepareAnvilEvent event = (PrepareAnvilEvent) e;
            if (event.getInventory() != this.anvil) return;
            resultReference.set(event.getResult());
            event.getView().setProperty(InventoryView.Property.REPAIR_COST, 0);
        });
        this.onClick = makeListener(plugin, (l, e) -> {
            InventoryClickEvent event = (InventoryClickEvent) e;
            Inventory inventory = event.getClickedInventory();
            if (inventory != this.anvil) return;
            event.setCancelled(true);
            if (event.getSlot() == 2) {
                ItemStack result = resultReference.get();
                String str = null;
                if (result != null && result.hasItemMeta())
                    str = result.getItemMeta().getDisplayName();
                onEnter.accept(
                        event.getWhoClicked(),
                        Objects.requireNonNullElse(str, "")
                );
            }
        });
        this.onClose = makeListener(plugin, (l, e) -> {
            InventoryCloseEvent event = (InventoryCloseEvent) e;
            if (event.getView().getTopInventory() != this.anvil) return;
            onExit.accept(event.getPlayer());
            resultReference.set(null);
            Player player = (Player) event.getPlayer();
            player.giveExpLevels(0);
        });
        PrepareAnvilEvent.getHandlerList().register(this.onPrepare);
        InventoryClickEvent.getHandlerList().register(this.onClick);
        InventoryCloseEvent.getHandlerList().register(this.onClose);
    }

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
