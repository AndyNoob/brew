package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.MenuTest;
import comfortable_andy.brew.menu.TestWithMenu;
import comfortable_andy.brew.menu.actions.MenuAction;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleButtonComponentTest extends TestWithMenu {

    @Test
    public void testClick() {
        SimpleButtonComponent component = new SimpleButtonComponent(new Vector2i(1, 0), 10, 10, new ItemStack(Material.DIAMOND), h -> {throw new IllegalStateException("yes good");});
        menu.addComponent(component);
        assertThrows(IllegalStateException.class, () -> menu.handleClick(inv, MenuAction.ActionType.LEFT, null, MenuTest.CENTER_SLOT + 9 * 2, null));
    }

}