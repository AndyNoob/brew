package comfortable_andy.brew.test_plugin.components;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.defaults.TextFieldComponent;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
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

    public SimpleTextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, ItemStack item, BiConsumer<HumanEntity, String> consumer) {
        this(plugin, position, item, consumer, new HashMap<>());
    }

    public SimpleTextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, ItemStack item, BiConsumer<HumanEntity, String> consumer, Map<HumanEntity, Inventory> reopens) {
        super(plugin, position, (h, str) -> {
            h.closeInventory(); // the onExit will handle reopens
            Bukkit.getScheduler().runTaskLater(plugin, () -> consumer.accept(h, str), 1);
        }, h -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Inventory inventory = reopens.get(h);
            if (inventory != null) h.openInventory(inventory);
        }, 1));
        getCollisionTable().set(0, 0);
        this.item = item;
        getItemTable().set(0, 0, item);
        getActions().put((h, rel) -> {
            Inventory cur = h.getOpenInventory().getTopInventory();
            reopens.put(h, cur);
            h.openInventory(super.anvil);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

}
