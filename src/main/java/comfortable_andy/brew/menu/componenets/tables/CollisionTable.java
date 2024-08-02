package comfortable_andy.brew.menu.componenets.tables;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CollisionTable extends Table<Boolean> {

    @Override
    public boolean isOutside(int x, int y) {
        return super.isOutside(x, y) || !Objects.equals(super.get(x, y), true);
    }

    @Override
    protected @Nullable Boolean defaultValue() {
        return false;
    }

}
