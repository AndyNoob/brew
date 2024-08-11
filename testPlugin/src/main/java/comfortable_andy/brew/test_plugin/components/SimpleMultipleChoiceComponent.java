package comfortable_andy.brew.test_plugin.components;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.defaults.MultipleChoiceComponent;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SimpleMultipleChoiceComponent extends MultipleChoiceComponent {

    public SimpleMultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, ItemStack item, BiConsumer<HumanEntity, String> callback) {
        super(plugin, position, callback);
        getItemTable().set(0, 0, item);
        getCollisionTable().set(0, 0);
        getActions().put((h, r) -> {
            open(h);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

    @Override
    protected String displayName() {
        return "yo mama";
    }

    @Override
    protected Map<String, Supplier<ItemStack>> choices() {
        return Map.of("thing", () -> new ItemStack(Material.BLUE_BANNER), "other thing", () -> new ItemStack(Material.RED_BANNER));
    }
}
