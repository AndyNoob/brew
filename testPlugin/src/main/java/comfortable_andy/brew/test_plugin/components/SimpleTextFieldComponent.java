package comfortable_andy.brew.test_plugin.components;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.defaults.TextFieldComponent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Getter
public class SimpleTextFieldComponent extends TextFieldComponent {

    @Setter
    private ItemStack item;
    private final JavaPlugin plugin;
    private final BiConsumer<HumanEntity, String> callback;
    private final Map<HumanEntity, Inventory> reopens = new HashMap<>();

    public SimpleTextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, ItemStack item, BiConsumer<HumanEntity, String> callback) {
        super(plugin, position);
        this.plugin = plugin;
        this.callback = callback;
        this.item = item;
        getCollisionTable().set(0, 0);
        getItemTable().set(0, 0, item);
        getActions().put((h, rel) -> {
            Inventory cur = h.getOpenInventory().getTopInventory();
            reopens.put(h, cur);
            Inventory anvil = open(h);
            anvil.setItem(0, new ItemStack(Material.PAPER));
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

    @Override
    protected void onEnterText(HumanEntity entity, String str) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> callback.accept(entity, str), 1);
    }

    @Override
    protected void reopenOriginal(HumanEntity entity) {
        Inventory inventory = reopens.remove(entity);
        if (inventory != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> entity.openInventory(inventory), 1);
        }
    }
}
