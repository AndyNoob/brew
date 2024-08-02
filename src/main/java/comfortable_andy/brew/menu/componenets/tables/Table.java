package comfortable_andy.brew.menu.componenets.tables;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

@RequiredArgsConstructor
@EqualsAndHashCode
public abstract class Table<T> implements Iterable<Table.Item<T>> {

    private final T[][] table;
    @EqualsAndHashCode.Exclude
    private transient int width = -1;
    private transient Class<T> clazz;

    @Nullable
    protected T defaultValue() {
        return null;
    }

    public T get(int x, int y) {
        if (isOutside(x, y)) return null;
        return getLayer(y)[x];
    }

    public T[] getLayer(int y) {
        if (y + 1 > getHeight()) return makeArray(0);
        return this.table[y];
    }

    public void set(int x, int y, @Nullable T value) {
        if (isOutside(x, y)) setNewDimensions(x + 1, y + 1);
        getLayer(y)[x] = value;
    }

    public void setLayer(int y, @NotNull T[] layer) {
        if (y + 1 > getHeight() || layer.length > getWidth()) setNewDimensions(layer.length, y + 1);
        this.table[y] = layer;
    }

    @NotNull
    @Override
    public Iterator<Item<T>> iterator() {
        final List<Item<T>> items = new ArrayList<>();
        for (int y = 0; y < this.table.length; y++)
            for (int x = 0; x < this.table[y].length; x++)
                items.add(new Item<>(x, y, get(x, y)));
        return items.iterator();
    }

    /**
     * @param width  new width
     * @param height new height
     * @return a list of items that were trimmed off if dimension is reduced
     */
    public List<Item<T>> setNewDimensions(int width, int height) {
        final List<Item<T>> trimmed = new ArrayList<>();

        this.width = width;
        for (int y = 0; y < height; y++) {
            if (y >= getHeight()) {
                setLayer(y, makeArray(width));
                continue;
            }
            final T[] currentLayer = getLayer(y);
            final int currentLength = currentLayer.length;
            if (currentLength == width) continue;
            if (currentLength > width)
                for (int x = width; x < currentLength; x++)
                    trimmed.add(new Item<>(x, y, get(x, y)));
            final T[] newArray = makeArray(width);

            System.arraycopy(currentLayer, 0, newArray, 0, width);

            setLayer(y, newArray);
        }

        return trimmed;
    }

    public int getWidth() {
        return width == -1 ? width = Arrays.stream(this.table)
                .map(layer -> layer.length)
                .max(Integer::compareTo)
                .orElse(-1) : width;
    }

    public int getHeight() {
        return this.table.length;
    }

    public boolean isOutside(int x, int y) {
        return x + 1 > getWidth() || y + 1 > getHeight();
    }

    @SuppressWarnings("unchecked")
    private T[] makeArray(int size) {
        this.clazz = this.clazz == null ? (Class<T>) this.table.getClass().getComponentType().getComponentType() : this.clazz;
        final T[] array = (T[]) Array.newInstance(this.clazz, size);
        if (size != 0)
            for (int i = 0; i < size; i++) array[i] = defaultValue();
        return array;
    }

    public record Item<T>(int x, int y, @Nullable T value) {
    }

}
