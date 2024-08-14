package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.MenuTest;
import comfortable_andy.brew.menu.TestWithMenu;
import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Renderer;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScrollComponentTest extends TestWithMenu {

    @SuppressWarnings("DataFlowIssue")
    @Test
    public void testScroll() {
        Renderer renderer = menu.getRenderer();
        renderer.setInventory(inv);
        ScrollComponent component = ScrollComponent.builder()
                .pos(new Vector2i(4, 0))
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
        renderer.render();
        assertThrows(IllegalStateException.class, () -> menu.handleClick(inv, MenuAction.ActionType.LEFT, null, MenuTest.CENTER_SLOT + 4 + 9, null));
        assertEquals(new Vector2i(0, 10), renderer.getViewAnchor());
        assertEquals(Material.DIAMOND, inv.getItem(MenuTest.CENTER_SLOT + 4 - 9).getType());
    }

}