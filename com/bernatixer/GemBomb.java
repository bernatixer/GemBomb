package com.bernatixer;

import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GemBomb extends JavaPlugin implements Listener{

    protected int step = 0;
    protected FireworkEffect firework;
    
  @Override
  public void onEnable(){

    Bukkit.getServer().getPluginManager().registerEvents(this, this);     
    getConfig().options().copyDefaults(true);
    saveConfig();
  }

  @EventHandler
  public void take(PlayerPickupItemEvent event){
  
      if(event.getItem().getItemStack().getType() == Material.getMaterial(getConfig().getString("Item"))){
          if(getConfig().getBoolean("CommandB") == true){
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), getConfig().getString("Command").replaceAll("%player%", event.getPlayer().getName()));
            event.setCancelled(true);
            event.getItem().remove();
          }else{
            event.setCancelled(true);
            event.getItem().remove();
          }
          
      }
      
  }
  
  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
      
    Player player = (Player)sender;
    if(commandLabel.equalsIgnoreCase("gembomb") && player.isOp()){
        if(args.length == 0){
            ItemStack is = new ItemStack(Material.getMaterial(getConfig().getString("Block")));
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§a§lGemBomb");
            is.setItemMeta(im);
            player.getInventory().addItem(is);
        }else{
            if(args[0].equals("reload")){
                reloadConfig();
                player.sendMessage("§aYou have reloaded GemBomb config.");
            }else{
                player.sendMessage("§aUsage: /gembomb reload");
            }
        }
    }
    
        return false;
  }
  
  @EventHandler
  public void use(PlayerInteractEvent event){

    final Player player = event.getPlayer();

    if (((event.getAction() != Action.RIGHT_CLICK_AIR) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)))
      return;
    
    if(player.getItemInHand().getType() == Material.getMaterial(getConfig().getString("Block")) && "§a§lGemBomb".equals(player.getItemInHand().getItemMeta().getDisplayName())){

        Bukkit.broadcastMessage(getConfig().getString("Message").replaceAll("%player%", player.getName()).replaceAll("&", "§"));
        player.getInventory().remove(player.getItemInHand());        
        player.playSound(player.getLocation(), Sound.WITHER_SHOOT, 0.1F, 1.0F);
        final Location loc = player.getLocation();
        Location loc2 = loc;
        loc2.setY(loc.getY() + 0.2);

            new BukkitRunnable(){
                int count = 10;
                @Override
                public void run(){

                    fire(loc);
                    Random r = new Random();
                    int a = getConfig().getInt("Gems");
                    for(int i = 0; i < a; i++){
                        ItemStack moneda = new ItemStack(Material.getMaterial(getConfig().getString("Item")), 1);
                        String s = Integer.toString(r.nextInt(100000) + 1);
                        ItemMeta im = moneda.getItemMeta();
                        im.setDisplayName(s);
                        moneda.setItemMeta(im);
                        Item b = player.getWorld().dropItemNaturally(loc, moneda);
                        Vector dir = b.getLocation().getDirection();
                        Vector vec = new Vector(dir.getX() * 0.4D, 0.4D, dir.getZ() * 0.4D);
                        b.setVelocity(vec);
                        b.setFallDistance(-80.0F);
                    }
                    
                    if(--count == 0)
                        cancel();

                }
            }.runTaskTimer(this, 0L, 20L);
            
    }

  }
  
    public void fire(Location location){
        Random random = new Random(System.nanoTime());
        if (firework == null){
            Builder b = FireworkEffect.builder().with(FireworkEffect.Type.BURST);
            b.withColor(Color.RED).withColor(Color.ORANGE).withColor(Color.BLUE);
            b.withFade(Color.YELLOW);
            b.trail(true);
            firework = b.build();
        }
        for(int i = 0; i < 100; i++){

            Vector v = new Vector(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1).normalize().multiply(2);
            explosion(location, v);
            if (5 != 0 && step % 5 == 0)
                location.getWorld().playSound(location, Sound.EXPLODE, 100, 1);
        }
        step++;
    }

    protected void explosion(Location location, Vector v){
        final Firework fw = (Firework) location.getWorld().spawnEntity(location.add(v), EntityType.FIREWORK);
        location.subtract(v);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(0);
        for(int i = 0; i < 2; i++){
            meta.addEffect(firework);
        }
        fw.setFireworkMeta(meta);
        fw.detonate();
    }

}
