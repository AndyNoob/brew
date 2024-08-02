package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.Table;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Renderer {

    private final Set<Component> components = new HashSet<>();
    private final Vector2i worldSpaceAnchor = new Vector2i(0, 0);

    public void insertComponents(@NotNull final Component... components) {
        Arrays.stream(components).forEach(this::insertComponent);
    }

    public void insertComponent(@NotNull final Component component) {
        this.components.add(component);
        component.setRenderer(this);
    }

    public void removeComponent(@NotNull final Component component) {
        component.setRenderedBy(null);
        this.components.remove(component);
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

    public void renderTo(@NotNull final Inventory inventory) {
        this.components.stream()
                .filter(component -> component.getSnapshot().checkChangedAndCollect())
                .sorted(Comparator.comparing(Component::getZIndex))
                .forEach(component -> renderComponent(inventory, component));
    }

    private void renderComponent(@NotNull final Inventory inventory, @NotNull Component component) {
        for (Table.Item<ItemStack> item : component.getItemTable()) {
            if (item.value() == null) continue;
            final int index;
            if ((index = translateToIndex(new Vector2i(item.x(), item.y()), component.isFloating())) >= inventory.getSize())
                continue;
            inventory.setItem(index, item.value());
        }
    }

    /**
     * @param localPosition the xy position to check
     * @return a list of components that contain the position, with larger z index appearing earlier in the list
     */
    public List<Component> componentsAt(@NotNull Vector2i localPosition) {
        return this.components.stream()
                .sorted(Comparator.comparing(Component::getZIndex).reversed())
                .filter(component -> doesComponentContain(localPosition, component))
                .toList();
    }

    public boolean doesComponentContain(@NotNull Vector2i localPosition, @NotNull Component component) {
        final var collisionTable = component.getCollisionTable();

        if (component.isFloating())
            return Objects.equals(collisionTable.get(localPosition.x(), localPosition.y()), true);

        final Vector2i worldPosition = worldSpaceAnchor.add(localPosition, new Vector2i());
        final Vector2i tableCenterToComponentPosition = component
                .getPosition()
                .sub(component.getCollisionTable().getCenter(), new Vector2i());

        final int width = collisionTable.getWidth();
        final int height = collisionTable.getHeight();

        if (width <= 0 || height <= 0) return false;

        // first do an aabb check
        final Vector2i mins = component.getPosition()
                .add(tableCenterToComponentPosition, new Vector2i());
        final Vector2i maxes = new Vector2i(width - 1, height - 1)
                .add(component.getPosition())
                .add(tableCenterToComponentPosition);

        if (worldPosition.x() < mins.x() || worldPosition.x() > maxes.x()) return false;
        if (worldPosition.y() < mins.y() || worldPosition.y() > maxes.y()) return false;

        // then do the iteration
        for (Table.Item<Boolean> item : component.getCollisionTable()) {
            if (Objects.equals(item.value(), false)) continue;
            final Vector2i itemWorldPosition = new Vector2i(item.x(), item.y()).add(component.getPosition()).add(tableCenterToComponentPosition);
            if (!itemWorldPosition.equals(worldPosition)) continue;
            return true;
        }

        return false;
    }

    private int translateToIndex(@NotNull Vector2i position, boolean isLocalSpace) {
        final Vector2i localPosition = isLocalSpace ? position : position.sub(this.worldSpaceAnchor, new Vector2i());
        return localPosition.y() * 9 + localPosition.x();
    }

}
