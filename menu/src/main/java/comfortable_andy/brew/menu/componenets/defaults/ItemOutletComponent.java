package comfortable_andy.brew.menu.componenets.defaults;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

public interface ItemOutletComponent {

    @Nullable
    ItemStack tryTakeOutItems(HumanEntity who, @NotNull Vector2i relPos);

}
