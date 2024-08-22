package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.function.BiConsumer;

@Getter
public class SimpleTextFieldComponent extends TextFieldComponent {

    @Setter
    private ItemStack item;
    private final BiConsumer<HumanEntity, String> callback;

    @Builder
    public SimpleTextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, ItemStack item, BiConsumer<HumanEntity, String> callback) {
        super(plugin, position);
        this.callback = callback;
        this.item = item;
        getCollisionTable().set(0, 0);
        getItemTable().set(0, 0, item);
        getActions().put((h, rel) -> {
            open(h);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

    @Override
    protected void onEnterText(HumanEntity entity, String str) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> callback.accept(entity, str), 1);
    }

}
