package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;
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

        private final Snapping<CollisionTable> collision;
        private final Snapping<ItemTable> items;
        private final Snapping<Vector2i> position;
        private final Snapping<Vector2i> viewAnchor;
        @Getter(AccessLevel.NONE)
        private final Map<String, Snapping<Object>> states;

        @Builder
        public Snapshot(Supplier<CollisionTable> collision, Supplier<ItemTable> items, Supplier<Vector2i> position, Supplier<Vector2i> viewAnchor, @Singular Map<String, Supplier<Object>> states) {
            this.collision = new Snapping<>(collision);
            this.items = new Snapping<>(items);
            this.position = new Snapping<>(position);
            this.viewAnchor = new Snapping<>(viewAnchor);
            this.states = states.entrySet().stream().reduce(new HashMap<>(), (i, e) -> {
                i.put(e.getKey(), new Snapping<>(e.getValue()));
                return i;
            }, (a, b) -> {
                a.putAll(b);
                return a;
            });
        }

        public void registerToCheck(String id, @NotNull Supplier<Object> stateSupplier) {
            this.states.put(id, new Snapping<>(stateSupplier));
        }

        /**
         * @return true if changed; false otherwise
         */
        public boolean collectAndCheckChanged() {
            boolean changed = false;

            final List<Snapping<?>> snappings = new ArrayList<>(this.states.values());
            snappings.addAll(Arrays.asList(
                    this.collision,
                    this.items,
                    this.position,
                    this.viewAnchor
            ));

            for (Snapping<?> snapping : snappings) {
                final Object oldState = snapping.getVal();
                final Object newState = snapping.getSupplier().get();

                if (!Objects.equals(oldState, newState)) changed = true;
            }

            return changed;
        }

        @Data
        public static class Snapping<T> {

            private final Supplier<T> supplier;
            private T val = null;

        }

    }

}
