package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

/**
 * For things such as page changing
 */
@ToString(callSuper = true)
public abstract class FloatingComponent extends Component {

    public FloatingComponent(@NotNull Vector2i position) {
        super(new CollisionTable(), new ItemTable(), position);
        setFloating(true);
        setZIndex(-1);
    }

}
