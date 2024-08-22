package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.List;

public abstract class ItemInletComponent extends StaticComponent {

    public ItemInletComponent(@NotNull Vector2i position) {
        super(position);
    }

    public abstract boolean tryTakeItems(List<ItemStack> items);

}
