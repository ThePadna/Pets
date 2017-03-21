package main;

import anvil.AnvilGUI;
import com.avaje.ebean.validation.NotNull;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.menubuilder.chat.ChatListener;
import org.inventivetalent.menubuilder.chat.ChatMenuBuilder;
import org.inventivetalent.menubuilder.chat.LineBuilder;
import org.inventivetalent.menubuilder.inventory.InventoryMenuBuilder;
import org.inventivetalent.menubuilder.inventory.ItemListener;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cory on 27/02/2017.
 */
public class Pets extends JavaPlugin implements Listener {

    /**
     * Singleton
     */
    private final HashSet<Pet> activePets = new HashSet<Pet>();
    private YamlConfiguration yamlConfiguration;
    private static Pets instance;
    private Inventory kaz;
    private ChatMenuBuilder insure;
    private File file;
    private static Economy economy;
    ItemStack anvil_Wool, anvil_NameTag;

    public Pets() {
        this.instance = this;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }


    public static Pets getInstance() {
        return instance;
    }

    private ItemStack genCustomItem(Material material, String name) {
        ItemStack i = new ItemStack(material, 1);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(name);
        i.setItemMeta(im);
        return i;
    }

    private void insurePet(Pet pet, int days) {
        long now = System.currentTimeMillis();
        if(pet.hasInsurance()) {
            long current = pet.getInsuranceDate();
            current += TimeUnit.DAYS.toMillis(days);
            pet.setInsuranceDate(current);
        } else {
            pet.setInsuranceDate(now + TimeUnit.DAYS.toMillis(days));
        }
        Player p = Bukkit.getPlayer(pet.getOwner());
        p.sendMessage(ChatColor.YELLOW + "[kaz]: Your pet is insured with me for" + Util.format((pet.getInsuranceDate() - now)));
        pet.save(true);
    }

    @Override
    public void onEnable() {
        anvil_NameTag = new ItemStack(Material.NAME_TAG, 1);
        ConfigurationSerialization.registerClass(Pet.class);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        if(!setupEconomy()) {
            System.out.println("Disabling Pets -- VAULT plugin not found!");
            this.getPluginLoader().disablePlugin(this);
        }

        this.insure = new ChatMenuBuilder().withLine(new LineBuilder().append(new ChatListener() {
            @Override
            public void onClick(Player player) {
                Pet pet = getPlayerPet(player.getUniqueId());
                if(pet != null) {
                    if(economy.has(player, 2000)) {
                        insurePet(pet, 1);
                        economy.withdrawPlayer(player, 2000);
                    } else {
                        player.sendMessage(ChatColor.GREEN + "[kaz]: Sorry, I charge $2000 for 1 day of insurance.");
                    }
                } else {
                    player.sendMessage(ChatColor.YELLOW + "kaz: You have no active pet to insure.");
                    return;
                }
            }
        }, new TextComponent(ChatColor.GREEN + "1 Day - " + ChatColor.DARK_GREEN + "$2,000\n"))
                .append(new ChatListener() {
                    @Override
                    public void onClick(Player player) {
                        Pet pet = getPlayerPet(player.getUniqueId());
                        if(pet != null) {
                            if(economy.has(player, 10000)) {
                                insurePet(pet, 7);
                                economy.withdrawPlayer(player, 10000);
                            } else {
                                player.sendMessage(ChatColor.GREEN + "[kaz]: Sorry, I charge $10,000 for 7 days of insurance.");
                            }
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "kaz: You have no active pet to insure.");
                            return;
                        }
                    }
                }, new TextComponent(ChatColor.GREEN + "7 Day - " +  ChatColor.DARK_GREEN + "$10,000\n"))
                .append(new ChatListener() {
                    @Override
                    public void onClick(Player player) {
                        Pet pet = getPlayerPet(player.getUniqueId());
                        if(pet != null) {
                            if(economy.has(player, 30000)) {
                                insurePet(pet, 31);
                                economy.withdrawPlayer(player, 30000);
                            } else {
                                player.sendMessage(ChatColor.GREEN + "[kaz]: Sorry, I charge $30000 for 31 days of insurance.");
                            }
                        } else {
                            player.sendMessage(ChatColor.YELLOW + "kaz: You have no active pet to insure.");
                            return;
                        }
                    }
                }, new TextComponent(ChatColor.GREEN + "31 Day - " + ChatColor.DARK_GREEN + "$30,000\n")));

