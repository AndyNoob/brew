package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public abstract class ItemInletComponent extends StaticComponent {

    public ItemInletComponent(@NotNull Vector2i position) {
        super(position);
    }

    /**
     * @return remainder, exact same item if failed
     */
    public abstract ItemStack tryTakeItems(HumanEntity who, ItemStack item);

}
