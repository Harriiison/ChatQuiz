package me.harriiison.quiz.listeners;

import me.harriiison.quiz.ChatQuiz;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

public class QuizSignInteractEvent implements Listener {
    private ChatQuiz plugin;

    public QuizSignInteractEvent(ChatQuiz instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        // Process a quiz if the player right clicks on a sign

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();

                // Check if it is a quiz sign
                if (sign.getLine(0).contains("[QUIZ]")) {
                    String quizName = ChatColor.stripColor(sign.getLine(1));
                    Set<String> quizNames = plugin.getConfig().getKeys(false);

                    if (quizNames.contains(quizName)) {
                        Player player = event.getPlayer();

                        // Player must have perms to use signs
                        if (player.hasPermission("quiz.signs." + quizName) || player.hasPermission("quiz.admin")) {
                            // run the quiz start command via the console
                            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "quiz start " + player.getName() + " " + quizName);
                        } else {
                            player.sendMessage(ChatColor.RED + "You do not have the required permissions to take this quiz!");
                            player.sendMessage(ChatColor.RED + "Please contact a server administrator if you believe this is a mistake.");
                        }
                    }
                }
            }
        }
    }

}
