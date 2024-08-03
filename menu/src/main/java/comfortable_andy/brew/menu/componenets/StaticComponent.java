package comfortable_andy.brew.menu.componenets;

import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

@ToString(callSuper = true)
public abstract class StaticComponent extends Component {

    public StaticComponent(@NotNull Vector2i position) {
        super(new CollisionTable(), new ItemTable(), position);
    }

}
