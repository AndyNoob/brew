package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.Menu;
import comfortable_andy.brew.menu.componenets.Renderer;
import lombok.Getter;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Getter
public abstract class MultipleChoiceComponent extends InventorySwitchingComponent<Inventory> {

    private static final int MAX_CHOICES_PAGE = 9 * 5;
    protected final Inventory choiceInv;
    protected final Menu menu;
    protected final BiConsumer<HumanEntity, Set<String>> callback;
    protected final LinkedHashMap<String, Supplier<ItemStack>> choices;
    protected final LinkedHashMap<String, SimpleButtonComponent> choiceButtons = new LinkedHashMap<>();
    protected final String displayName;
    protected int rows;
    protected int choiceLimit;
    protected final Set<String> chosen = new HashSet<>();

    public MultipleChoiceComponent(@NotNull JavaPlugin plugin, @NotNull Vector2i position, BiConsumer<HumanEntity, Set<String>> callback, LinkedHashMap<String, Supplier<ItemStack>> choices, String displayName, @Nullable @Range(from = 1, to = 6) Integer additionalRows, @Range(from = 1, to = Integer.MAX_VALUE) int choiceLimit) {
        super(plugin, position);
        this.choiceLimit = choiceLimit;
        this.choices = choices;
        this.callback = callback;
        final int choiceSize = choices.size();
        this.rows = NumberConversions.ceil(choiceSize / 9f) + (additionalRows == null ? 0 : additionalRows);
        this.displayName = displayName;
        this.menu = new Menu(
                "" + hashCode(),
                this.displayName,
                "auto created by " + this.getClass().getSimpleName()
        );
        final int pageCount = NumberConversions.ceil((choiceSize * 1f) / MAX_CHOICES_PAGE);
        final ScrollComponent component;
        if (pageCount > 1) {
            component = makeScrollComponent(pageCount);
            if (component != null) {
                this.rows += 1;
                menu.addComponent(component);
            }
        } else component = null;
        this.rows = Math.min(54 / 9, this.rows);
        final int invSize = Math.min(54, this.rows * 9);
        this.choiceInv = Bukkit.createInventory(null, invSize);
        final Renderer renderer = this.menu.getRenderer();
        renderer.setInventory(this.choiceInv);
        if (component != null)
            component.getPosition().set(renderer.translateToScreenSpaceVec(this.choiceInv, invSize - 4 - 1));
        generateChoiceButtons();
        renderer.render();
    }

    protected void newChoice(String choice, boolean removed) {
    }

    protected void generateChoiceButtons() {
        int i = 0;

        for (Map.Entry<String, Supplier<ItemStack>> entry : this.choices.entrySet()) {
            final int horizontalOffset = i / MAX_CHOICES_PAGE;
            final int x = i % 9 - 4 + 9 * horizontalOffset;
            final int y = Renderer.getInventoryCenterRowColumn(rows).y - (i % MAX_CHOICES_PAGE) / 9;
            Vector2i pos = new Vector2i(x, y);
            ItemStack item = entry.getValue().get();
            AtomicBoolean selected = new AtomicBoolean(false);
            SimpleButtonComponent component = new SimpleButtonComponent(
                    pos,
                    1,
                    1,
                    item,
                    (h, b) -> {
                        boolean isChosen = !selected.get();
                        if (isChosen) {
                            if (choiceLimit == 1) {
                                for (String s : chosen) { // unfortunately this is a set
                                    SimpleButtonComponent button = choiceButtons.get(s);
                                    button.setItem(changeItemVisual(
                                            button.getItemTable().get(0, 0).clone(),
                                            false
                                    ));
                                }
                                chosen.clear();
                            }
                            if (chosen.size() + 1 > choiceLimit)
                                return;
                            chosen.add(entry.getKey());
                            newChoice(entry.getKey(), false);
                        } else if (choiceLimit != 1) {
                            // user selects the same option
                            b.setItem(changeItemVisual(item.clone(), false));
                            chosen.remove(entry.getKey());
                            newChoice(entry.getKey(), true);
                        }
                        selected.set(isChosen);
                        this.callback.accept(h, chosen);
                        if (choiceLimit == 1) reopenOriginal(h);
                        b.setItem(changeItemVisual(item.clone(), isChosen));
                    }
            );
            this.choiceButtons.put(entry.getKey(), component);
            this.menu.addComponent(component);
            i++;
        }
    }

    protected abstract ItemStack changeItemVisual(ItemStack item, boolean selected);

    @Override
    protected void handleClick(InventoryClickEvent e) {
        this.menu.handleClick(e);
    }

    @Override
    protected void handleDrag(InventoryDragEvent e) {
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
                .callback((a, b) -> {
                })
                .hideItemIfPaginating(true)
                .build();
    }

    @Override
    protected Inventory getInventoryFor(HumanEntity entity) {
        return this.choiceInv;
    }

}
