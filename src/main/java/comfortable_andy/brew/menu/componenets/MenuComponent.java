package comfortable_andy.brew.menu.componenets;

import lombok.Getter;
import comfortable_andy.brew.menu.Menu;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

@Getter
public abstract class MenuComponent extends Component {

    private @Nullable Menu assignedTo;

    public MenuComponent(@NotNull CollisionTable collisionTable, @NotNull ItemTable itemTable, @NotNull Vector2i position) {
        super(collisionTable, itemTable, position);
    }

    public void assignTo(@Nullable Menu app) {
        if (this.assignedTo != null) this.assignedTo.removeComponent(this);
        this.assignedTo = app;
        if (this.assignedTo != null) app.addComponent(this);
    }

}
