package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

/**
 * For things such as page changing
 */
public abstract class SystemComponent extends Component {

    public SystemComponent(@NotNull CollisionTable collisionTable, @NotNull ItemTable itemTable, @NotNull Vector2i position) {
        super(collisionTable, itemTable, position);
        setFloating(true);
        setZIndex(-1);
    }

}
