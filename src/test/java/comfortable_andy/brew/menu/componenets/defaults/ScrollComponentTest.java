package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.FakeChestInv;
import comfortable_andy.brew.menu.Menu;
import comfortable_andy.brew.menu.MenuTest;
import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Renderer;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScrollComponentTest {

    @Test
    public void testScroll() {
        final Menu menu = new Menu("bruh", "bruh", "bru??????");
        Renderer renderer = menu.getRenderer();
        FakeChestInv inventory = new FakeChestInv();
        renderer.setInventory(inventory);
        ScrollComponent component = ScrollComponent.builder()
                .pos(new Vector2i(4, 0))
                .simulatePagination(true)
                .moveAmount(10)
                .isHorizontal(false)
                .range(IntegerRange.of(-100, 100))
                .callback((oldPage, newPage) -> {
                    if (newPage != null && newPage == -99) {
                        throw new IllegalStateException("yessir");
                    }
                })
                .forward(new ItemStack(Material.DIAMOND))
                .back(new ItemStack(Material.DIAMOND))
                .build();
        menu.addComponent(component);
        assertThrows(IllegalStateException.class, () -> menu.handleClick(inventory, MenuAction.ActionType.LEFT, null, MenuTest.CENTER_SLOT + 4 - 9, null));
        assertEquals(new Vector2i(0, 10), renderer.getViewAnchor());
    }

}