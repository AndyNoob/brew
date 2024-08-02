package comfortable_andy.brew.menu.componenets;

import lombok.Getter;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

@Getter
public abstract class StaticComponent extends Component {

    public StaticComponent(@NotNull CollisionTable collisionTable, @NotNull ItemTable itemTable, @NotNull Vector2i position) {
        super(collisionTable, itemTable, position);
    }

}
