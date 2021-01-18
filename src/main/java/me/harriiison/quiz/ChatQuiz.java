package me.harriiison.quiz;

import me.harriiison.quiz.commands.QuizCommand;
import me.harriiison.quiz.listeners.QuizSignInteractEvent;
import me.harriiison.quiz.tabcompleters.QuizTab;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatQuiz extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("quiz").setExecutor(new QuizCommand(this));
        getCommand("quiz").setTabCompleter(new QuizTab(this));
        getServer().getPluginManager().registerEvents(new QuizSignInteractEvent(this), this);
    }
}
