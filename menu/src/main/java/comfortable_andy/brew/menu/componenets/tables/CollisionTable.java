package comfortable_andy.brew.menu.componenets.tables;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString(callSuper = true)
public class CollisionTable extends Table<Boolean, CollisionTable> {

    @Override
    protected @Nullable Boolean defaultValue() {
        return false;
    }

    @Override
    protected Boolean clone(Boolean bool) {
        return bool;
    }

    public void set(int x, int y) {
        super.set(x, y, true);
    }
}
