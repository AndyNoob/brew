package comfortable_andy.brew.test_plugin.components;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.defaults.TextFieldComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
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

public class SimpleTextFieldComponent extends TextFieldComponent {

    public SimpleTextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, String> consumer) {
        this(plugin, position, consumer, new HashMap<>());
    }
    public SimpleTextFieldComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, String> consumer, Map<HumanEntity, Inventory> reopens) {
        super(plugin, position, (h, str) -> {
            Inventory inventory = reopens.get(h);
            h.openInventory(inventory);
            consumer.accept(h, str);
        });
        getCollisionTable().set(0, 0);
        final ItemStack item = new ItemStack(Material.PAPER);
        item.editMeta(meta -> meta.displayName(Component.text("<not set>", Style.style(TextDecoration.ITALIC))));
        getItemTable().set(0, 0, item);
        getActions().put((h, rel) -> {
            Inventory cur = h.getOpenInventory().getTopInventory();
            reopens.put(h, cur);
            h.openInventory(super.anvil);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

}
