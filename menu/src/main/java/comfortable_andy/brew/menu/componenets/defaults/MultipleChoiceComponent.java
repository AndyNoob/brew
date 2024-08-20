package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.Menu;
import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.Getter;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public abstract class MultipleChoiceComponent extends InventorySwitchingComponent<Inventory> {

    private static final int MAX_CHOICES_PAGE = 9 * 5;
    protected final Inventory choiceInv;
    protected final Menu menu;
    protected final BiConsumer<HumanEntity, String> callback;
    protected final LinkedHashMap<String, Supplier<ItemStack>> choices;
    protected int rows;

    public MultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, String> callback, LinkedHashMap<String, Supplier<ItemStack>> choices) {
        super(plugin, position);
        this.choices = choices;
        this.callback = callback;
        final int choiceSize = choices.size();
        this.rows = NumberConversions.ceil(choiceSize / 9f);
        this.menu = new Menu(
                "" + hashCode(),
                displayName(),
                "auto created by " + this.getClass().getSimpleName()
        );
        final int pageCount = NumberConversions.ceil((choiceSize * 1f) / MAX_CHOICES_PAGE);
        final ScrollComponent component;
        if (pageCount > 1) {
            component = makeScrollComponent(pageCount);
            if (component != null) {
                rows += 1;
                menu.addComponent(component);
            }
        } else component = null;
        final int invSize = Math.min(54, rows * 9);
        this.choiceInv = Bukkit.createInventory(null, invSize);
        final Renderer renderer = this.menu.getRenderer();
        renderer.setInventory(this.choiceInv);
        if (component != null)
            component.getPosition().set(renderer.translateToScreenSpaceVec(this.choiceInv, invSize - 4 - 1));
        generateChoiceButtons();
        renderer.render();
    }

    protected abstract String displayName();

    protected void generateChoiceButtons() {
        int i = 0;

        for (Map.Entry<String, Supplier<ItemStack>> entry : this.choices.entrySet()) {
            final int horizontalOffset = i / MAX_CHOICES_PAGE;
            final int x = i % 9 - 4 + 9 * horizontalOffset;
            final int y = this.rows / 2 - (i % MAX_CHOICES_PAGE) / 9 - 1;
            Vector2i pos = new Vector2i(x, y);
            this.menu.addComponent(new SimpleButtonComponent(
                    pos,
                    1,
                    1,
                    entry.getValue().get(),
                    h -> {
                        this.callback.accept(h, entry.getKey());
                        reopenOriginal(h);
                    }
            ));
            i++;
        }
    }

    @Override
    protected void handleClick(InventoryClickEvent e) {
        this.menu.handleClick(e);
    }

    @Override
    public void open(HumanEntity entity) {
        super.open(entity);
        this.menu.updateInventoryView(entity.getOpenInventory());
    }

    @Nullable
    protected ScrollComponent makeScrollComponent(int pages) {
        return ScrollComponent.builder()
                .pos(new Vector2i(0, -2))
                .range(IntegerRange.of(1, pages))
                .isHorizontal(true)
                .moveAmount(9)
                .forward(new ItemStack(Material.ARROW))
                .back(new ItemStack(Material.ARROW))
                .callback((a, b) -> {})
                .hideItemIfPaginating(true)
                .build();
    }

    @Override
    protected Inventory getInventoryFor(HumanEntity entity) {
        return this.choiceInv;
    }

}
