package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.MenuTest;
import comfortable_andy.brew.menu.TestWithMenu;
import comfortable_andy.brew.menu.actions.MenuAction;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleStaticComponentTest extends TestWithMenu {

    @Test
    public void testUpdate() {
        AtomicInteger i = new AtomicInteger(0);
        menu.addComponent(new SimpleStaticComponent(new Vector2i(0, 0), () -> {
            i.incrementAndGet();
            return new ItemStack(Material.WOODEN_AXE);
        }));
        menu.handleClick(inv, MenuAction.ActionType.LEFT, null, MenuTest.CENTER_SLOT, null);
        assertEquals(2, i.get());
    }

}