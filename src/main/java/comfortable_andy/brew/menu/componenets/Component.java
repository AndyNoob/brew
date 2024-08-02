package comfortable_andy.brew.menu.componenets;

import com.google.common.base.Preconditions;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class Component {

    private boolean floating = false;
    private final @NotNull CollisionTable collisionTable;
    private final @NotNull ItemTable itemTable;
    /**
     * If the menu isn't moved, the center of a 5 row inventory is (0, 0), where up is +y and right is +x
     */
    private final @NotNull Vector2i position;

    private final Snapshot snapshot = new Snapshot(this::isFloating, this::getCollisionTable, this::getPosition, this::getItemTable);
    @Getter(AccessLevel.NONE)
    private Renderer renderedBy = null;
    /**
     * Works the same as CSS, larger z index gets rendered later (later means on top).
     */
    private int zIndex = 0;

    public void setRenderer(@NotNull Renderer renderer) {
        if (this.renderedBy != null) this.renderedBy.removeComponent(this);
        this.renderedBy = renderer;
        if (!renderer.getComponents().contains(this)) renderer.insertComponent(this);
    }

    public void putItem(@NotNull Vector2i localPosition, @NotNull ItemStack item) {
        Preconditions.checkArgument(
                !collisionTable.isOutside(localPosition.x(), localPosition.y()),
                "The position provided is not within the collision area!"
        );
        this.itemTable.set(localPosition.x(), localPosition.y(), item);
    }

    // TODO clicked event method

    public static class Snapshot {

        @SafeVarargs
        private Snapshot(@NotNull Supplier<Object>... suppliers) {
            Arrays.stream(suppliers).forEach(this::registerToCheck);
        }

        private final Map<Supplier<Object>, Object> states = new HashMap<>();

        /**
         * @param stateSupplier WILL be invoked <bold>REGULARLY</bold>, insert with caution.
         */
        public void registerToCheck(@NotNull Supplier<Object> stateSupplier) {
            this.states.put(stateSupplier, null);
        }

        /**
         * @return true if changed; false otherwise
         */
        public boolean checkChangedAndCollect() {
            boolean changed = false;

            for (Map.Entry<Supplier<Object>, Object> entry : this.states.entrySet()) {
                final Object newState = entry.getKey().get();

                if (Objects.equals(entry.getValue(), newState)) continue;

                changed = true;
                this.states.put(entry.getKey(), newState);
            }

            return changed;
        }

    }

}
