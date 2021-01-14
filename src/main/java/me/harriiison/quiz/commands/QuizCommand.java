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

public class QuizCommand implements CommandExecutor {

    private ChatQuiz plugin;
    private Map<String, List<Object>> participants;
    private Map<String, List<String>> results;

    public QuizCommand(ChatQuiz instance) {
        this.plugin = instance;
        this.participants = new HashMap<>();
        this.results = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("quiz")) {

            // "/quiz"
            if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
                sender.sendMessage("");
                sender.sendMessage(cc("&3&lChat Quiz &r&7(v" + plugin.getDescription().getVersion() + ")"));
                sender.sendMessage(cc("&7Designed by Harrison#4651 <&n" + plugin.getDescription().getWebsite() + "&r&7>"));
                sender.sendMessage("");
                if (sender.hasPermission("quiz.admin")) {
                    sender.sendMessage(cc("&3&nHelp - Admin Commands"));
                    sender.sendMessage(cc("&b/quiz reload &r>> &eReload the quiz configuration and questions"));
                    sender.sendMessage(cc("&b/quiz start [Player] [QuizName] &r>> &eBegin a quiz for a player"));
                    sender.sendMessage(cc("&7To begin making a quiz, open the config.yml file located in the plugins directory"));
                    sender.sendMessage("");
                }
                return true;
            }

            // "/quiz reload"
            if (args.length == 1 && sender.hasPermission("quiz.admin")) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(cc( "&aSuccess. The quiz config has been reloaded."));
                }
                return true;
            }

            // "/quiz start [Player] [QuizName]"
            if (args.length == 3  && sender.hasPermission("quiz.admin")) {
                if (args[0].equalsIgnoreCase("start")) {
                    Player player = Bukkit.getPlayer(args[1]);
                    if (player == null) {
                        sender.sendMessage(cc("&cInvalid Player"));
                        return true;
                    }

                    if (participants.containsKey(player.getName())) {
                        sender.sendMessage(cc("&cThat player already has a quiz in progress. Please try again later."));
                        player.sendMessage(cc("&cYou are already taking a quiz. Please finish that one first."));
                        return true;
                    }

                    String quizName = args[2];

                    Set<String> quizNames = plugin.getConfig().getKeys(false);
                    if (!quizNames.contains(quizName)) {
                        sender.sendMessage(cc( "&cInvalid Quiz: " + quizName));
                        return true;
                    }

                    List<Object> quizStatus = new ArrayList<>();
                    quizStatus.add(quizName);
                    quizStatus.add(1);

                    participants.put(player.getName(), quizStatus);
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

    // Translate colour codes in a message
    public String cc(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
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

        player.sendTitle(cc(title), cc(subtitle), 10, 70, 20);

        // Send Question
        String questionMessage = plugin.getConfig().getString("settings." + quizName + ".questionFormat", "Question " + questionNo + ": " + theQuestion);
        questionMessage = cleanMessage(questionMessage, quizName, Integer.toString(questionNo), theQuestion, "", "", "");
        player.sendMessage(cc(questionMessage));
        player.sendMessage("");

        // Send Answers
        plugin.getConfig().getConfigurationSection(quizName + "." + questionNo + ".answers").getKeys(false).forEach(answer -> {

            String answerStr = plugin.getConfig().getString(quizName + "." + questionNo + ".answers." + answer + ".answer");

            String answerMessage = plugin.getConfig().getString("settings." + quizName + ".answerFormat", "[" + answer + "] " + answerStr);
            answerMessage = cleanMessage(answerMessage, quizName, Integer.toString(questionNo), theQuestion, answer, answerStr, "");

            TextComponent message = new TextComponent(cc(answerMessage));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quiz answer " + questionNo + " " + answer));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Click here to select " + answerStr).color(ChatColor.GRAY).italic(true).create()
            ));
            player.spigot().sendMessage(message);
        });
        player.sendMessage("");

        // update participants to set current question as questionNo
        List<Object> quizStatus = new ArrayList<>();
        quizStatus.add(quizName);
        quizStatus.add(questionNo);

        participants.put(player.getName(), quizStatus);

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
                player.sendMessage(cc("&cYou took too long to respond to this question."));
                player.sendMessage("");
                player.sendMessage(cc(endMessage));
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

        player.sendMessage(cc(endMessage));
        player.sendMessage("");

        List<String> responses = results.get(player.getName());

        // Find the most common occurrence in the responses string
        int count = 1;
        int tempCount;
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

        player.sendMessage(cc(outcomeMessage));
        player.sendMessage("");

        participants.remove(player.getName());
        results.remove(player.getName());
    }
}
