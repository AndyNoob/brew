package comfortable_andy.brew.menu.componenets.tables;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CollisionTable extends Table<Boolean> {

    @Override
    protected @Nullable Boolean defaultValue() {
        return false;
    }

}
