package comfortable_andy.brew.menu.componenets.defaults;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

public class SimpleOutInletComponent extends SimpleInletComponent implements ItemOutletComponent {

    public SimpleOutInletComponent(@NotNull Vector2i position, @Range(from = 1, to = Long.MAX_VALUE) int width, @Range(from = 1, to = Long.MAX_VALUE) int height) {
        super(position, width, height);
    }

    @Override
    public @Nullable ItemStack tryTakeOutItems(HumanEntity who, @NotNull Vector2i relPos) {
        final int index = from(relPos);
        final ItemStack content = contents[index];
        contents[index] = null;
        updateContents();
        return content;
    }
}
