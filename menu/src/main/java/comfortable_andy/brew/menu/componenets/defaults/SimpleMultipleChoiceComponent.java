package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Will not re-render automatically
 * @see comfortable_andy.brew.menu.componenets.Renderer#tryRender(boolean)
 */
public class SimpleMultipleChoiceComponent extends MultipleChoiceComponent {

    private final Function<@NotNull Set<String>, ItemStack> item;

    public SimpleMultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, Function<@NotNull String, ItemStack> item, BiConsumer<HumanEntity, Set<String>> callback, LinkedHashMap<String, Supplier<ItemStack>> choices, String displayName, @Nullable @Range(from = 1, to = 6) Integer additionalRows, @Range(from = 1, to = Integer.MAX_VALUE) int choiceLimit) {
        this(plugin, position, (Set<String> s) -> item.apply(s.stream().findFirst().orElse("")), callback, choices, displayName, additionalRows, choiceLimit, null);
    }

    @Builder
    public SimpleMultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, Function<@NotNull Set<String>, ItemStack> item, BiConsumer<HumanEntity, Set<String>> callback, LinkedHashMap<String, Supplier<ItemStack>> choices, String displayName, @Nullable @Range(from = 1, to = 6) Integer additionalRows, @Range(from = 1, to = Integer.MAX_VALUE) int choiceLimit, @SuppressWarnings("unused") @Nullable Object anything) {
        super(plugin, position, callback, choices, displayName, additionalRows, choiceLimit);
        this.item = item;
        getItemTable().set(0, 0, item.apply(new HashSet<>()));
        getCollisionTable().set(0, 0);
        getActions().put((h, r) -> {
            open(h);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

    @SuppressWarnings("unused")
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
    protected void newChoice(String choice, boolean removed) {
        getItemTable().set(0, 0, this.item.apply(chosen));
    }

    @Override
    protected ItemStack changeItemVisual(ItemStack item, boolean selected) {
        item.editMeta(meta -> {
            if (meta.hasEnchants()) return;
            meta.setEnchantmentGlintOverride(selected);
        });
        return item;
    }

}
