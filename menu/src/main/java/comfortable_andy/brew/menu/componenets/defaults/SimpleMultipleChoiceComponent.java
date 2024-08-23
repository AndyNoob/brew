package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SimpleMultipleChoiceComponent extends MultipleChoiceComponent {

    @Builder
    public SimpleMultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, ItemStack item, BiConsumer<HumanEntity, String> callback, LinkedHashMap<String, Supplier<ItemStack>> choices) {
        super(plugin, position, callback, choices);
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

    @NotNull
    public static LinkedHashMap<String, Supplier<ItemStack>> randomChoices() {
        final List<Material> itemMats = new ArrayList<>(Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(material -> !material.isLegacy())
                .toList());
        Collections.shuffle(itemMats);
        return new LinkedHashMap<>() {{
            for (int i = 0; i < 60; i++) {
                int finalI = i;
                put(i + "", () -> new ItemStack(itemMats.get(finalI)));
            }
        }};
    }
}