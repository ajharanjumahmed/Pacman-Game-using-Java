package org.yourcompany.yourproject;
import javax.swing.JFrame;

public class App {
    public static void main(String[] args) {
        int rowCount = 21; //number of rows in the game map
        int columnCount = 19; //number of columns in the game map
        int tileSize = 32; //size of each block
        int boardWidth = columnCount * tileSize; //width of the map
        int boardHeight = rowCount * tileSize; //height of the map

        JFrame frame = new JFrame("Pacman Game"); //main frame of the game
        frame.setSize(boardWidth, boardHeight); //setting breadth and height of the main frame
        frame.setResizable(false); //making the main frame non-resizable
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //close the main frame on clicking cross sign
        frame.setLocationRelativeTo(null); //position the main frame on the middle of the frame
        
        PacMan pacmanGame = new PacMan(); //create an object of the PacMan class which extends JPanel
        frame.add(pacmanGame); //add the object to the main frame
        frame.pack(); //keep every elements inside the frame
        pacmanGame.requestFocus(); //listens the key presses

        frame.setVisible(true); //make the main frame visible
    }
} 