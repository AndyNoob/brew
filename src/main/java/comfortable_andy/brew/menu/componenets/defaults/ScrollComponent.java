package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.FloatingComponent;
import comfortable_andy.brew.menu.componenets.tables.CollisionTable;
import comfortable_andy.brew.menu.componenets.tables.ItemTable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.IntegerRange;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.function.BiConsumer;

@Getter
@ToString
public class ScrollComponent extends FloatingComponent {

    @Range(from = 1, to = Integer.MAX_VALUE)
    @Setter
    private Integer page;

    @Builder
    public ScrollComponent(Vector2i pos, boolean simulatePagination, boolean isHorizontal, int moveAmount, ItemStack back, ItemStack forward, IntegerRange range, BiConsumer<@Nullable Integer, @Nullable Integer> callback) {
        super(pos);
        if (simulatePagination) page = range.getMinimum();
        else page = null;
        setZIndex(1);
        int backX = isHorizontal ? -1 : 0;
        int backY = isHorizontal ? 0 : 1;
        int forwardX = isHorizontal ? 1 : 0;
        int forwardY = isHorizontal ? 0 : -1;
        getItemTable().set(backX, backY, back);
        getItemTable().set(forwardX, forwardY, forward);
        getCollisionTable().set(backX, backY);
        getCollisionTable().set(forwardX, forwardY);
        getActions().put((e, rel) -> {
            boolean isBack = rel.equals(backX, backY);
            final int oldPage = page;
            if (simulatePagination) {
                final int mod = isBack ? -1 : 1;
                if (range.contains(page + mod)) {
                    page += mod;
                } else return true;
            }
            final int amount = isBack ? -moveAmount : moveAmount;
            getRenderedBy().shiftView(new Vector2i(isHorizontal ? amount : 0, isHorizontal ? 0 : amount));
            callback.accept(oldPage, page);
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

}
