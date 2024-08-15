package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

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

    private final Snapshot snapshot = Snapshot.builder()
            .collision(() -> getCollisionTable().clone())
            .items(() -> getItemTable().clone())
            .position(() -> new Vector2i(getPosition()))
            .viewAnchor(() -> this.renderedBy == null ? null : this.renderedBy.getViewAnchor())
            .state("floating", this::isFloating)
            .build();
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PACKAGE)
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

    @Getter
    public static class Snapshot {

        private final Supplier<CollisionTable> collision;
        private final Supplier<ItemTable> items;
        private final Supplier<Vector2i> position;
        private final Supplier<Vector2i> viewAnchor;
        @Getter(AccessLevel.NONE)
        private final Map<String, Supplier<Object>> states;
        @Getter(AccessLevel.NONE)
        private final Map<String, Object> prevStates = new HashMap<>();

        @Builder
        public Snapshot(Supplier<CollisionTable> collision, Supplier<ItemTable> items, Supplier<Vector2i> position, Supplier<Vector2i> viewAnchor, @Singular Map<String, Supplier<Object>> states) {
            this.collision = collision;
            this.items = items;
            this.position = position;
            this.viewAnchor = viewAnchor;
            this.states = states;
        }

        public void registerToCheck(String id, @NotNull Supplier<Object> stateSupplier) {
            this.states.put(id, stateSupplier);
        }

        /**
         * @return true if changed; false otherwise
         */
        public boolean collectAndCheckChanged() {
            boolean changed = false;

            final Map<String, Supplier<?>> map = new HashMap<>(this.states);
            map.putAll(Map.of(
                    System.currentTimeMillis() + "", this.collision,
                    System.currentTimeMillis() + "", this.items,
                    System.currentTimeMillis() + "", this.position,
                    System.currentTimeMillis() + "", this.viewAnchor
            ));

            for (Map.Entry<String, Supplier<?>> entry : map.entrySet()) {
                final Object oldState = this.prevStates.get(entry.getKey());
                final Object newState = this.prevStates.compute(
                        entry.getKey(),
                        (k, v) -> entry.getValue().get()
                );

                if (!Objects.equals(oldState, newState)) changed = true;
            }

            return changed;
        }

    }

}
