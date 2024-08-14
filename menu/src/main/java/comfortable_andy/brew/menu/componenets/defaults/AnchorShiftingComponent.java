package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Component;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public abstract class AnchorShiftingComponent extends Component {

    public AnchorShiftingComponent(@NotNull Vector2i position, boolean floating) {
        super(new CollisionTable(), new ItemTable(), position);
        setFloating(floating);
    }

    public abstract void shift(boolean forward);

}
