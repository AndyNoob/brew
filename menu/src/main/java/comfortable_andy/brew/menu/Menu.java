package comfortable_andy.brew.menu;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Component;
import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @see Menu#getRenderer()
 * @see Renderer#render()
 * @see Renderer#tryRender(boolean)
 * @see Menu#updateInventoryView(InventoryView)
 * @see Menu#handleClick(InventoryClickEvent)
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Menu extends Displaying {

    private final Renderer renderer = new Renderer();

    public Menu(String id, String displayName, String description) {
        super(id, displayName, description);
    }

    public void addComponent(@NotNull comfortable_andy.brew.menu.componenets.Component component) {
        this.renderer.insertComponent(component);
    }

    public void removeComponent(@NotNull comfortable_andy.brew.menu.componenets.Component component) {
        if (this.renderer.removeComponent(component)) {
            component.postRemoval();
        }
    }

    public void updateInventoryView(InventoryView view) {
        String title = LegacyComponentSerializer.legacySection().serialize(getDisplayName());
        view.setTitle(title);
    }

    public void handleClick(InventoryClickEvent event) {
        if (this.renderer.getInventory() == null) return;
        if (event.getClickedInventory() != this.renderer.getInventory()) {
            switch (event.getAction()) {
                case MOVE_TO_OTHER_INVENTORY -> event.setCancelled(true);
                case COLLECT_TO_CURSOR -> {
                    final ItemStack collecting = event.getCursor();
                    for (ItemStack stack : event.getView().getTopInventory()) {
                        if (collecting.isSimilar(stack)) {
                            event.setCancelled(true);
                            break;
                        }
                    }
                }
            }
            return;
        }
        MenuAction.ActionModifier modifier = MenuAction.ActionModifier.NONE;
        final MenuAction.ActionType type = switch (event.getClick()) {
            case DROP, LEFT, SWAP_OFFHAND, NUMBER_KEY -> MenuAction.ActionType.LEFT;
            case DOUBLE_CLICK -> {
                modifier = MenuAction.ActionModifier.DOUBLE;
                yield MenuAction.ActionType.LEFT;
            }
            case RIGHT -> MenuAction.ActionType.RIGHT;
            case MIDDLE -> MenuAction.ActionType.MIDDLE;
            case SHIFT_LEFT, CONTROL_DROP -> {
                modifier = MenuAction.ActionModifier.SHIFT;
                yield MenuAction.ActionType.LEFT;
            }
            case SHIFT_RIGHT -> {
                modifier = MenuAction.ActionModifier.SHIFT;
                yield MenuAction.ActionType.RIGHT;
            }
            default -> null;
        };
        if (type == null) return;
        boolean cancel = handleClick(event.getClickedInventory(), type, modifier, event.getSlot(), event.getWhoClicked());
        if (cancel) {
            event.setCancelled(true);
            this.renderer.tryRender(false);
        }
    }

    public void handleClick(InventoryDragEvent event) {
        final List<Integer> coveredSlots = new ArrayList<>();
        for (Integer slot : event.getRawSlots()) {
            if (event.getView().getInventory(slot) == renderer.getInventory())
                coveredSlots.add(slot);
        }
        if (coveredSlots.isEmpty()) return;
        Integer first = coveredSlots.get(0);
        boolean cancel = handleClick(
                event.getInventory(),
                MenuAction.ActionType.LEFT,
                MenuAction.ActionModifier.NONE,
                event.getView().convertSlot(first),
                event.getWhoClicked()
        );
        if (cancel) event.setCancelled(true);
    }

    public boolean handleClick(Inventory inventory, MenuAction.ActionType type, @Nullable MenuAction.ActionModifier modifier, int slot, HumanEntity whoClicked) {
        final MenuAction.ActionCriteria criteria = new MenuAction.ActionCriteria(type, modifier);
        final Vector2i screenPosition = this.renderer
                .translateToScreenSpaceVec(inventory, slot);
        boolean ran = false;
        boolean cancel = false;
        for (Map.Entry<Component, Vector2i> e : this.renderer.componentsAt(screenPosition, true, true).entrySet()) {
            Component component = e.getKey();
            for (var actionEntry : component.getActions().entrySet()) {
                if (actionEntry.getValue().equals(criteria)) {
                    cancel = cancel || actionEntry.getKey().tryRun(whoClicked, e.getValue());
                    ran = true;
                }
            }
        }
        return !ran || cancel;
    }

}
