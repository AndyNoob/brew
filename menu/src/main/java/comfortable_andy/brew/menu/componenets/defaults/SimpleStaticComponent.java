package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import lombok.Builder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.function.Supplier;

public class SimpleStaticComponent extends StaticComponent {

    private final Supplier<ItemStack> item;

    @Builder
    public SimpleStaticComponent(@NotNull Vector2i position, Supplier<ItemStack> item) {
        super(position);
        this.item = item;
        getCollisionTable().set(0, 0);
        update();
        getActions().put((h, rel) -> {
            update();
            return true;
        }, new MenuAction.ActionCriteria(MenuAction.ActionType.ANY, null));
    }

    public void update() {
        getItemTable().set(0, 0, this.item.get());
    }

}
