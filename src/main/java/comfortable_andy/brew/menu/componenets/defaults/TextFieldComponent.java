package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Objects;
import java.util.function.BiConsumer;

public abstract class TextFieldComponent extends StaticComponent {

    private static final NotListener LISTENER = new NotListener();

    private final AnvilInventory anvil;
    private final RegisteredListener onPrepare;
    private final RegisteredListener onClick;

    public TextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, String> consumer) {
        super(position);
        this.anvil = (AnvilInventory) Bukkit.createInventory(null, InventoryType.ANVIL);
        this.anvil.setFirstItem(new ItemStack(Material.PAPER));
        this.onPrepare = new RegisteredListener(
                LISTENER,
                (l, e) -> {
                    PrepareAnvilEvent event = (PrepareAnvilEvent) e;
                    if (event.getInventory() != this.anvil) return;
                    this.anvil.setRepairCost(0);
                },
                EventPriority.NORMAL,
                plugin,
                false
        );
        this.onClick = new RegisteredListener(
                LISTENER,
                (l, e) -> {
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
                },
                EventPriority.NORMAL,
                plugin,
                false
        );
        PrepareAnvilEvent.getHandlerList().register(this.onPrepare);
        InventoryClickEvent.getHandlerList().register(this.onClick);
    }

    @Override
    public void postRemoval() {
        PrepareAnvilEvent.getHandlerList().unregister(this.onPrepare);
        InventoryClickEvent.getHandlerList().unregister(this.onClick);
    }

    private static final class NotListener implements Listener {
    }

}
