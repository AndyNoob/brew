package comfortable_andy.brew.menu.componenets.tables;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemTable extends Table<ItemStack> {

    @Override
    protected @Nullable ItemStack defaultValue() {
        return new ItemStack(Material.AIR);
    }
}
