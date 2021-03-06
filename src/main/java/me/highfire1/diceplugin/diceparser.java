package me.highfire1.diceplugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static me.highfire1.diceplugin.Diceplugin.*;

public class diceparser implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("roll")) {

            String[] dice_output = dice_logic(args);

                // mode 0, aka error
            if (dice_output.length == 1) {
                sender.sendMessage(dice_output[0]);

                // mode 1, aka self
            } else if (dice_output[1].equals(default_mode_types.get(0))) {
                sender.sendMessage(dice_output[3] + dice_output[4]);
                sender.sendMessage("Total: " + dice_output[2]);

                // mode 2, aka to everyone
            } else if (dice_output[1].equals(default_mode_types.get(1))) {
                String sendername = sender.getName();
                String str1 = sendername + ", " + dice_output[3] + dice_output[4];
                String str2 = "Total: " + dice_output[2];

                // send to all players
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(str1);
                    online.sendMessage(str2);
                }

                // also send to console, if sender is console
                if (!(sender instanceof Player)) {
                    sender.sendMessage(str1);
                    sender.sendMessage(str2);
                }
            } else {
                sender.sendMessage("Something has gone catastrophically wrong. Dumping values: ");
                for (String val : dice_output) {
                    sender.sendMessage(val);
                }
            }

        }
        return true;
    }
    // TODO convert to hashmap + exceptions at some point
    // OUTPUTS
    // [0] - error       - if no error, blank
    // [1] - mode        - all / self
    // [2] - total       - 23
    // [3] - title       - Rolling:
    // [4] - dice rolls  - 1d20 (2, 4) + 23
    public static String[] dice_logic(String[] args) {
        // check if a mode was selected
        String mode = default_mode;
        boolean temp = false;
        String str1 = "";

        // default to 1d20 if "no" args provided
        if (args.length == 0 || !args[0].contains("d")) {
            String[] first =  new String[]{"1d20"};

            // from stackoverflow
            // adds d20 and args
            int length = first.length + args.length;
            String[] new_array = new String[length];
            System.arraycopy(first, 0, new_array, 0, first.length);
            System.arraycopy(args, 0, new_array, first.length, args.length);

            args = new_array;
        }

        // iterate through args while also building str1
        for (int i = 1; i < args.length; i++) {

            // iterate through all default modes
            for (String default_mode : default_mode_types) {
                if (args[i].equals(("-" + default_mode))) {
                    mode = default_mode;
                    temp = true;
                    break;
                }
            }
            // build to str1 if not parameter
            if (!temp) {
                str1 += args[i] + " ";
            }
            temp = false; // replacement for if/else
        }

        if (str1.length() > 0) {
            str1 = str1.substring(0, str1.length() - 1) + ": ";
        } else {
            str1 = "Rolling: ";
        }

        String title = str1;
        str1 = "";



        // build output string
        // use custom text if exists, else default to generic message
        //String str1 = (args.length >= 2 && (!param_exists || args.length >= 3)) ?
        //        String.join(" ", args).substring(1).substring(args[0].length()) + " : " :
        //        "Rolling: ";
        //str1.replace(" -" + mode, "");


        // Preprocess args
        // generates string list in the form of {"1d20", "+", "35"}
        // iterates through every char in args, generates a split at every operator
        // operators are +-*/^()
        ArrayList<String> dicereader = new ArrayList<>();
        StringBuilder temp_holder = new StringBuilder();

        for (char s : args[0].toCharArray()) {

            if ("+-*/^()".contains(Character.toString(s))) {
                if (temp_holder.length() > 0) {
                    dicereader.add(temp_holder.toString());
                    temp_holder = new StringBuilder(); // equivalent to clearing StringBuilder as setLength is bad
                }
                dicereader.add(String.valueOf(s));

            } else if (s != ' ') {
                temp_holder.append(s);
            }
        }
        dicereader.add(temp_holder.toString());

        int dicecount = 0;

        // Convert dice into numbers by iterating through dicereader
        for (int i = 0; i < dicereader.size(); i++) {
            String param = dicereader.get(i); // will be either a roll/num or operator e.g. 1d20, +, -, 15, etc.

            // if parameter looks like dice then try to roll it
            if (param.contains("d")) {


                // make d20 -> 1d20
                if (param.charAt(0) == 'd') {
                    param = "1".concat(param);
                }

                String[] dice_parts = param.split("d", 2);

                if (dice_parts[0].equals("") || dice_parts[1].equals("")) {
                    return new String[]{"Malformed input."};
                }
                // Catch bad args
                try {
                    Integer.parseInt(dice_parts[0]);
                    Integer.parseInt(dice_parts[1]);

                } catch (Exception e) {
                    return new String[]{"Malformed input."};
                }

                // Finally roll dice now that dice parts are clean
                str1 = str1.concat(param + " (");
                int dice_num = Integer.parseInt(dice_parts[0]);
                int dice_val = Integer.parseInt(dice_parts[1]);

                String tempstring = "";

                // max_dice_per_roll check
                dicecount += dice_num;
                if (dicecount > max_dice_per_roll) {
                    return new String[]{"Too many dice!"};
                }

                int total = 0;
                for (int j = 0; j < dice_num; j++) {
                    int roll = ThreadLocalRandom.current().nextInt(dice_val) + 1;
                    if (roll == dice_val || roll == 1) {
                        tempstring = tempstring.concat(ChatColor.BOLD + Integer.toString(roll) + ChatColor.RESET + ", ");
                    } else {
                        tempstring = tempstring.concat(roll + ", ");
                    }
                    total += roll;
                }
                // if string for dice is longer than max_char_per_dice, cut it
                if (tempstring.length() > max_char_per_dice) {
                    str1 += tempstring.substring(0, max_char_per_dice) + "...)";
                } else {
                    str1 += tempstring.substring(0, tempstring.length() - 2) + ")";
                }

                // replace dice string with dice "integer" to use in evaluation
                dicereader.set(i, Integer.toString(total));

                // if parameter not like dice then just add it to str1
            } else if (param.contains("(") || param.contains(")")) {
                str1 = str1.concat(param);

            } else {
                str1 = str1.concat(" " + param);
            }
        }

        // evaluate the expression to output a total, taking into account parentheses/math
        String total_;
        try {
            total_ = Integer.toString(math_eval(String.join("", dicereader)));
        } catch (Exception e) {
            return new String[]{"Math failed :/ Error: " + e.getMessage()};
        }

        // duplicate comment for convenience
        // [0] - error       - if no error, blank
        // [1] - mode        - all / self
        // [2] - total       - 23
        // [3] - title       - Rolling:
        // [4] - dice rolls  - 1d20 (2, 4) + 23

        return new String[]{"", mode, total_, title, str1};
    }




    // Courtesy of Boann from Stackoverflow @
    // https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
    public static int math_eval(final String str) {
        return (int) new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }
}
