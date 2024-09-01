package comfortable_andy.brew.test_plugin.components;

import comfortable_andy.brew.menu.componenets.defaults.ItemInletComponent;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

public class SimpleInletComponent extends ItemInletComponent {

    private final ItemStack[] contents;
    private final int width, height;
    private final int halfWidth, halfHeight;

    public SimpleInletComponent(@NotNull Vector2i position, @Range(from = 1, to = Long.MAX_VALUE) int width, @Range(from = 1, to = Long.MAX_VALUE) int height) {
        super(position);

        contents = new ItemStack[width * height];
        this.width = width;
        halfWidth = width / 2;
        this.height = height;
        halfHeight = height / 2;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                getCollisionTable().set(x - halfWidth, y - halfHeight);
            }
        }
    }

    public void updateContents() {
        for (int i = 0; i < contents.length; i++) {
            final int x = i % width - halfWidth;
            final int y = i / width - halfHeight;
            getItemTable().set(x, y, contents[i]);
        }
    }

    @Override
    public ItemStack tryTakeItems(HumanEntity who, ItemStack item) {
        for (int i = 0; i < contents.length; i++) {
            if (item.isEmpty()) return item;
            ItemStack cur = contents[i];
            if (cur == null || cur.isEmpty()) {
                contents[i] = item;
                return new ItemStack(Material.AIR);
            }
            if (cur.getAmount() >= item.getMaxStackSize()) continue;
            if (!cur.isSimilar(item)) continue;
            final int total = item.getAmount() + cur.getAmount();
            final int actualNew = Math.max(cur.getMaxStackSize(), total);
            cur.setAmount(actualNew);
            final int remainder = total - actualNew;
            item.setAmount(remainder);
        }
        updateContents();
        return item;
    }

}
