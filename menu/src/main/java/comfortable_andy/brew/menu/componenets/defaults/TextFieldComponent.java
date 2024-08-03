package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class TextFieldComponent extends StaticComponent {

    private static final NotListener LISTENER = new NotListener();

    protected final AnvilInventory anvil;
    private boolean isRemoved = false;
    private final RegisteredListener onPrepare;
    private final RegisteredListener onClick;
    private final RegisteredListener onClose;

    public TextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, @Nullable String> consumer) {
        super(position);
        this.anvil = (AnvilInventory) Bukkit.createInventory(null, InventoryType.ANVIL);
        this.anvil.setFirstItem(new ItemStack(Material.PAPER));
        this.onPrepare = makeListener(plugin, (l, e) -> {
            PrepareAnvilEvent event = (PrepareAnvilEvent) e;
            if (event.getInventory() != this.anvil) return;
            this.anvil.setRepairCost(0);
        });
        this.onClick = makeListener(plugin, (l, e) -> {
            InventoryClickEvent event = (InventoryClickEvent) e;
            if (event.getClickedInventory() != this.anvil) return;
            event.setCancelled(true);
            if (event.getSlot() == 2) {
                consumer.accept(
                        event.getWhoClicked(),
                        Objects.requireNonNullElse(
                                this.anvil.getRenameText(),
                                ""
                        )
                );
            }
        });
        this.onClose = makeListener(plugin, (l, e) -> {
            InventoryCloseEvent event = (InventoryCloseEvent) e;
            if (event.getView().getTopInventory() != this.anvil) return;
            consumer.accept(event.getPlayer(), null);
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
