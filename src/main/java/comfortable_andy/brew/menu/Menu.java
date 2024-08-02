package comfortable_andy.brew.menu;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Component;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.ToString;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

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
        this.renderer.removeComponent(component);
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
        final MenuAction.ActionCriteria criteria = new MenuAction.ActionCriteria(type, modifier);
        final Vector2i screenPosition = this.renderer
                .translateToVec(this.renderer.getInventory(), event.getSlot());
        boolean cancel = false;
        for (Component component : this.renderer.componentsAt(screenPosition)) {
            for (var entry : component.getActions().entrySet()) {
                if (entry.getValue().equals(criteria))
                    cancel = cancel || entry.getKey().tryRun(event.getWhoClicked(), screenPosition);
            }
        }
        if (cancel) {
            event.setCancelled(true);
            this.renderer.tryRender(false);
        }
    }

}
