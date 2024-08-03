package comfortable_andy.brew.test_plugin;

import com.mojang.brigadier.Command;
import comfortable_andy.brew.menu.Menu;
import comfortable_andy.brew.menu.componenets.Renderer;
import comfortable_andy.brew.menu.componenets.defaults.SimpleButtonComponent;
import comfortable_andy.brew.test_plugin.components.SimpleTextFieldComponent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector2i;

import java.util.HashMap;
import java.util.Map;

public class BrewTestMain extends JavaPlugin implements Listener {

    private final Map<HumanEntity, Menu> menus = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        registerCommands();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void registerCommands() {
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, h -> {
            final Commands commands = h.registrar();
            final var root = Commands.literal("bruh");
            root.then(
                    Commands
                            .literal("text")
                            .executes(s -> {
                                final CommandSourceStack source = s.getSource();
                                final CommandSender sender = source.getExecutor() == null ? source.getSender() : source.getExecutor();
                                if (!(sender instanceof HumanEntity entity)) return 0;
                                Menu menu = menus.computeIfAbsent(entity, k -> new Menu("yo", entity.getName(), "bruhhhh"));
                                Renderer renderer = menu.getRenderer();
                                menu.addComponent(new SimpleTextFieldComponent(this, new Vector2i(), (e, str) -> menu.setDisplayName(str)));
                                Inventory inventory = renderer.getInventory() == null ? Bukkit.createInventory(null, 54) : renderer.getInventory();
                                renderer.setInventory(inventory);
                                entity.openInventory(inventory);
                                renderer.render();
                                return Command.SINGLE_SUCCESS;
                            })
            );
            commands.register(root.build());
        });
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Menu menu = menus.get(event.getWhoClicked());
        if (menu != null) menu.handleClick(event);
    }
}
