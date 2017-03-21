package main;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_11_R1.EntityArmorStand;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Cory on 18/03/2017.
 */
public class PetAstronaut extends Pet {
    int smokeCounter = 0;

    public PetAstronaut(String petName, UUID owner, EntityType e, long insured) {
        super(petName, owner, e, insured);
    }

    @Override
    public boolean isAstronaut() {
        return true;
    }

    @Override
    public void spawn() {
        Player p = Bukkit.getPlayer(super.getOwner());
        super.entity = p.getWorld().spawnEntity(p.getLocation(), super.getEntityType());
        ArmorStand entityArmorStand = (ArmorStand) super.getEntity();
        entityArmorStand.setBasePlate(false);
        entityArmorStand.setSmall(true);
        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.WHITE);
        boots.setItemMeta(bootsMeta);
        entityArmorStand.setBoots(boots);
        ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestMeta = (LeatherArmorMeta) boots.getItemMeta();
        chestMeta.setColor(Color.WHITE);
        chest.setItemMeta(chestMeta);
        entityArmorStand.setChestplate(chest);
        SkullMeta  meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);

        meta.setOwner("space_man");

        ItemStack stack = new ItemStack(Material.SKULL_ITEM,1 , (byte)3);

        stack.setItemMeta(meta);
        entityArmorStand.setHelmet(stack);
        ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(Color.WHITE);
        leggings.setItemMeta(leggingsMeta);
        entityArmorStand.setLeggings(leggings);
        entityArmorStand.setArms(true);

        if (hasCustomName()) {
            entity.setCustomName(p.getDisplayName() + "'s " + super.getName());
        } else {
            entity.setCustomName(p.getDisplayName() + "'s " + ChatColor.WHITE + super.getEntityType().toString().toLowerCase());
        }
        entity.setCustomNameVisible(true);
        petFollowPlayer();
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap map = new HashMap<String, Object>();
        map.put("name", super.getName());
        map.put("type", "astronaut");
        map.put("insurance", super.getInsuranceDate());
        map.put("owner", super.getOwner());
        return map;
    }

    @Override
    public void tick(int taskID, int xDelay, int yDelay, int zDelay) {
        smokeCounter++;
        if(!super.isFrozen()) {
            Location astronaut = super.getEntity().getLocation();
            Player player = Bukkit.getPlayer(super.getOwner());
            Location playerLoc = player.getLocation();

            Location last;
            if (xDelay == 0) {
                xDelay = playerLoc.getBlockX();
                yDelay = playerLoc.getBlockY();
                zDelay = playerLoc.getBlockZ();
                last = new Location(player.getWorld(), xDelay, yDelay, zDelay);
            } else {
                last = new Location(player.getWorld(), xDelay, yDelay, zDelay);
                if (last.distance(playerLoc) < 3) return;
            }
            final float newZ = (float)(player.getLocation().getZ() + ( 1 * Math.sin(Math.toRadians(player.getLocation().getYaw() + 90 * 0))));
            final float newX = (float)(player.getLocation().getX() + ( 1 * Math.cos(Math.toRadians(player.getLocation().getYaw() + 90 * 0))));
            double height = 2.5;
            switch(smokeCounter) {
                case 15:
                    height = 2.4;
                    break;
                case 16:
                    height = 2.3;
                    break;
                case 17:
                    height = 2.2;
                    break;
                case 18:
                    height = 2.1;
                    break;
                case 19:
                    height = 2;
                    break;
                case 20:
                    height = 1.9;
                    break;
                case 21:
                    height = 2;
                    break;
                case 22:
                    height = 2.1;
                    break;
                case 23:
                    height = 2.2;
                    break;
                case 24:
                    height = 2.3;
                    break;
                case 25:
                    height = 2.4;
                    break;
                case 26:
                    height = 2.5;
                    smokeCounter = -5;
                    break;
            }
            Location target = new Location(playerLoc.getWorld(), newX, playerLoc.getY(), newZ);
            Location difference = target.subtract(astronaut);
            difference.getDirection().normalize().multiply(-1);
            super.getEntity().setVelocity(difference.toVector().add(new Vector(0, height, 0)));
            Block block = player.getTargetBlock((Set<Material>)null, 5);
            Location looking = Util.lookAt(super.getEntity().getLocation(), block.getLocation());
            ((ArmorStand)super.getEntity()).setHeadPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setBodyPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setLeftArmPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setRightArmPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setLeftLegPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            ((ArmorStand)super.getEntity()).setRightLegPose(new EulerAngle(Math.toRadians(looking.getPitch()), Math.toRadians(looking.getYaw()), 0));
            astronaut.getWorld().spawnParticle(Particle.CLOUD, astronaut, 0);
        }
    }
}
