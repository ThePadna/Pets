package main;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.bukkit.Bukkit.getServer;

/**
 * Created by Cory on 05/03/2017.
 */
public class Util {

    public static Pet deserialize(Map<String, Object> map) {
        return new Pet((String) map.get("name"), UUID.fromString((String) map.get("owner")), EntityType.fromName((String) map.get("type")), (long) map.get("insurance"));
    }

    public static Class<?> getNMSClass(String className) throws ClassNotFoundException {

        return Class.forName("net.minecraft.server." + getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + className);
    }

    public static Class<?> getCraftBukkitClass(String className) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + "." + className);
    }

    public static String format(long timestamp) {
        int day = (int) TimeUnit.MILLISECONDS.toDays(timestamp);
        long hours = TimeUnit.MILLISECONDS.toHours(timestamp) - (day *24);
        long minute = TimeUnit.MILLISECONDS.toMinutes(timestamp) - (TimeUnit.MILLISECONDS.toHours(timestamp)* 60);
        long second = TimeUnit.MILLISECONDS.toSeconds(timestamp) - (TimeUnit.MILLISECONDS.toMinutes(timestamp) *60);

        return day + "" + ChatColor.RED + "d " + ChatColor.RESET + "" + hours + ChatColor.RED + "h " +  ChatColor.RESET + "" + minute + ChatColor.RED + "m " + ChatColor.RESET + "" + second + "" + ChatColor.RED + "s" + ChatColor.RESET;
    }

    public static String toDate(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat( "dd" +  "/" +  "MM" + "/" +  "yyyy" + " h " + "a").format(date);
    }

    public static Location lookAt(Location loc, Location lookat) {
        loc = loc.clone();
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        if (dx != 0) {
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        loc.setPitch((float) -Math.atan(dy / dxz));
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
        loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
        return loc;
    }
}
