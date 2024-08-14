package comfortable_andy.brew.menu.componenets.defaults;

import comfortable_andy.brew.menu.actions.MenuAction;
import comfortable_andy.brew.menu.componenets.Renderer;
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
public class ScrollComponent extends AnchorShiftingComponent {

    @Range(from = 1, to = Integer.MAX_VALUE)
    @Setter
    private Integer page;
    private final IntegerRange range;
    private final int moveAmount;
    private final boolean isHorizontal;
    private final BiConsumer<@Nullable Integer, @Nullable Integer> callback;

    @Builder
    public ScrollComponent(Vector2i pos, @Nullable IntegerRange range, boolean isHorizontal, int moveAmount, ItemStack back, ItemStack forward, BiConsumer<@Nullable Integer, @Nullable Integer> callback) {
        super(pos, true);
        this.range = range;
        this.moveAmount = moveAmount;
        this.isHorizontal = isHorizontal;
        this.callback = callback;
        if (this.range == null) this.page = null;
        else this.page = this.range.getMinimum();
        setZIndex(1);
        final Vector2i backPos = isHorizontal ? Renderer.Direction.LEFT.get() : Renderer.Direction.UP.get();
        final Vector2i forwardPos = isHorizontal ? Renderer.Direction.RIGHT.get() : Renderer.Direction.DOWN.get();
        getItemTable().set(backPos.x, backPos.y, back);
        getItemTable().set(forwardPos.x, forwardPos.y, forward);
        getCollisionTable().set(backPos.x, backPos.y);
        getCollisionTable().set(forwardPos.x, forwardPos.y);
        getActions().put((e, rel) -> {
            shift(rel.equals(forwardPos));
            return true;
        }, MenuAction.ActionCriteria.builder().type(MenuAction.ActionType.LEFT).build());
    }

    @Override
    public void shift(boolean forward) {
        final int oldPage = this.page;
        final int mod = forward ? 1 : -1;
        if (this.range != null) {
            if (this.range.contains(this.page + mod)) {
                this.page += mod;
            } else return;
        }
        getRenderedBy().shiftView(
                new Vector2i(
                        this.isHorizontal ? this.moveAmount : 0,
                        this.isHorizontal ? 0 : this.moveAmount
                ).mul(mod)
        );
        this.callback.accept(oldPage, this.page);
    }

}
