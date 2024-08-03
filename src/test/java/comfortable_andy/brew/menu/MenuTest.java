package comfortable_andy.brew.menu;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.FloatingComponent;
import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.StaticComponent;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector2i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MenuTest {

    public static final int CENTER_SLOT = 31;

    private Menu menu;
    private final FakeChestInv inv = new FakeChestInv();

    @BeforeEach
    public void initMenu() {
        menu = new Menu("thing", "yo", "to");
    }

    @Test
    public void testOriginFloatingClick() {
        menu.addComponent(new ScrollingComponent(new Vector2i()));
        assertThrows(RuntimeException.class, () -> menu.handleClick(inv, MenuAction.ActionType.LEFT, null, CENTER_SLOT, null));
    }

    @Test
    public void testOffsetFloatingClick() {
        menu.addComponent(new ScrollingComponent(new Vector2i(1, 0)));
        assertThrows(RuntimeException.class, () -> menu.handleClick(inv, MenuAction.ActionType.LEFT, null, CENTER_SLOT + 1, null));
    }

    @Test
    public void testLeftShiftedAnchorClick() {
        menu.addComponent(new ButtonComponent(new Vector2i()));
        menu.getRenderer().shiftView(new Vector2i(1, 0));
        assertThrows(RuntimeException.class, () -> menu.handleClick(inv, MenuAction.ActionType.LEFT, null, CENTER_SLOT - 1, null));
    }

    @Test
    public void testUnShiftedOffsetClick() {
        menu.addComponent(new ButtonComponent(new Vector2i(1, 0)));
        assertThrows(RuntimeException.class, () -> menu.handleClick(inv, MenuAction.ActionType.LEFT, null, CENTER_SLOT + 1, null));
    }

    @Test
    public void testRender() {
        ButtonComponent component = new ButtonComponent(new Vector2i(0, 0));
        menu.addComponent(component);
        Renderer renderer = menu.getRenderer();
        renderer.setInventory(inv);
        renderer.render();
        assertEquals(Material.DIAMOND, Objects.requireNonNull(inv.getItem(CENTER_SLOT)).getType());
        component.getPosition().x += 1000;
        assertDoesNotThrow(renderer::render);
    }

    public static class ScrollingComponent extends FloatingComponent {

        public ScrollingComponent(Vector2i pos) {
            super(pos);
            getCollisionTable().set(0, 0, true);
            getActions().put((e, p) -> {throw new RuntimeException("yes");}, new MenuAction.ActionCriteria(MenuAction.ActionType.LEFT, null));
        }

    }

    public static class ButtonComponent extends StaticComponent {

        public ButtonComponent(Vector2i pos) {
            super(pos);
            getCollisionTable().set(0, 0, true);
            getItemTable().set(0, 0, new ItemStack(Material.DIAMOND));
            getActions().put((e, p) -> {throw new RuntimeException("yes");}, new MenuAction.ActionCriteria(MenuAction.ActionType.LEFT, null));
        }

    }

}