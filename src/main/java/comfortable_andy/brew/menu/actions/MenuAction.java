package comfortable_andy.brew.menu.actions;

import lombok.Builder;
import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public interface MenuAction {

    boolean tryRun(HumanEntity entity, Vector2i relativePosition);

    @Builder
    public record ActionCriteria(@NotNull ActionType type, @Nullable ActionModifier modifier) {
    }

    public enum ActionType {
        LEFT,
        RIGHT,
        MIDDLE
    }

    public enum ActionModifier {
        SHIFT,
        DOUBLE
    }

}
