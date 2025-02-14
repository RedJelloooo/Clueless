package ui;

import javax.swing.*;
import java.awt.*;

/**
 * @author rogelio
 * This is a swimg component method to display a leaderboard whenever a user wishes to
 * see the current leaderboard. The input is a string seperates by new lines along
 * with the placement based on score
 */
public class Leaderboard extends JFrame{

    public Leaderboard(String inputString){
        GridLayout layout = new GridLayout(10, 1);
        setLayout(layout);

        String[] strArr = inputString.split("\n");

        if (!strArr[0].isEmpty()) {
            for(int x = 0; x < strArr.length ; x++){
                if (x < 10) {
                    JLabel name = new JLabel(x+1 + ". " + strArr[x]);
                    add(name);
                    name.setHorizontalAlignment(SwingConstants.LEFT);
                }
            }
        } else {
            JLabel name = new JLabel("No entries yet.");
            add(name);
            name.setHorizontalAlignment(SwingConstants.CENTER);
        }
    }
}
