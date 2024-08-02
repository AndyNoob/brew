package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.Table;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Renderer {

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

    public void render() {
        if (this.inventory == null)
            throw new IllegalStateException("No inventory set, could not render");
        tryRender();
    }

    public void tryRender() {
        if (this.inventory == null) return;
        this.components.stream()
                .filter(component -> component.getSnapshot().checkChangedAndCollect())
                .sorted(Comparator.comparing(Component::getZIndex))
                .forEach(component -> renderComponent(this.inventory, component));
    }

    private void renderComponent(@NotNull final Inventory inventory, @NotNull Component component) {
        for (Table.Item<ItemStack> item : component.getItemTable()) {
            if (item.value() == null) continue;
            final int index;
            if ((index = translateToIndex(inventory, new Vector2i(item.x(), item.y()), component.isFloating())) >= inventory.getSize() || index < 0)
                continue;
            inventory.setItem(index, item.value());
        }
    }

    /**
     * @param screenPosition the xy screen position to check
     * @return a list of components that contain the position, with larger z index appearing earlier in the list
     */
    public List<Component> componentsAt(@NotNull Vector2i screenPosition) {
        return this.components.stream()
                .sorted(Comparator.comparing(Component::getZIndex).reversed())
                .filter(component -> doesComponentContain(component, screenPosition))
                .toList();
    }

    public boolean doesComponentContain(@NotNull Component component, @NotNull Vector2i screenPosition) {
        final Vector2i componentPosition = component.getPosition();
        screenPosition = new Vector2i(screenPosition).add(componentPosition);
        final var collisionTable = component.getCollisionTable();

        if (component.isFloating())
            return Objects.equals(collisionTable.get(screenPosition.x(), screenPosition.y()), true);

        final Vector2i clickPosition = getViewAnchor().add(screenPosition, new Vector2i());
        final Vector2i clickPosComponentSpace = new Vector2i(clickPosition)
                .sub(componentPosition);

        return Objects.equals(collisionTable.get(clickPosComponentSpace.x, clickPosComponentSpace.y), true);
    }

    @Range(from = -1, to = 53)
    private int translateToIndex(Inventory inventory, @NotNull Vector2i position, boolean isLocalSpace) {
        assert inventory.getType() == InventoryType.CHEST;
        final int size = inventory.getSize();
        final int height = NumberConversions.ceil(size / 9f);
        final Vector2i centerIndices = new Vector2i(9 / 2, height / 2);
        final Vector2i offset = isLocalSpace ? position : position.sub(this.worldSpaceAnchor, new Vector2i());
        offset.y *= -1;
        final Vector2i finalPosition = centerIndices.add(offset);
        if (finalPosition.x < 0 || finalPosition.y < 0) return -1;
        if (finalPosition.x >= 9 || finalPosition.y >= height) return -1;
        return -finalPosition.y() * 9 + finalPosition.x();
    }

}
