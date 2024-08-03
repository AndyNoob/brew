package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;

import java.util.function.Consumer;

public class SimpleButtonComponent extends StaticComponent {

    public SimpleButtonComponent(Vector2i pos, int width, int height, ItemStack item, Consumer<HumanEntity> callback) {
        super(pos);
        final int halfWidth = width / 2;
        final int halfHeight = height / 2;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                getCollisionTable().set(w - halfWidth, h - halfHeight);
                getItemTable().set(w - halfWidth, h - halfHeight, item.clone());
            }
        }
        getActions().put((h, rel) -> {
            callback.accept(h);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

}
