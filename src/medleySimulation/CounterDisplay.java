package medleySimulation;

import java.awt.Color;

import javax.swing.JLabel;

//You don't need to change this class
public class CounterDisplay  implements Runnable {
	
	private FinishCounter results;
	private JLabel win;
		
	CounterDisplay(JLabel w, FinishCounter score) {
        this.win=w;
        this.results=score;
    }
	
	public void run() { //this thread just updates the display of a text field
        while (true) {
        	//test changes colour when the race is won
        	if (results.isRaceWon()) {
        		win.setForeground(Color.RED);
               	win.setText("Winning Team: " + results.getWinningTeam() + "!!"); 
        	}
        	else {
        		win.setForeground(Color.BLACK);
        		win.setText("------"); 
        	}	
        }
    }
}
