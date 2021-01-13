package me.harriiison.quiz.commands;

import me.harriiison.quiz.ChatQuiz;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class Quiz implements CommandExecutor {

    private ChatQuiz plugin;

    public Quiz(ChatQuiz instance) {
        this.plugin = instance;
    }

    Map<String, List<Object>> participants = new HashMap<>();
    Map<String, List<String>> results = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("quiz")) {

            // "/quiz"
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                sender.sendMessage("");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&lChat Quiz &r&7(V" + plugin.getDescription().getVersion() + ")"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Designed by Harrison#4651 <&nhttps://harriiison.me/&r&7>"));
                sender.sendMessage("");
                if (sender.hasPermission("quiz.admin")) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3&nHelp - Admin Commands"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/quiz reload &r>> &eReload the quiz configuration and questions"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/quiz start [Player] [QuizName] &r>> &eBegin a quiz for a player"));
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7To begin making a quiz, open the config.yml file located in the plugins directory"));
                    sender.sendMessage("");
                }
                return true;
            }

            // "/quiz reload"
            if (args.length == 1 && sender.hasPermission("quiz.admin")) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSuccess. The quiz config has been reloaded."));
                }
                return true;
            }

            // "/quiz start [Player] [QuizName]"
            if (args.length == 3  && sender.hasPermission("quiz.admin")) {
                if (args[0].equalsIgnoreCase("start")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Player"));
                        return true;
                    }

                    if (participants.containsKey(player.getName())) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat player already has a quiz in progress. Please try again later."));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou are already taking a quiz. Please finish that one first."));
                        return true;
                    }

                    String quizName = args[2];

                    Set<String> quizNames = plugin.getConfig().getKeys(false);
                    if (!quizNames.contains(quizName)) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cInvalid Quiz: " + quizName));
                        return true;
                    }

                    List<Object> quizInfo = new ArrayList<>();
                    quizInfo.add(quizName);
                    quizInfo.add(1);

                    participants.put(player.getName(), quizInfo);
                    sendQuestions(quizName, 1, player);
                    return true;
                }
            }

            // "/quiz answer [QuestionNo] [AnswerNo]"
            if (args.length == 3 && sender instanceof Player) {
                Player player = (Player) sender;
                if (args[0].equalsIgnoreCase("answer") && participants.containsKey(player.getName())) {
                    String quizName = (String) participants.get(player.getName()).get(0);
                    int questionNo = Integer.parseInt(args[1]);
                    int currentQuestion = (int) participants.get(player.getName()).get(1);
                    String answerNo = args[2];

                    // Answered a previous question (do nothing)
                    if (questionNo != currentQuestion) {
                        return true;
                    }

                    // Add outcomes to quiz score
                    List<String> outcomes = plugin.getConfig().getStringList(quizName + "." + currentQuestion + ".answers." + answerNo + ".outcomes");

                    if (results.containsKey(player.getName())) {
                        results.get(player.getName()).addAll(outcomes);
                    } else {
                        results.put(player.getName(), outcomes);
                    }

                    // Load next question
                    int nextQuestion = currentQuestion + 1;
                    sendQuestions(quizName, nextQuestion, player);
                    return true;
                }
            }
        }
        return true;
    }

    // Format message using inputs from the config.yml
    public String cleanMessage(String message, String quizName, String questionNo, String question, String answerNo, String answer, String outcome) {
        message = message.replace("{quizName}", quizName);
        message = message.replace("{questionNo}", questionNo);
        message = message.replace("{question}", question);
        message = message.replace("{answerNo}", answerNo);
        message = message.replace("{answer}", answer);
        message = message.replace("{outcome}", outcome);
        return message;
    }

    public void sendQuestions(String quizName, int questionNo, Player player) {
        String theQuestion = plugin.getConfig().getString(quizName + "." + questionNo + ".question");
        if (theQuestion == null) {
            endQuiz(player, quizName);
            return;
        }

        for (int i = 0; i <= 20; i++) {
            player.sendMessage("");
        }

        // Send Question Title and Subtitle to Player
        String title = plugin.getConfig().getString("settings." + quizName + ".questionTitle", "Question " + questionNo);
        String subtitle = plugin.getConfig().getString("settings." + quizName + ".questionSubtitle", "Click the corresponding option in chat to answer");

        title = cleanMessage(title, quizName, Integer.toString(questionNo), theQuestion, "", "", "");
        subtitle = cleanMessage(subtitle, quizName, Integer.toString(questionNo), theQuestion, "", "", "");

        player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), ChatColor.translateAlternateColorCodes('&', subtitle), 10, 70, 20);

        // Send Question
        String questionMessage = plugin.getConfig().getString("settings." + quizName + ".questionFormat", "Question " + questionNo + ": " + theQuestion);
        questionMessage = cleanMessage(questionMessage, quizName, Integer.toString(questionNo), theQuestion, "", "", "");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', questionMessage));
        player.sendMessage("");

        // Send Answers
        plugin.getConfig().getConfigurationSection(quizName + "." + questionNo + ".answers").getKeys(false).forEach(answer -> {

            String answerStr = plugin.getConfig().getString(quizName + "." + questionNo + ".answers." + answer + ".answer");

            String answerMessage = plugin.getConfig().getString("settings." + quizName + ".answerFormat", "[" + answer + "] " + answerStr);
            answerMessage = cleanMessage(answerMessage, quizName, Integer.toString(questionNo), theQuestion, answer, answerStr, "");

            TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&', answerMessage));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quiz answer " + questionNo + " " + answer));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click here to select " + answerStr).color(ChatColor.GRAY).italic(true).create()
            ));
            player.spigot().sendMessage(message);
        });
        player.sendMessage("");

        // update participants to set current question as questionNo
        List<Object> quizInfo = new ArrayList<>();
        quizInfo.add(quizName);
        quizInfo.add(questionNo);

        participants.put(player.getName(), quizInfo);

        startQuestionTimer(questionNo, player);
    }

    public void startQuestionTimer(int questionNo, Player player) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (!participants.containsKey(player.getName())) {
                return;
            }

            String quizName = (String) participants.get(player.getName()).get(0);
            int currentQuestion = (int) participants.get(player.getName()).get(1);
            // they took over 60 seconds to answer
            if (questionNo == currentQuestion) {
                String endMessage = plugin.getConfig().getString("settings." + quizName + ".endMessage", "The quiz has ended.");

                player.sendMessage("");
                player.sendMessage("");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou took too long to respond to this question."));
                player.sendMessage("");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
                player.sendMessage("");
                player.sendMessage("");

                participants.remove(player.getName());
                results.remove(player.getName());
            }
        }, 1200);
    }

    public void endQuiz(Player player, String quizName) {
        for (int i = 0; i <= 20; i++) {
            player.sendMessage("");
        }

        String endMessage = plugin.getConfig().getString("settings." + quizName + ".endMessage", "The quiz has ended.");

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', endMessage));
        player.sendMessage("");

        List<String> responses = results.get(player.getName());

        int count = 1, tempCount;
        String popular = responses.get(0);
        String temp;
        for (int i = 0; i < (responses.size() - 1); i++) {
            temp = responses.get(i);
            tempCount = 0;
            for (int j = 1; j < responses.size(); j++) {
                if (temp.equals(responses.get(j))) {
                    tempCount++;
                }
            }
            if (tempCount > count) {
                popular = temp;
                count = tempCount;
            }
        }

        String outcomeMessage = plugin.getConfig().getString("settings." + quizName + ".outcomeMessage", "Your chosen outcome was: " + popular);
        outcomeMessage = cleanMessage(outcomeMessage, quizName, "", "", "", "", popular);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', outcomeMessage));
        player.sendMessage("");

        participants.remove(player.getName());
        results.remove(player.getName());
    }
}
