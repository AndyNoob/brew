package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

public class SimpleInletComponent extends StaticComponent implements ItemInletComponent {

    protected final ItemStack[] contents;
    private final int width;
    protected final int halfWidth, halfHeight;

    public SimpleInletComponent(@NotNull Vector2i position, @Range(from = 1, to = Long.MAX_VALUE) int width, @Range(from = 1, to = Long.MAX_VALUE) int height) {
        super(position);

        contents = new ItemStack[width * height];
        this.width = width;
        halfWidth = width / 2;
        halfHeight = height / 2;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                getCollisionTable().set(x - halfWidth, y - halfHeight);
            }
        }
    }

    public void updateContents() {
        for (int i = 0; i < contents.length; i++) {
            Vector2i xy = from(i);
            getItemTable().set(xy.x, xy.y, contents[i]);
        }
    }

    @Override
    public ItemStack tryTakeInItems(HumanEntity who, @Nullable Vector2i relPos, ItemStack item) {
        if (relPos == null) {
            for (int i = 0; i < contents.length; i++) {
                if (item.isEmpty()) {
                    updateContents();
                    return item;
                }
                ItemStack cur = contents[i];
                if (cur == null || cur.isEmpty()) {
                    contents[i] = item;
                    updateContents();
                    return new ItemStack(Material.AIR);
                }
                if (cur.getAmount() >= item.getMaxStackSize()) continue;
                if (!cur.isSimilar(item)) continue;
                final int total = item.getAmount() + cur.getAmount();
                final int actualNew = Math.min(cur.getMaxStackSize(), total);
                cur.setAmount(actualNew);
                final int remainder = total - actualNew;
                item.setAmount(remainder);
            }
            updateContents();
            return item;
        } else {
            final int index = from(relPos);
            // either merge or swap
            final ItemStack cur = contents[index];
            if (cur == null) {
                contents[index] = item;
                updateContents();
                return new ItemStack(Material.AIR);
            } else if (cur.isSimilar(item)) {
                if (cur.getAmount() >= cur.getMaxStackSize()) {
                    updateContents();
                    return null;
                }
                final int total = item.getAmount() + cur.getAmount();
                final int actualNew = Math.min(cur.getMaxStackSize(), total);
                cur.setAmount(actualNew);
                final int remainder = total - actualNew;
                item.setAmount(remainder);
                updateContents();
                return item;
            } else {
                contents[index] = item;
                updateContents();
                return cur;
            }
        }
    }

    protected int from(Vector2i xy) {
        return (Renderer.Direction.UP.get().y * xy.y + halfHeight) * width + (Renderer.Direction.RIGHT.get().x * xy.x + halfWidth);
    }

    protected Vector2i from(int i) {
        return new Vector2i(Renderer.Direction.RIGHT.get().x * i % width - halfWidth, Renderer.Direction.UP.get().y * (i / width - halfHeight));
    }

}
