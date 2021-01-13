package me.harriiison.quiz;

import me.harriiison.quiz.commands.Quiz;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatQuiz extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("quiz").setExecutor(new Quiz(this));
    }
}
