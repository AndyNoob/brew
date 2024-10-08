package comfortable_andy.brew.menu.componenets.defaults;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public interface ItemInletComponent {

    /**
     * @return remainder, exact same item if failed
     */
    ItemStack tryTakeInItems(HumanEntity who, @Nullable Vector2i relPos, ItemStack item);

}
