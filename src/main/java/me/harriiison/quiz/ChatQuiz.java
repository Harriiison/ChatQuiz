package me.harriiison.quiz;

import me.harriiison.quiz.commands.QuizCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatQuiz extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("quiz").setExecutor(new QuizCommand(this));
    }
}
