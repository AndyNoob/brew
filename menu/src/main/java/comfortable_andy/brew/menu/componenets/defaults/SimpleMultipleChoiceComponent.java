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
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleMultipleChoiceComponent extends MultipleChoiceComponent {

    private final Function<@NotNull String, ItemStack> item;

    @Builder
    public SimpleMultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, Function<@NotNull String, ItemStack> item, BiConsumer<HumanEntity, String> callback, LinkedHashMap<String, Supplier<ItemStack>> choices, String displayName) {
        super(plugin, position, callback, choices, displayName);
        this.item = item;
        getItemTable().set(0, 0, item.apply(""));
        getCollisionTable().set(0, 0);
        getActions().put((h, r) -> {
            open(h);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
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

    @Override
    protected void newChoice(String choice) {
        getItemTable().set(0, 0, this.item.apply(choice));
    }

}
