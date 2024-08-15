package comfortable_andy.brew.menu.componenets.tables;

import lombok.ToString;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@ToString(callSuper = true)
public class ItemTable extends Table<ItemStack, ItemTable> {

    @Override
    protected @Nullable ItemStack defaultValue() {
        return new ItemStack(Material.AIR);
    }

    @Override
    protected ItemStack clone(ItemStack stack) {
        return stack == null ? null : stack.clone();
    }
}
