# Brew (menu)
This library was created because I felt that making inventory menus with Bukkit is pretty messy. In particular, things such as pagination, click detection, and inventory switching. This library attempts to tackle these problems.

<img alt="pagination" src="https://github.com/user-attachments/assets/4338d072-2def-4dea-a9cb-de4ece47ad77"/>

## Adding to your dependencies [![](https://jitpack.io/v/AndyNoob/brew.svg)](https://jitpack.io/#AndyNoob/brew)
To get started, add the library to as a dependency.
### Maven
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
    <!--...-->
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>com.github.AndyNoob</groupId>
        <artifactId>brew</artifactId>
        <version>version from the jitpack badge</version>
    </dependency>
    <!--...-->
</dependencies>
```
### Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
    // ...
}
```
```groovy
dependencies {
    implementation 'com.github.AndyNoob:brew:<version from badge>'
    // ...
}
```
## Basic usage
First, a menu object should be created.
```java
public class SomeClass {
    public void doSomething() {
        // create a new menu
        Menu menu = new Menu("id", "title name", "description");
    }
}
```
A menu object by itself serves no purpose. It is simply a container for all your components.
```java
public class SomeClass {
    public void doSomething() {
        Menu menu = new Menu("id", "title name", "description");
        // add a new component
        menu.addComponent(new SimpleButtonComponent(new Vector2i(0, 0), 1, 1, new ItemStack(Material.DIAMOND), h -> h.sendMessage("you clicked button!")));
    }
}
```
Now, we want to actually display this menu. So, we create a new inventory.
```java
public class SomeClass {
    public void doSomething() {
        Menu menu = new Menu("id", "title name", "description");
        menu.addComponent(new SimpleButtonComponent(new Vector2i(0, 0), 1, 1, new ItemStack(Material.DIAMOND), h -> h.sendMessage("you clicked button!")));
        // create inventory and set in renderer
        Inventory inventory = Bukkit.createInventory(null, 54);
        Renderer renderer = menu.getRenderer();
        renderer.setInventory(inventory);
        // then we render
        renderer.render();
    }
}
```

> [!NOTE]
> Note that row of a component increase as the y-position of a component increase.

Now, a diamond should be rendered in the center of the fourth row. However, you may notice that the inventory still acts like a normal double chest inventory. This is because you must notify the menu of click events (`InventoryClickEvent` AND `InventoryDragEvent`) yourself.

```java
// assume that this listener is registered.
public class SomeClass implements Listener {
    
    private Menu menu;
    
    public void doSomething() {
        menu = new Menu("id", "title name", "description");
        menu.addComponent(new SimpleButtonComponent(new Vector2i(0, 0), 1, 1, new ItemStack(Material.DIAMOND), h -> h.sendMessage("you clicked a button!")));
        Inventory inventory = Bukkit.createInventory(null, 54);
        Renderer renderer = menu.getRenderer();
        renderer.setInventory(inventory);
        renderer.render();
    }
    
    // the event handlers
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        menu.handleClick(e);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        menu.handleClick(e);
    }
}
```

Now players should be able to see the message!