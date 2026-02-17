package ua.atherium.holyitems.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import ua.atherium.holyitems.HolyWorldItems;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MachineManager {

    private final HolyWorldItems plugin;
    private final Set<Location> goldenSpawners = new HashSet<>();
    private final Set<Location> autoCrafters = new HashSet<>();
    private final Set<Location> fastFurnaces = new HashSet<>();

    public MachineManager(HolyWorldItems plugin) {
        this.plugin = plugin;
        loadMachines();
        startTasks();
    }

    private void loadMachines() {
        File file = new File(plugin.getDataFolder(), "machines.yml");
        if (!file.exists()) return;
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        loadList(config, "golden_spawners", goldenSpawners);
        loadList(config, "auto_crafters", autoCrafters);
        loadList(config, "fast_furnaces", fastFurnaces);
    }

    private void loadList(FileConfiguration config, String path, Set<Location> set) {
        if (!config.contains(path)) return;
        for (String s : config.getStringList(path)) {
            try {
                set.add(deserializeLoc(s));
            } catch (Exception ignored) {}
        }
    }

    public void saveMachines() {
        File file = new File(plugin.getDataFolder(), "machines.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        saveList(config, "golden_spawners", goldenSpawners);
        saveList(config, "auto_crafters", autoCrafters);
        saveList(config, "fast_furnaces", fastFurnaces);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveList(FileConfiguration config, String path, Set<Location> set) {
        List<String> list = new ArrayList<>();
        for (Location loc : set) {
            list.add(serializeLoc(loc));
        }
        config.set(path, list);
    }

    public void addMachine(Location loc, String type) {
        switch (type) {
            case "golden_spawner": goldenSpawners.add(loc); break;
            case "auto_crafter": autoCrafters.add(loc); break;
            case "fast_furnace": fastFurnaces.add(loc); break;
        }
        saveMachines();
    }

    public boolean isMachine(Location loc, String type) {
        switch (type) {
            case "golden_spawner": return goldenSpawners.contains(loc);
            case "auto_crafter": return autoCrafters.contains(loc);
            case "fast_furnace": return fastFurnaces.contains(loc);
            default: return false;
        }
    }

    private void startTasks() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Location> iter = goldenSpawners.iterator();
            while (iter.hasNext()) {
                Location loc = iter.next();
                if (loc.getBlock().getType() != Material.SPAWNER) {
                    iter.remove();
                    continue;
                }

                int amount = 10 + new Random().nextInt(41);
                ItemStack drop = new ItemStack(Material.GOLD_NUGGET, amount);
                loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 1, 0.5), drop);

                if (new Random().nextInt(100) < 5) {
                    loc.getBlock().setType(Material.AIR);
                    loc.getWorld().createExplosion(loc, 1F, false);
                    iter.remove();
                }
            }
        }, 6000L, 6000L);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Location> iter = autoCrafters.iterator();
            while (iter.hasNext()) {
                Location loc = iter.next();
                if (loc.getBlock().getType() != Material.DISPENSER) {
                    iter.remove();
                    continue;
                }

                org.bukkit.block.Dispenser dispenser = (org.bukkit.block.Dispenser) loc.getBlock().getState();
                Inventory inv = dispenser.getInventory();

                ItemStack[] matrix = new ItemStack[9];
                boolean empty = true;
                for (int i = 0; i < 9; i++) {
                    matrix[i] = inv.getItem(i);
                    if (matrix[i] != null && matrix[i].getType() != Material.AIR) empty = false;
                }

                if (empty) continue;

                ItemStack result = null;

                Iterator<Recipe> recipes = Bukkit.recipeIterator();
                while (recipes.hasNext()) {
                    Recipe r = recipes.next();
                    if (r instanceof ShapedRecipe) {
                        ShapedRecipe sr = (ShapedRecipe) r;
                        if (matchShaped(sr, matrix)) {
                            result = sr.getResult();
                            break;
                        }
                    }
                }

                if (result != null) {
                    for (int i = 0; i < 9; i++) {
                        if (matrix[i] != null && matrix[i].getType() != Material.AIR) {
                            ItemStack is = inv.getItem(i);
                            is.setAmount(is.getAmount() - 1);
                            inv.setItem(i, is);
                        }
                    }
                    loc.getWorld().dropItemNaturally(loc.clone().add(0.5, -1, 0.5), result);
                    loc.getWorld().playSound(loc, org.bukkit.Sound.BLOCK_ANVIL_USE, 0.5f, 1f);
                }
            }
        }, 60L, 60L);
    }

    private boolean matchShaped(ShapedRecipe recipe, ItemStack[] matrix) {
        String[] shape = recipe.getShape();
        Map<Character, RecipeChoice> map = recipe.getChoiceMap();

        int rows = shape.length;
        int cols = shape[0].length();

        for (int rowOffset = 0; rowOffset <= 3 - rows; rowOffset++) {
            for (int colOffset = 0; colOffset <= 3 - cols; colOffset++) {
                if (checkMatch(recipe, matrix, rowOffset, colOffset)) return true;
            }
        }
        return false;
    }

    private boolean checkMatch(ShapedRecipe recipe, ItemStack[] matrix, int rowOffset, int colOffset) {
        String[] shape = recipe.getShape();
        Map<Character, RecipeChoice> map = recipe.getChoiceMap();

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int index = r * 3 + c;
                ItemStack item = matrix[index];

                int recipeR = r - rowOffset;
                int recipeC = c - colOffset;

                boolean inRecipe = recipeR >= 0 && recipeR < shape.length && recipeC >= 0 && recipeC < shape[0].length();

                if (inRecipe) {
                    char key = shape[recipeR].charAt(recipeC);
                    RecipeChoice choice = map.get(key);
                    if (choice == null) {
                        if (item != null && item.getType() != Material.AIR) return false;
                    } else {
                        if (item == null || !choice.test(item)) return false;
                    }
                } else {
                    if (item != null && item.getType() != Material.AIR) return false;
                }
            }
        }
        return true;
    }

    private String serializeLoc(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location deserializeLoc(String s) {
        String[] parts = s.split(",");
        if (Bukkit.getWorld(parts[0]) == null) return null;
        return new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }

    public void removeMachine(Location loc) {
        Location blockLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        if (goldenSpawners.remove(blockLoc) || autoCrafters.remove(blockLoc) || fastFurnaces.remove(blockLoc)) {
            saveMachines();
        }
    }
}
