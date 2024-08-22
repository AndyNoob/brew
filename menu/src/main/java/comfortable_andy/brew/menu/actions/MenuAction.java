package comfortable_andy.brew.menu.actions;

import lombok.Builder;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public interface MenuAction {

    boolean tryRun(HumanEntity entity, Vector2i relativePosition);

    @Builder
    record ActionCriteria(@NotNull ActionType type, @Nullable ActionModifier modifier) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ActionCriteria criteria = (ActionCriteria) o;

            if (type != ActionType.ANY && type != criteria.type) return false;
            return modifier == null || modifier == criteria.modifier;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
            return result;
        }
    }

    enum ActionType {
        LEFT,
        RIGHT,
        MIDDLE,
        ANY
    }

    enum ActionModifier {
        NONE,
        SHIFT,
        DOUBLE
    }

}