        ItemStack nameTag = this.genCustomItem(Material.NAME_TAG, ChatColor.YELLOW + "Pet Name Change");
        ItemStack skull = this.genCustomItem(Material.SKULL_ITEM, ChatColor.YELLOW + "Pet Insurance");
        this.kaz = new InventoryMenuBuilder().withSize(9).withTitle(ChatColor.RED + "kaz")
                .withItem(0, nameTag, new ItemListener() {
                    @Override
                    public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                       AnvilGUI anvilGUI = new AnvilGUI(getInstance(), player, new AnvilGUI.AnvilClickHandler() {
                            @Override
                            public boolean onClick(AnvilGUI menu, String text){
                                Pet pet = getPlayerPet(player.getUniqueId());
                                if(pet == null) {
                                    player.sendMessage(ChatColor.GREEN + "[kaz]: I don't see your pet?");
                                    return true;
                                }
                                if(economy.has(player, 2000)) {
                                    economy.withdrawPlayer(player, 2000);
                                } else {
                                    player.sendMessage(ChatColor.GREEN + "[kaz]: Sorry, I charge $2,000 for name changes. ");
                                    return true;
                                }
                                pet.setName(text);
                                return true;
                            }
                        });
                       anvilGUI.setInputName("Enter Pet Name");
                       anvilGUI.setItem(2, new ItemStack(Material.WOOL, 1), "Use this name");
                       anvilGUI.open();
                    }
                }, ClickType.LEFT)
                .withItem(3, skull, new ItemListener() {
                    @Override
                    public void onInteract(Player player, ClickType clickType, ItemStack itemStack) {
                        player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "[kaz]: Please click which insurance option you would like:");
                        insure.show(player);
                    }
                }, ClickType.LEFT).build();



        this.getDataFolder().mkdirs();
        file = new File(this.getDataFolder(), "pets.yml");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        yamlConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public boolean onCommand(CommandSender s, Command cmd, String commandName, String[] args) {
        if (commandName.equalsIgnoreCase("pet")) {
            if (args.length != 1 && args.length != 2) {
                s.sendMessage("Invalid args! /pet <Entity> [optional]-baby  || /pet name <customName>");
                return false;
            }

            Player p = (Player) s;
            if(args[0].equalsIgnoreCase("name")) {
                if(args[1] == null) s.sendMessage("Invalid args! /pet <Entity> [optional]-baby || /pet name <customName>");
                String name = args[1];
                Pet pet = this.getPlayerPet(p.getUniqueId());
                if(pet == null) s.sendMessage("You have no active pets.");
                else {
                    pet.setName(name);
                }
                return false;
            }

            EntityType type = EntityType.fromName(args[0]);
            if (type == null && !args[0].equalsIgnoreCase("astronaut")) {
                s.sendMessage("Invalid entity: " + args[0]);
                return false;
            } else {
                if (this.getPlayerPet(p.getUniqueId()) != null) {
                    s.sendMessage("You already have an active pet.");
                    return false;
                }
                if (type != null) {
                    Pet pet = new Pet(null, p.getUniqueId(), type, 0);
                    pet.spawn();
                    if (args.length == 2) {
                        if (args[1].equals("-baby")) {
                            if (!pet.setBaby()) {
                                s.sendMessage("Cannot make entity " + args[0] + " baby");
                                return false;
                            }
                        } else {
                            s.sendMessage("Invalid args! /pet <Entity> [optional]-baby  || /pet name <customName>");
                            return false;
                        }
                    }
                    this.activePets.add(pet);
                } else if(args[0].equalsIgnoreCase("astronaut")) {
                    PetAstronaut astronaut = new PetAstronaut(null, p.getUniqueId(), EntityType.ARMOR_STAND, 0);
                    astronaut.spawn();
                    this.activePets.add(astronaut);
                }
            }
        }
        return false;
    }

    public void savePet(Pet p) {
        yamlConfiguration.set(p.getOwner().toString(), p);
        try {
            yamlConfiguration.save(file);
        } catch(Exception e1) {
            e1.printStackTrace();
        }
    }

    public void nullifyPet(UUID owner) {
        yamlConfiguration.set(owner.toString(), null);
        try {
            yamlConfiguration.save(file);
        } catch(Exception e1) {
            e1.printStackTrace();
        }
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent e) {
        Map<String, Object> map = (Map<String, Object>) yamlConfiguration.get(e.getPlayer().getUniqueId().toString());
        if(map == null) return;
        Pet pet = Util.deserialize(map);
        pet.spawn();
        activePets.add(pet);
    }

    public Pet getPlayerPet(UUID uuid) {
        Iterator iterator = this.activePets.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet) iterator.next();
            if(pet.getOwner() == uuid) {
                return pet;
            }
        }
        return null;
    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent e) {
        Pet pet = this.getPlayerPet(e.getPlayer().getUniqueId());
        if(pet != null) {
            this.savePet(pet);
            pet.kill();
            this.activePets.remove(pet);
        }
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled=true)
    public void playerInteractNPC(PlayerInteractEntityEvent e) {
        Entity clicked = e.getRightClicked();
        if(clicked.getType() == EntityType.PLAYER) {
            if(clicked.getName().equals("kaz")) {
                e.getPlayer().openInventory(this.kaz);
            }
        }
    }

    private boolean isFull(ItemStack[] inventoryContents) {
        for(int i = 0; i <= inventoryContents.length; i++) {
            if(inventoryContents[i] == null) return false;
        }
        return true;
    }

    @EventHandler
    public void playerInteractPet(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        Iterator iterator = this.activePets.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet) iterator.next();
            if(entity == pet.getEntity()) {
                Player p = Bukkit.getPlayer(pet.getOwner());
                if(isFull(p.getInventory().getContents())) {
                    p.sendMessage(ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + "Inventory is full, cannot return pet to egg form.");
                } else {
                    p.getInventory().addItem(pet.getEgg());
                    pet.save(false);
                    iterator.remove();
                    pet.kill();
                }
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent ev) {
        for(Pet pet : this.activePets) {
            if(ev.getEntity() == pet.getEntity()) ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityHitByEntity(EntityDamageByEntityEvent ev) {
        Iterator iterator = this.activePets.iterator();
        while(iterator.hasNext()) {
            Pet pet = (Pet) iterator.next();
            if(pet.isAstronaut()) continue;
            if(ev.getEntity() == pet.getEntity()) {
                Player p = Bukkit.getPlayer(pet.getOwner());
                if(p.isSneaking()) {
                    pet.toggleFrozen();
                    if(pet.isFrozen()) p.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "*WHISTLE* " + ChatColor.RESET + "sit.");
                    else p.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "*WHISTLE* " + ChatColor.RESET + "follow me.");
                }
            }
        }
    }

    @EventHandler
    public void onEggUse(PlayerInteractEvent e) {
        if(e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().getInventory().getItemInMainHand().getType() == Material.MONSTER_EGG) {
            if(e.getItem().hasItemMeta()) {
                Player player = e.getPlayer();
                if(this.getPlayerPet(player.getUniqueId()) != null) {
                    player.sendMessage("You already have an active pet.");
                    return;
                }
                ItemMeta itemMeta = e.getItem().getItemMeta();
                player.getInventory().remove(e.getItem());
                String name = null;
                String insurance = null;
                if(itemMeta.hasLore()) {
                    List metaList = itemMeta.getLore();
                    for (int i = 0; i < metaList.size(); i++) {
                        String metaEntry = ((String) metaList.get(i));
                        if (metaEntry.contains("Custom Name")) {
                            name = metaEntry.substring(11);
                        }
                        if (metaEntry.contains("Insured Until")) {
                            insurance = metaEntry.substring(15);
                        }
                    }
                }

                EntityType entityType;
                boolean baby;
                String babyText = "Baby ";
                if(itemMeta.getDisplayName().contains(babyText)) {
                    String strippedName = itemMeta.getDisplayName().replace(babyText, "");
                    entityType = EntityType.fromName(strippedName);
                    baby = true;
                } else {
                    entityType = EntityType.fromName(itemMeta.getDisplayName());
                    baby = false;
                }

                long insuranceDate = 0;
                if(insurance != null) {
                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy h a");
                    java.util.Date date = null;
                    try {
                        date = dateFormat.parse(insurance);
                    } catch(ParseException ex) {
                        ex.printStackTrace();
                    }
                    if(date != null) insuranceDate = date.getTime();
                }
                Pet pet = new Pet(name, player.getUniqueId(), entityType, insuranceDate);
                pet.spawn();
                if(baby) pet.setBaby();
                this.activePets.add(pet);

                player.sendMessage(ChatColor.GREEN + "Pet " + ChatColor.YELLOW + pet.getName() + ChatColor.GREEN + " follows close behind.");
                if(pet.hasInsurance()) {
                    player.sendMessage(Util.format(pet.getInsuranceDate()) + ChatColor.GREEN + " remaining insurance time.");
                }
            }
        }
    }
}
