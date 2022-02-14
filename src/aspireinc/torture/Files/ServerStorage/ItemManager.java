package aspireinc.torture.Files.ServerStorage;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemManager {
    public static ItemStack showMobMoves;
    public static ItemStack showSwap;
    public static ItemStack showBag;
    public static ItemStack showRun;

    public static void init(){
        createShowMobMoves();
        createShowSwap();
        createShowBag();
        createRun();
    }
    public static void createShowMobMoves(){
        ItemStack itemStack = new ItemStack(Material.IRON_SWORD, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§cattack");
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(itemMeta);
        showMobMoves = itemStack;
    }
    public static void createShowSwap(){
        ItemStack itemStack = new ItemStack(Material.HOPPER, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§aswap");
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(itemMeta);
        showSwap = itemStack;
    }
    public static void createShowBag(){
        ItemStack itemStack = new ItemStack(Material.BROWN_SHULKER_BOX, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.of("#330C00") + "bag");
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(itemMeta);
        showBag = itemStack;
    }
    public static void createRun(){
        ItemStack itemStack = new ItemStack(Material.SADDLE, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.of("#0055FF") + "run");
        itemMeta.setUnbreakable(true);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemStack.setItemMeta(itemMeta);
        showRun = itemStack;
    }
}
