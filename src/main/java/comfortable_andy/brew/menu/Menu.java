package comfortable_andy.brew.menu;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import comfortable_andy.brew.menu.componenets.MenuComponent;
import comfortable_andy.brew.menu.componenets.Renderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@Data
public abstract class Menu {

    private final String id;
    @Getter(AccessLevel.NONE)
    private final String displayName;
    private final String description;
    private final Renderer renderer = new Renderer();

    @NotNull
    public Component getDisplayName() {
        return MiniMessage.miniMessage().deserialize(displayName);
    }

    @NotNull
    public Component getDescription() {
        return MiniMessage.miniMessage().deserialize(this.description);
    }

    public void addComponent(@NotNull MenuComponent component) {
        this.renderer.insertComponent(component);
    }

    public void removeComponent(@NotNull MenuComponent component) {
        this.renderer.removeComponent(component);
    }

}
