package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.Table;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.*;

public class Renderer {

    @Getter
    @Setter
    private @Nullable Inventory inventory;
    private final Set<Component> components = new HashSet<>();
    private final Vector2i worldSpaceAnchor = new Vector2i(0, 0);

    public void insertComponents(@NotNull final Component... components) {
        Arrays.stream(components).forEach(this::insertComponent);
    }

    public void insertComponent(@NotNull final Component component) {
        this.components.add(component);
        component.setRenderer(this);
    }

    public boolean removeComponent(@NotNull final Component component) {
        component.setRenderedBy(null);
        return this.components.remove(component);
    }

    public List<Component> getComponents() {
        return new ArrayList<>(this.components);
    }

    public void shiftView(@NotNull Vector2i toShift) {
        this.worldSpaceAnchor.add(toShift);
    }

    @NotNull
    public Vector2i getViewAnchor() {
        return new Vector2i(this.worldSpaceAnchor);
    }

    public void render() {
        if (this.inventory == null)
            throw new IllegalStateException("No inventory set, could not render");
        tryRender(true);
    }

    public void tryRender(boolean force) {
        if (this.inventory == null) return;
        this.components.stream()
                .filter(component -> component.getSnapshot().collectAndCheckChanged())
                .sorted(Comparator.comparing(Component::getZIndex))
                .forEach(component -> renderComponent(this.inventory, component, force));
    }

    private void renderComponent(@NotNull final Inventory inventory, @NotNull Component component, boolean force) {
        if (!force && !component.getSnapshot().collectAndCheckChanged()) return;
        for (Table.Item<ItemStack> item : component.getItemTable()) {
            if (item.value() == null) continue;
            final int index;
            Vector2i pos = component.getPosition();

            if ((index = translateToIndex(
                    inventory,
                    new Vector2i(item.x() + pos.x, item.y() + pos.y),
                    component.isFloating()
            )) >= inventory.getSize() || index < 0)
                continue;
            inventory.setItem(index, item.value());
        }
    }

    /**
     * @param screenPosition the xy screen position to check
     * @return a list of components that contain the position, with larger z index appearing earlier in the list
     */
    public Map<Component, Vector2i> componentsAt(@NotNull Vector2i screenPosition) {
        return this.components.stream()
                .sorted(Comparator.reverseOrder())
                .reduce(new LinkedHashMap<>(), (map, component) -> {
                    Vector2i relative = clickedRelativePosition(component, screenPosition);
                    if (relative != null) map.put(component, relative);
                    return map;
                }, (a, b) -> {
                    a.putAll(b);
                    return a;
                });
    }

    /**
     * @return relative clicked position from the component
     */
     @Nullable
    public Vector2i clickedRelativePosition(@NotNull Component component, @NotNull Vector2i screenPosition) {
        final Vector2i componentPosition = component.getPosition();
        final var collisionTable = component.getCollisionTable();

        final boolean shouldBeAbsolute = !component.isFloating();
        final Vector2i clickPosition = shouldBeAbsolute ? getViewAnchor().add(screenPosition, new Vector2i()) : screenPosition;
        final Vector2i clickPosComponentSpace = new Vector2i(clickPosition)
                .sub(componentPosition);

        return Objects.equals(collisionTable.get(clickPosComponentSpace.x, clickPosComponentSpace.y), true) ? clickPosComponentSpace : null;
    }

    @Range(from = -1, to = 53)
    private int translateToIndex(Inventory inventory, @NotNull Vector2i position, boolean isLocalSpace) {
        assert inventory.getType() == InventoryType.CHEST;
        final int size = inventory.getSize();
        final int height = NumberConversions.ceil(size / 9f);
        final Vector2i centerIndices = getInventoryCenterIndices(height);
        final Vector2i offset = isLocalSpace ? position : position.sub(this.worldSpaceAnchor, new Vector2i());
        offset.y *= -1;
        final Vector2i finalPosition = centerIndices.add(offset);
        if (finalPosition.x < 0 || finalPosition.y < 0) return -1;
        if (finalPosition.x >= 9 || finalPosition.y >= height) return -1;
        return finalPosition.y() * 9 + finalPosition.x();
    }

    public Vector2i translateToVec(Inventory inventory, int i) {
        assert inventory.getType() == InventoryType.CHEST;
        final int size = inventory.getSize();
        final int height = NumberConversions.ceil(size / 9f);
        final Vector2i centerIndices = getInventoryCenterIndices(height);
        final Vector2i currentIndices = new Vector2i(i % 9, i / 9);
        return currentIndices.sub(centerIndices);
    }

    @NotNull
    private static Vector2i getInventoryCenterIndices(int height) {
        return new Vector2i(9 / 2, height / 2);
    }

}
