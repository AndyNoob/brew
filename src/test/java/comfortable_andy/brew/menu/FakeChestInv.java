package comfortable_andy.brew.menu;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class FakeChestInv implements Inventory {

    private final ItemStack[] items = new ItemStack[54];

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setMaxStackSize(int size) {

    }

    @Override
    public @Nullable ItemStack getItem(int index) {
        return items[index];
    }

    @Override
    public void setItem(int index, @Nullable ItemStack item) {
        items[index] = item;
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        return new HashMap<>();
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        return new HashMap<>();
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... items) throws IllegalArgumentException {
        return null;
    }

    @Override
    public @Nullable ItemStack @NotNull [] getContents() {
        return new ItemStack[0];
    }

    @Override
    public void setContents(@Nullable ItemStack @NotNull [] items) throws IllegalArgumentException {
    }

    @Override
    public @Nullable ItemStack @NotNull [] getStorageContents() {
        return new ItemStack[0];
    }

    @Override
    public void setStorageContents(@Nullable ItemStack @NotNull [] items) throws IllegalArgumentException {

    }

    @Override
    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack item) {
        return false;
    }

    @Override
    public boolean contains(@NotNull Material material, int amount) throws IllegalArgumentException {
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
        return false;
    }

    @Override
    public @NotNull HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        return new HashMap<>();
    }

    @Override
    public @NotNull HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        return new HashMap<>();
    }

    @Override
    public int first(@NotNull Material material) throws IllegalArgumentException {
        return 0;
    }

    @Override
    public int first(@NotNull ItemStack item) {
        return 0;
    }

    @Override
    public int firstEmpty() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void remove(@NotNull Material material) throws IllegalArgumentException {

    }

    @Override
    public void remove(@NotNull ItemStack item) {

    }

    @Override
    public void clear(int index) {

    }

    @Override
    public void clear() {

    }

    @Override
    public int close() {
        return 0;
    }

    @Override
    public @NotNull List<HumanEntity> getViewers() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull InventoryType getType() {
        return InventoryType.CHEST;
    }

    @Override
    public @Nullable InventoryHolder getHolder() {
        return null;
    }

    @Override
    public @Nullable InventoryHolder getHolder(boolean useSnapshot) {
        return null;
    }

    @Override
    public @NotNull ListIterator<ItemStack> iterator() {
        return new ArrayList<ItemStack>().listIterator();
    }

    @Override
    public @NotNull ListIterator<ItemStack> iterator(int index) {
        return new ArrayList<ItemStack>().listIterator();
    }

    @Override
    public @Nullable Location getLocation() {
        return null;
    }
}
