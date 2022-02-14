package aspireinc.torture.Files.ServerScriptService;

import aspireinc.torture.Torture;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class randomScripts {
    public static void actionBarMessage(Player player, String message){
        message =  message + " §3[" + "§eF" + "§3]";
        message = ChatColor.translateAlternateColorCodes('§', message);

        if(Torture.playerSpecifics.get(player.getUniqueId() + " entity message") != message){
            Torture.playerSpecifics.put(player.getUniqueId() + " entity message", message.toCharArray()[0]);
            Torture.playerSpecifics.put(player.getUniqueId() + " fullMessage", message);
        }
        List<Character> charList = message.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            String innerString = null;
            for(Character character : charList){
                if(innerString != null){
                    innerString = innerString + character;
                }else{
                    innerString = character.toString();
                }

                Torture.playerSpecifics.put(player.getUniqueId() + " entity message", innerString);
                TextComponent textComponent = new TextComponent(TextComponent.fromLegacyText(innerString));
                textComponent.setColor(net.md_5.bungee.api.ChatColor.GRAY);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);

            }

        };
        ScheduledFuture scheduledFuture = service.scheduleAtFixedRate(task,0, 100, TimeUnit.MILLISECONDS);
        Torture.playerSpecifics.put(player.getUniqueId() + " stopMessage", scheduledFuture);
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}
