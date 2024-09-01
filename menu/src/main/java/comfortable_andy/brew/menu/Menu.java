package comfortable_andy.brew.menu;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Component;
import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.defaults.ItemInletComponent;
import comfortable_andy.brew.menu.componenets.defaults.ItemOutletComponent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
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
        Inventory clickedInventory = event.getClickedInventory();
        Inventory otherInventory = event.getView().getTopInventory() == clickedInventory ? event.getView().getBottomInventory() : event.getView().getTopInventory();
        if (clickedInventory != renderer.getInventory() && otherInventory != renderer.getInventory()) return;
        HumanEntity entity = event.getWhoClicked();
        boolean clickedMenu = clickedInventory == renderer.getInventory();
        switch (event.getAction()) {
            case MOVE_TO_OTHER_INVENTORY -> {
                if(!handleMoveItems(event.getView(), !clickedMenu, entity, event.getSlot(), -1, event::setCurrentItem))
                    event.setCancelled(true);
            }
            case COLLECT_TO_CURSOR -> {
                final ItemStack collecting = event.getCursor();
                for (ItemStack stack : otherInventory) {
                    if (collecting.isSimilar(stack)) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
        if (event.getAction().name().contains("PLACE")) {
            if (handleMoveItems(
                    event.getView(),
                    clickedMenu,
                    entity,
                    -1,
                    event.getSlot(),
                    entity::setItemOnCursor
            )) {
                event.setCancelled(true);
                return;
            }
        } else if (event.getClick() == ClickType.NUMBER_KEY) {
            if (!clickedMenu) return;
            handleMoveItems(
                    event.getView(),
                    true,
                    entity,
                    event.getHotbarButton(),
                    event.getSlot(),
                    i -> event.getView().getBottomInventory().setItem(event.getHotbarButton(), i)
            );
            handleMoveItems(
                    event.getView(),
                    false,
                    entity,
                    event.getSlot(),
                    event.getHotbarButton(),
                    null
            );
        } else if (event.getAction().name().contains("PICKUP") && clickedMenu) {
            if (handleMoveItems(
                    event.getView(),
                    false,
                    entity,
                    event.getSlot(),
                    -1,
                    null
            )) {
                event.setCancelled(true);
                return;
            }
        }
        if (!clickedMenu) return;
        MenuAction.ActionModifier modifier = MenuAction.ActionModifier.NONE;
        final MenuAction.ActionType type = switch (event.getClick()) {
            case DROP, LEFT, SWAP_OFFHAND -> MenuAction.ActionType.LEFT;
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
        boolean cancel = handleClick(clickedInventory, type, modifier, event.getSlot(), entity);
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

    public boolean handleMoveItems(InventoryView view, boolean toMenu, HumanEntity who, @Range(from = -1, to = 53) int from, @Range(from = -1, to = 53) int destination, @Nullable Consumer<ItemStack> remainderHandler) {
        if (toMenu) {
            ItemStack moving = from == -1 ? who.getItemOnCursor() : view.getBottomInventory().getItem(from);
            final Map<Component, Vector2i> componentsAt;
            if (destination == -1)
                componentsAt = this.renderer.getComponentsInView(view.getTopInventory(), true);
            else {
                componentsAt = this.renderer.componentsAt(
                        this.renderer.translateToScreenSpaceVec(
                                view.getTopInventory(),
                                destination
                        ),
                        true,
                        true
                );
            }
            List<Component> components = new ArrayList<>(componentsAt.keySet());
            if (components.size() != 1) return false;
            Component component = components.get(0);
            if (!(component instanceof ItemInletComponent inlet)) return false;
            assert remainderHandler != null;
            remainderHandler.accept(inlet.tryTakeInItems(who, destination == -1 ? null : componentsAt.get(component), moving));
        } else {
            if (from == -1) return false;
            final var componentsAt = this.renderer.componentsAt(
                    this.renderer.translateToScreenSpaceVec(
                            view.getTopInventory(),
                            from
                    ),
                    true,
                    true
            );
            List<Component> components = new ArrayList<>(componentsAt.keySet());
            if (components.size() != 1) return false;
            Component component = components.get(0);
            if (!(component instanceof ItemOutletComponent outlet)) return false;
            ItemStack out = outlet.tryTakeOutItems(who, componentsAt.get(component));
            if (out == null) return false;
            if (destination == -1) {
                if (who.getItemOnCursor().isEmpty()) who.setItemOnCursor(out);
                else {
                    for (Map.Entry<Integer, ItemStack> entry : view.getBottomInventory().addItem(out).entrySet()) {
                        who.getWorld().dropItem(who.getEyeLocation(), entry.getValue());
                    }
                };
            } else {
                view.getBottomInventory().setItem(destination, out);
            }
        }
        this.renderer.render();
        return true;
    }

}
