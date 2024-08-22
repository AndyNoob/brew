package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import lombok.Builder;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;

import java.util.function.Consumer;

public class SimpleButtonComponent extends StaticComponent {

    @Builder
    public SimpleButtonComponent(Vector2i pos, int width, int height, ItemStack item, Consumer<HumanEntity> callback) {
        super(pos);
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;
        final IntegerRange xRange = IntegerRange.of(-halfWidth, halfWidth);
        final IntegerRange yRange = IntegerRange.of(-halfHeight, halfHeight);
        getCollisionTable().set(xRange, yRange, true);
        getItemTable().set(xRange, yRange, item.clone());
        getActions().put((h, rel) -> {
            callback.accept(h);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

}
