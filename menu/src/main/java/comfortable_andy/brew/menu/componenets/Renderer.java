package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.Table;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
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
        final Set<Component> rendering;
        if (force) {
            resetCanvas(this.inventory, true);
            rendering = this.components.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
            for (Component component : rendering) {
                component.getSnapshot().collectIfNotChecked();
            }
        } else {
            resetCanvas(this.inventory, false);
            rendering = new LinkedHashSet<>();
            for (Component component : this.components) {
                final Component.Snapshot snapshot = component.getSnapshot();

                if (snapshot.lastSnapTick() == -1) {
                    // if this component has ever been rendered, this shouldn't be reached
                    rendering.add(component);
                    continue;
                }

                final var itemSnapping = snapshot.getItems().clone();
                final var posSnapping = snapshot.getPosition().clone();
                final var floatingSnapping = snapshot.getFloating().clone();
                final var anchorSnapping = snapshot.getViewAnchor().clone();

                if (!snapshot.collectAndCheckChanged()) {
                    System.out.println("nothing changed");
                    System.out.println(itemSnapping.getVal());
                    System.out.println(snapshot.getItems().getVal());
                    System.out.println(Bukkit.getCurrentTick());
                    continue;
                }

                final Vector2i oldPos = posSnapping.getVal();
                if (floatingSnapping.getVal() && anchorSnapping.getVal() != null)
                    oldPos.add(anchorSnapping.getVal());
                final Vector2i newPos = snapshot.getPosition().getVal();
                if (snapshot.getFloating().getVal() && snapshot.getViewAnchor().getVal() != null)
                    newPos.add(snapshot.getViewAnchor().getVal());

                final boolean itemsChanged = !itemSnapping.equals(snapshot.getItems());
                final boolean anchorChanged = !Objects.equals(anchorSnapping.getVal(), snapshot.getViewAnchor().getVal());

                if (itemsChanged || anchorChanged) {
                    // setting screen space to false because the positions
                    // has already been converted to world space (if needed)
                    for (Table.Item<ItemStack> item : itemSnapping.getVal()) {
                        final Vector2i relPos = new Vector2i(item.x(), item.y());
                        final Vector2i actualPos = relPos.add(oldPos, new Vector2i());
                        rendering.addAll(componentsAt(actualPos, false, false).keySet());
                    }
                    for (Table.Item<ItemStack> item : snapshot.getItems().getVal()) {
                        final Vector2i relPos = new Vector2i(item.x(), item.y());
                        final Vector2i actualPos = relPos.add(newPos, new Vector2i());
                        rendering.addAll(componentsAt(actualPos, false, false).keySet());
                    }
                }
            }
        }
        rendering.forEach(c -> this.renderComponent(this.inventory, c));
    }

    private void resetCanvas(Inventory inventory, boolean hard) {
        if (hard) {
            inventory.clear();
        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                if (componentsAt(translateToScreenSpaceVec(inventory, i), true, false).isEmpty()) {
                    inventory.setItem(i, null);
                }
            }
        }
    }

    private void renderComponent(@NotNull final Inventory inventory, @NotNull Component component) {
        final Vector2i pos = component.getPosition();
        for (Table.Item<ItemStack> item : component.getItemTable()) {
            ItemStack stack = item.value();
            if (stack == null) continue;
            final int index;

            if ((index = translateToIndex(
                    inventory,
                    new Vector2i(item.x() + pos.x, item.y() + pos.y),
                    component.isFloating()
            )) == -1) continue;
            inventory.setItem(index, stack);
        }
    }

    /**
     * @param position the xy screen position to check
     * @return a map of components that contain the position, with larger z index appearing earlier in the list AND the relative position to the component
     */
    public Map<Component, Vector2i> componentsAt(@NotNull Vector2i position, boolean convertToWorldSpace, boolean checkCollision) {
        // convert everything to world space
        if (convertToWorldSpace) {
            position = position.add(getViewAnchor(), new Vector2i());
        }
        @NotNull Vector2i finalPosition = position;
        return this.components.stream()
                .sorted(Comparator.reverseOrder())
                .reduce(new LinkedHashMap<>(), (map, component) -> {
                    final Vector2i componentPosition = new Vector2i(component.getPosition());
                    if (component.isFloating()) componentPosition.add(getViewAnchor());
                    final Table<?, ?> table = checkCollision ? component.getCollisionTable() : component.getItemTable();
                    for (Table.Item<?> item : table) {
                        Vector2i relPos = new Vector2i(item.x(), item.y());
                        if (componentPosition.add(relPos, new Vector2i()).equals(finalPosition)) {
                            map.put(component, relPos);
                        }
                    }
                    return map;
                }, (a, b) -> {
                    a.putAll(b);
                    return a;
                });
    }

    @Range(from = -1, to = 53)
    private int translateToIndex(Inventory inventory, @NotNull Vector2i position, boolean isPositionScreenSpace) {
        assert inventory.getType() == InventoryType.CHEST;
        final int size = inventory.getSize();
        final int height = NumberConversions.ceil(size / 9f);
        final Vector2i rowColumn = getInventoryCenterRowColumn(height);
        final Vector2i offset = isPositionScreenSpace ? position : position.sub(this.worldSpaceAnchor, new Vector2i());
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
