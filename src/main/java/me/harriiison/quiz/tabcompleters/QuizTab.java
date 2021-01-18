package me.harriiison.quiz.tabcompleters;

import me.harriiison.quiz.ChatQuiz;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class QuizTab implements TabCompleter {
    private ChatQuiz plugin;

    public QuizTab(ChatQuiz instance) {
        this.plugin = instance;
    }

    List<String> arguments = new ArrayList<>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (arguments.isEmpty()) {
            arguments.addAll(plugin.getConfig().getKeys(false));
            arguments.remove("settings");
        }

        List<String> result = new ArrayList<>();

        if (args.length == 1) {
            result.add("start");
            return result;
        }

        if (args.length == 3) {
            for (String argument: arguments) {
                if (argument.toLowerCase().startsWith(args[2].toLowerCase())) {
                    result.add(argument);
                }
            }
            return result;
        }

        return null;
    }

}
