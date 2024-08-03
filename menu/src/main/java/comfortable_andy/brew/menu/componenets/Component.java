package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.*;
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
@ToString
public abstract class Component implements Comparable<Component> {

    private boolean floating = false;
    private final @NotNull CollisionTable collisionTable;
    private final @NotNull ItemTable itemTable;
    /**
     * If the menu isn't moved, the center of a 5 row inventory is (0, 0), where up is +y and right is +x.
     * This acts as the center of {@link Component#collisionTable} and {@link Component#itemTable}
     */
    private final @NotNull Vector2i position;
    private final Map<MenuAction, MenuAction.ActionCriteria> actions = new HashMap<>();

    private final Snapshot snapshot = new Snapshot(
            this::isFloating,
            this::getCollisionTable,
            this::getPosition,
            this::getItemTable,
            () -> this.renderedBy == null ? null : this.renderedBy.getViewAnchor()
    );
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    private Renderer renderedBy = null;
    /**
     * Works the same as CSS, larger z index gets rendered on top
     */
    private int zIndex = 0;

    public void setRenderer(@NotNull Renderer renderer) {
        if (this.renderedBy != null) this.renderedBy.removeComponent(this);
        this.renderedBy = renderer;
        if (!renderer.getComponents().contains(this)) renderer.insertComponent(this);
    }

    public void postRemoval() {
    }

    @Override
    public int compareTo(@NotNull Component o) {
        return Integer.compare(zIndex, o.zIndex);
    }

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
        public boolean collectAndCheckChanged() {
            boolean changed = false;

            for (Map.Entry<Supplier<Object>, Object> entry : this.states.entrySet()) {
                final Object oldState = entry.getValue();
                final Object newState = entry.getKey().get();

                entry.setValue(newState);

                if (!Objects.equals(oldState, newState)) changed = true;
            }

            return changed;
        }

    }

}
