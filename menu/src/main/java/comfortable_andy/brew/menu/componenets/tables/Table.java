package comfortable_andy.brew.menu.componenets.tables;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.IntegerRange;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class Table<T, Self extends Table<T, Self>> implements Iterable<Table.Item<T>>, Cloneable {

    private final Map<Vector2i, T> table = new HashMap<>();

    @Nullable
    protected T defaultValue() {
        return null;
    }

    @Contract("!null -> !null; null -> null")
    protected abstract T clone(T t);

    public T get(@Range(from = Long.MIN_VALUE, to = Long.MAX_VALUE) int x, int y) {
        return this.table.getOrDefault(new Vector2i(x, y), defaultValue());
    }

    /**
     * @return the previous value, if it exists
     */
    @Nullable
    public T set(int x, int y, @Nullable T value) {
        return this.table.put(new Vector2i(x, y), value);
    }

    public void set(IntegerRange xRange, IntegerRange yRange, @Nullable T value) {
        IntStream.range(xRange.getMinimum(), xRange.getMaximum() + 1).forEachOrdered(x -> {
            IntStream.range(yRange.getMinimum(), yRange.getMaximum() + 1)
                    .forEachOrdered(y -> set(x, y, value));
        });
    }

    @NotNull
    @Override
    public Iterator<Item<T>> iterator() {
        return this.table.entrySet().stream().map(e -> new Item<>(e.getKey().x, e.getKey().y, e.getValue())).iterator();
    }

    public int getWidth() {
        return this.table.keySet().stream().mapToInt(Vector2i::x).map(v -> v == 0 ? 1 : 0).max().orElse(0);
    }

    public int getHeight() {
        return this.table.keySet().stream().mapToInt(Vector2i::x).map(v -> v == 0 ? 1 : 0).max().orElse(0);
    }

    public boolean isOutside(int x, int y) {
        return x + 1 > getWidth() || y + 1 > getHeight();
    }

    public boolean isEmpty() {
        return this.table.isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Self clone() {
        try {
            final Self clone = (Self) super.clone();
            ((Table<T, Self>) clone).table.replaceAll((v, t) -> clone(t));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public record Item<T>(int x, int y, @Nullable T value) {
    }

}
