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
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @see Direction
 */
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
        // TODO make snapshots actually work
        final Set<Component> rendering;
        if (force) {
            System.out.println("force");
            this.inventory.clear();
            rendering = this.components.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
        } else {
            rendering = new LinkedHashSet<>();
            for (Component component : this.components) {
                final Component.Snapshot snapshot = component.getSnapshot();
                final boolean checkedBefore = snapshot.hasChecked();
                final var itemSnapping = snapshot.getItems().clone();
                final var posSnapping = snapshot.getPosition().clone();
                final var floatingSnapping = snapshot.getFloating().clone();
                final var anchor = snapshot.getViewAnchor().clone();
                final boolean changed = snapshot.collectAndCheckChanged();
                if (!checkedBefore) {
                    rendering.add(component);
                    continue;
                }
                if (!changed) continue;
                final Vector2i oldPos = posSnapping.getVal();
                if (Objects.equals(true, floatingSnapping.getVal())) {
                    oldPos.add(anchor.getVal() == null ? getViewAnchor() : anchor.getVal());
                }
                final Vector2i newPos = snapshot.getPosition().getVal();
                if (Objects.equals(true, snapshot.getFloating().getVal())) {
                    newPos.add(getViewAnchor());
                }
                final Set<Vector2i> changedPos = new HashSet<>();
                final boolean itemsChanged = !itemSnapping.equals(snapshot.getItems());
                final boolean anchorChanged = !Objects.equals(anchor.getVal(), getViewAnchor());
                System.out.println(itemsChanged + " || " + anchorChanged);
                System.out.println(itemSnapping.getVal() + " -> " + snapshot.getItems().getVal());
                System.out.println(anchor.getVal() + " -> " + snapshot.getViewAnchor().getVal() + " or " + getViewAnchor());
                if (itemsChanged || anchorChanged) {
                    System.out.println("updating item");
                    for (Table.Item<ItemStack> item : itemSnapping.getVal()) {
                        final Vector2i relPos = new Vector2i(item.x(), item.y());
                        final Vector2i actualPos = relPos.add(oldPos, new Vector2i());
                        rendering.addAll(componentsAt(actualPos.sub(getViewAnchor(), new Vector2i())).keySet());
                        changedPos.add(actualPos);
                    }
                    for (Table.Item<ItemStack> item : snapshot.getItems().getVal()) {
                        final Vector2i relPos = new Vector2i(item.x(), item.y());
                        final Vector2i actualPos = relPos.add(newPos, new Vector2i());
                        rendering.addAll(componentsAt(actualPos.sub(getViewAnchor(), new Vector2i())).keySet());
                        changedPos.add(actualPos);
                    }
                    for (Vector2i pos : changedPos) {
                        int i = this.translateToIndex(this.inventory, pos, false);
                        if (i != -1)
                            this.inventory.setItem(i, null);
                    }
                }
            }
        }
        System.out.println("rendering [" + rendering.stream().map(c -> c.getClass().getSimpleName()).collect(Collectors.joining(", ")) + "]");
        if (force) rendering.forEach(c -> this.renderComponent(this.inventory, c));
    }

    private void renderComponent(@NotNull final Inventory inventory, @NotNull Component component) {
        final Vector2i pos = component.getPosition();
        for (Table.Item<ItemStack> item : component.getItemTable()) {
            if (item.value() == null) continue;
            final int index;

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
     * @return a map of components that contain the position, with larger z index appearing earlier in the list AND the {@link Renderer#clickedRelativePosition(Component, Vector2i)} of the component
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

        final Vector2i clickPosition = component.isFloating() ? screenPosition : getViewAnchor().add(screenPosition, new Vector2i());
        final Vector2i clickPosComponentSpace = new Vector2i(clickPosition)
                .sub(componentPosition);

        return Objects.equals(collisionTable.get(clickPosComponentSpace.x, clickPosComponentSpace.y), true) ? clickPosComponentSpace : null;
    }

    @Range(from = -1, to = 53)
    private int translateToIndex(Inventory inventory, @NotNull Vector2i position, boolean isLocalSpace) {
        assert inventory.getType() == InventoryType.CHEST;
        final int size = inventory.getSize();
        final int height = NumberConversions.ceil(size / 9f);
        final Vector2i rowColumn = getInventoryCenterRowColumn(height);
        final Vector2i offset = isLocalSpace ? position : position.sub(this.worldSpaceAnchor, new Vector2i());
        offset.y *= -1;
        offset.y *= Direction.UP.get().y;
        offset.x *= Direction.RIGHT.get().x;
        final Vector2i finalPosition = rowColumn.add(offset);
        if (finalPosition.x < 0 || finalPosition.x >= 9) return -1;
        if (finalPosition.y < 0 || finalPosition.y >= height) return -1;
        return finalPosition.y() * 9 + finalPosition.x();
    }

    public Vector2i translateToScreenSpaceVec(Inventory inventory, int i) {
        assert inventory.getType() == InventoryType.CHEST;
        final int size = inventory.getSize();
        final int height = NumberConversions.ceil(size / 9f);
        final Vector2i centerRowColumn = getInventoryCenterRowColumn(height);
        final Vector2i currentRowColumn = new Vector2i(i % 9, i / 9);
        return currentRowColumn.sub(centerRowColumn)
                .mul(1, -1)
                .mul(Direction.RIGHT.get().x, Direction.UP.get().y);
    }

    @NotNull
    private static Vector2i getInventoryCenterRowColumn(int height) {
        return new Vector2i(9 / 2, height / 2);
    }

    public static final class Direction {

        public static final Supplier<Vector2i> UP = () -> new Vector2i(0, 1);
        public static final Supplier<Vector2i> DOWN = () -> UP.get().negate();
        public static final Supplier<Vector2i> RIGHT = () -> new Vector2i(1, 0);
        public static final Supplier<Vector2i> LEFT = () -> RIGHT.get().negate();

    }

}
