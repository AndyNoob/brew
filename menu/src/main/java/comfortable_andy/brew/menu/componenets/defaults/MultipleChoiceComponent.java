package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.Menu;
import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.Getter;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public abstract class MultipleChoiceComponent extends InventorySwitchingComponent<Inventory> {

    private static final int MAX_CHOICES_PAGE = 9 * 5;
    private final Inventory choiceInv;
    private final Menu menu;

    public MultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, String> callback) {
        super(plugin, position);
        final int choiceSize = choices().size();
        int rows = NumberConversions.ceil(choiceSize / 9f);
        this.menu = new Menu(
                "" + hashCode(),
                displayName(),
                "auto created by " + this.getClass().getSimpleName()
        );
        final int pageCount = NumberConversions.ceil((choiceSize * 1f) / MAX_CHOICES_PAGE);
        final ScrollComponent component = makeScrollComponent(pageCount);
        if (component != null) {
            rows += 1;
            menu.addComponent(component);
        }
        final int invSize = Math.max(54, rows * 9);
        this.choiceInv = Bukkit.createInventory(null, invSize);
        final Renderer renderer = this.menu.getRenderer();
        renderer.setInventory(this.choiceInv);
        if (component != null)
            component.getPosition().set(renderer.translateToVec(this.choiceInv, invSize - 4 - 1));
        generateChoiceButtons();
    }

    protected abstract String displayName();

    protected abstract Map<String, Supplier<ItemStack>> choices();

    protected abstract void generateChoiceButtons();

    @Nullable
    protected ScrollComponent makeScrollComponent(int pages) {
        return ScrollComponent.builder()
                .pos(new Vector2i())
                .range(IntegerRange.of(1, pages))
                .isHorizontal(true)
                .moveAmount(9)
                .forward(new ItemStack(Material.ARROW))
                .back(new ItemStack(Material.ARROW))
                .callback((a, b) -> {})
                .build();
    }

    @Override
    protected Inventory open(HumanEntity entity) {
        entity.openInventory(this.choiceInv);
        return this.choiceInv;
    }

}
