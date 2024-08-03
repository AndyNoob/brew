package comfortable_andy.brew.menu;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Component;
import lombok.EqualsAndHashCode;

import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

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
        if (event.getClickedInventory() != this.renderer.getInventory()) return;
        MenuAction.ActionModifier modifier = null;
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

    public boolean handleClick(Inventory inventory, MenuAction.ActionType type, @Nullable MenuAction.ActionModifier modifier, int slot, HumanEntity whoClicked) {
        final MenuAction.ActionCriteria criteria = new MenuAction.ActionCriteria(type, modifier);
        final Vector2i screenPosition = this.renderer
                .translateToVec(inventory, slot);
        boolean cancel = false;
        for (Map.Entry<Component, Vector2i> e : this.renderer.componentsAt(screenPosition).entrySet()) {
            Component component = e.getKey();
            for (var actionEntry : component.getActions().entrySet()) {
                if (actionEntry.getValue().equals(criteria))
                    cancel = cancel || actionEntry.getKey().tryRun(whoClicked, e.getValue());
            }
        }
        return cancel;
    }

}