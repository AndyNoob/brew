package comfortable_andy.brew.menu;

import comfortable_andy.brew.menu.actions.MenuAction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.ToString;
import lombok.Value;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Menu extends Displaying {

    Renderer renderer = new Renderer();

    public void addComponent(@NotNull comfortable_andy.brew.menu.componenets.Component component) {
        this.renderer.insertComponent(component);
    }

    public void removeComponent(@NotNull comfortable_andy.brew.menu.componenets.Component component) {
        this.renderer.removeComponent(component);
    }

}
