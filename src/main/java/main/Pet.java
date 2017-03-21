package main;

import com.avaje.ebean.validation.NotNull;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.entity.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.util.*;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Date;

import static main.Util.getCraftBukkitClass;
import static main.Util.getNMSClass;
import static net.minecraft.server.v1_11_R1.SoundEffects.is;

/**
 * Created by Cory on 04/03/2017.
 */
public class Pet implements ConfigurationSerializable {

    private String name = null;
    private final UUID owner;
    private final EntityType entityType;
    public Entity entity = null;
    private boolean baby = false;
    private boolean frozen = false;
    private long insurance = 0;

    public Pet(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.owner = UUID.fromString((String) map.get("owner"));
        this.entityType = EntityType.fromName((String) map.get("type"));
        this.insurance = Long.valueOf((int)map.get("insurance"));
    }

    public Pet(String petName, UUID owner, EntityType e, long insured) {
        this.owner = owner;
        this.entityType = e;
        if(petName != null) name = petName;
        if(insured != 0) {
            this.insurance = insured;
        }
    }

    public void kill() {
        if(entity != null) {
            entity.remove();
            entity = null;
        }
    }

    public boolean isAstronaut() {
        return false;
    }

    public void setName(String name) {
        this.name = name.replace('&', '§');
        this.entity.setCustomName(Bukkit.getPlayer(this.getOwner()).getDisplayName() + "'s " + this.name);
    }

    public void save(boolean save) {
        if(!save) Pets.getInstance().nullifyPet(this.getOwner());
        else {
            Pets.getInstance().savePet(this);
        }
    }

    public void spawn() {
        Player p = Bukkit.getPlayer(owner);
            entity = p.getWorld().spawnEntity(p.getLocation(), entityType);
            if (hasCustomName()) {
                entity.setCustomName(p.getDisplayName() + "'s " + name);
            } else {
                entity.setCustomName(p.getDisplayName() + "'s " + ChatColor.WHITE + entityType.toString().toLowerCase());
            }
            entity.setCustomNameVisible(true);
            if(!(this instanceof PetAstronaut)) {
                this.NMS_eraseDefaults();
            }
            petFollowPlayer();
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public boolean hasCustomName() {
        return !(this.name == null);
    }

    public String getName() {
        if(name == null) {
            return name;
        } else {
            return this.getEntity().getName();
        }
    }

    public boolean hasInsurance() {
        return !(this.insurance == 0);
    }

    public long getInsuranceDate() {
        return this.insurance;
    }

    public void setInsuranceDate(long insurance) {
        this.insurance = insurance;
    }

    public ItemStack getEgg() {
        ItemStack is = new ItemStack(Material.MONSTER_EGG, 1);
        ItemMeta itemMeta = is.getItemMeta();
        ArrayList lore = new ArrayList();
        //insurance
        if(this.hasCustomName()) {
            lore.add("Custom Name: " + this.name);
        }
        if(this.hasInsurance()) {
            lore.add("Insured Until: " + Util.toDate(this.getInsuranceDate()));
        }

        String baby = "";
        if(this.isBaby()) baby = "Baby ";
        itemMeta.setDisplayName(baby + entityType.toString().toLowerCase());
        itemMeta.setLore(lore);
        is.setItemMeta(itemMeta);

        return is;
    }

    public boolean setBaby() {
        switch(entityType) {
            case COW:
                CraftCow cow = (CraftCow) entity;
                cow.setBaby();
                baby = true;
                break;
            case ZOMBIE:
                CraftZombie zombie = (CraftZombie) entity;
                zombie.setBaby(true);
                baby = true;
                break;
            case WOLF:
                CraftWolf wolf = (CraftWolf) entity;
                wolf.setBaby();
                baby = true;
                break;
            case LLAMA:
                CraftLlama llama = (CraftLlama) entity;
                llama.setBaby();
                baby = true;
                break;
            case OCELOT:
                CraftOcelot ocelot = (CraftOcelot) entity;
                ocelot.setBaby();
                baby = true;
                break;
            case PIG:
                CraftPig pig = (CraftPig) entity;
                pig.setBaby();
                baby = true;
                break;
            case RABBIT:
                CraftRabbit rabbit = (CraftRabbit) entity;
                rabbit.setBaby();
                baby = true;
                break;
            case POLAR_BEAR:
                CraftPolarBear polarBear = (CraftPolarBear) entity;
                polarBear.setBaby();
                baby = true;
                break;
            default:
                break;
        }
        return baby;
    }

    public boolean isBaby() {
        return this.baby;
    }

    public void tick(int taskID, int xDelay, int yDelay, int zDelay) {
        if (this.isFrozen()) return;
        if (entity != null) {
            if (entity.isDead() || Bukkit.getPlayer(getOwner()) == null) {
                Bukkit.getScheduler().cancelTask(taskID);
                return;
            }
            Location entityLocation = entity.getLocation();
            Location playerLocation = Bukkit.getPlayer(getOwner()).getLocation();
            Location last;
            if (xDelay == 0) {
                xDelay = playerLocation.getBlockX();
                yDelay = playerLocation.getBlockY();
                zDelay = playerLocation.getBlockZ();
                last = new Location(playerLocation.getWorld(), xDelay, yDelay, zDelay);
            } else {
                last = new Location(playerLocation.getWorld(), xDelay, yDelay, zDelay);
                if (last.distance(playerLocation) < 2) return;
            }
            double dist = last.distance(entityLocation);
            if (dist >= 15 || (last.getBlockY() + 4) <= entityLocation.getBlockY() || (last.getBlockY() - 4) >= entityLocation.getBlockY()) {
                entity.teleport(last);
                return;
            }
            PathEntity path;
            EntityInsentient entityInsentient = (EntityInsentient) ((CraftEntity) entity).getHandle();
            path = entityInsentient.getNavigation().a(xDelay, yDelay, zDelay);
            entityInsentient.getNavigation().a(path, 2D);
            xDelay = playerLocation.getBlockX();
            yDelay = playerLocation.getBlockY();
            zDelay = playerLocation.getBlockZ();
        }
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap map = new HashMap<String, Object>();
        map.put("name", name);
        map.put("type", entityType.getName());
        map.put("insurance", insurance);
        map.put("owner", owner);
        return map;
    }

    public void NMS_eraseDefaults() {
        EntityCreature c = (EntityCreature) ((EntityInsentient)((CraftEntity)entity).getHandle());
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(c.goalSelector, Sets.newLinkedHashSet());
            bField.set(c.targetSelector, Sets.newLinkedHashSet());
            cField.set(c.goalSelector, Sets.newLinkedHashSet());
            cField.set(c.targetSelector, Sets.newLinkedHashSet());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void petFollowPlayer() {
        FollowPlayer followPlayer = new FollowPlayer(this.owner, this);
        followPlayer.setId(Bukkit.getScheduler().scheduleSyncRepeatingTask(Pets.getInstance(), followPlayer, 0L, 1L));
    }

    public Entity getEntity() {
        return entity;
    }

    public void toggleFrozen() {
        frozen = !frozen;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

}

class FollowPlayer implements Runnable {

    private Pet pet;
    private final UUID player;
    private int id;
    private int xDelay, yDelay, zDelay;

    public FollowPlayer(UUID p, Pet inst) {
        this.pet = inst;
        this.player = p;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int getId() {
        return id;
    }

    public void run() {
        pet.tick(this.id, xDelay, yDelay, zDelay);
    }
}
