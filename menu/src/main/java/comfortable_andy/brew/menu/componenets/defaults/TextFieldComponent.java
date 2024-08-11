package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TextFieldComponent extends InventorySwitchingComponent<AnvilInventory> {

    private final RegisteredListener onPrepare;
    protected final AtomicReference<ItemStack> resultReference = new AtomicReference<>(null);

    public TextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position) {
        super(plugin, position);
        this.onPrepare = makeListener(plugin, (l, e) -> handleRenaming((PrepareAnvilEvent) e, resultReference));
        PrepareAnvilEvent.getHandlerList().register(this.onPrepare);
    }

    private void handleRenaming(PrepareAnvilEvent e, AtomicReference<ItemStack> resultReference) {
        if (e.getInventory() != this.openInv.get(e.getView().getPlayer()))
            return;
        resultReference.set(e.getResult());
        e.getView().setProperty(InventoryView.Property.REPAIR_COST, 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void handleClick(InventoryClickEvent e) {
        Inventory inventory = e.getClickedInventory();
        if (inventory == null) return;
        if (inventory != this.openInv.get(e.getWhoClicked())) return;
        e.setCancelled(true);
        if (e.getSlot() == 2) {
            ItemStack result = resultReference.get();
            String str = null;
            if (result != null && result.hasItemMeta())
                str = result.getItemMeta().getDisplayName();
            onEnterText(
                    e.getWhoClicked(),
                    Objects.requireNonNullElse(str, "")
            );
            close(e.getWhoClicked());
        }
    }

    public AnvilInventory getInventoryFor(HumanEntity entity) {
        final InventoryView view = entity.openAnvil(entity.getLocation(), true);
        assert view != null;
        final Inventory anvil = view.getTopInventory();
        return (AnvilInventory) anvil;
    }

    protected abstract void onEnterText(HumanEntity entity, String str);

    @Override
    public void postRemoval() {
        super.postRemoval();
        PrepareAnvilEvent.getHandlerList().unregister(this.onPrepare);
    }

    @Override
    public void setRenderer(@NotNull Renderer renderer) {
        if (this.isRemoved)
            throw new IllegalStateException("This component is already out of commission post removal, recreate another.");
        super.setRenderer(renderer);
    }

}
