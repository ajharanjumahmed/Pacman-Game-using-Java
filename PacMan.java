package org.yourcompany.yourproject;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class PacMan extends JPanel implements ActionListener, KeyListener { 
    private int rows = 21; // number of rows in panel
    private int columns = 19; // number of columns in panel
    private int tileSize = 32; // size of each block in panel
    private int panelWidth = columns * tileSize; // width of panel
    private int panelHeight = rows * tileSize; // height of panel

    private Image wallImage; // image for wall
    private Image redGhostImage;
    private Image FoodImage;
    private Image pinkGhostImage;
    private Image pacmanUpImage;
    private Image pacmanRightImage;
    private Image pacmanLeftImage;
    private Image pacmanDownImage;
    private Image orangeGhostImage;
    private Image blueGhostImage;

    class Block { // class for creating blocks
        int x; // position on x axis (columns)
        int y; // position on y axis (rows)
        int width; // block width
        int height; // block height
        Image image; // image to display in the block
        int startX; // starting x position
        int startY; // starting y position

        // current movement direction and velocity
        char direction = 'U'; // current direction: U=up, D=down, R=right, L=left
        char intendedDirection = 'U'; // the desired direction from key input
        int velocityX = 0; // velocity along x axis
        int velocityY = 0; // velocity along y axis

        Block(Image image, int x, int y, int width, int height) { // constructor
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
            this.direction = 'U'; // default starting direction
            this.intendedDirection = this.direction;
        }
        
        // This method is used when an immediate direction change is needed
        void updateDirection(char direction) {
            char prevDirection = this.direction; // store previous direction
            this.direction = direction;
            updateVelocity();
            // Check collision after moving a step
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }
        
        // Update velocity based on current direction
        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }
        
        // Reset block to its starting position
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // Array representing the game map (X = Wall, space = Food, b,p,o,r = ghosts, P = Pac-Man)
    private String[] gameMap = { 
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X       bpo       X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;   // to store wall blocks
    HashSet<Block> ghosts;  // to store ghost blocks
    HashSet<Block> foods;   // to store food blocks
    Block pacman;           // PacMan block

    Timer gameLoop;         // game loop timer
    char[] directions = {'U', 'D', 'L', 'R'}; // directions for ghost random movement
    Random randon = new Random(); // random generator
    int score = 0;         // player's score
    int lives = 3;         // player's lives
    boolean gameOver = false; // game over flag

    public PacMan() { // Constructor
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.BLACK);

        // Load images for walls, ghosts, and Pac-Man
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap(); // initialize the map elements

        // Initialize ghost directions randomly
        for (Block ghost : ghosts) {
            char newDirection = directions[randon.nextInt(4)];
            ghost.updateDirection(newDirection);
        }

        gameLoop = new Timer(50, this); // 20 fps (1000/50)
        gameLoop.start();
        addKeyListener(this);
        setFocusable(true);
    }

    // Load the map elements (walls, ghosts, food, Pac-Man) from gameMap
    public void loadMap() {
        walls = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        foods = new HashSet<Block>();

        for (int r = 0; r < rows; r++) {
            String row = gameMap[r];
            for (int c = 0; c < columns; c++) {
                char charAtRow = row.charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                if (charAtRow == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                } else if (charAtRow == 'b') {
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (charAtRow == 'o') {
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (charAtRow == 'p') {
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (charAtRow == 'r') {
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                } else if (charAtRow == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                    // Initialize Pac-Man’s intended direction same as its starting direction.
                    pacman.intendedDirection = 'U';
                    pacman.direction = 'U';
                    pacman.updateVelocity();
                } else if (charAtRow == ' ') {
                    // Food: a small dot centered in the tile
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    // Override paintComponent to draw game elements
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Draw all game elements
    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Display score and lives
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " +score, tileSize/2, tileSize/2);
        } else {
            g.drawString("x" + lives + "  Score: " +score, tileSize/2, tileSize/2);
        }
    }

    // move() method to smoothly update Pac-Man's direction.
    public void move() {
        // Attempt to change direction smoothly if the intended direction differs.
        if (pacman.intendedDirection != pacman.direction) {
            int savedX = pacman.x;
            int savedY = pacman.y;
            char origDirection = pacman.direction;
            int origVx = pacman.velocityX;
            int origVy = pacman.velocityY;

            // Temporarily update to the intended direction.
            pacman.direction = pacman.intendedDirection;
            pacman.updateVelocity();
            pacman.x += pacman.velocityX;
            pacman.y += pacman.velocityY;

            boolean canChange = true;
            for (Block wall : walls) {
                if (collision(pacman, wall)) {
                    canChange = false;
                    break;
                }
            }
            // Revert to saved position.
            pacman.x = savedX;
            pacman.y = savedY;
            if (!canChange) {
                // If collision, revert to original direction.
                pacman.direction = origDirection;
                pacman.velocityX = origVx;
                pacman.velocityY = origVy;
            } else {
                // Change accepted; update Pac-Man image based on new direction.
                updatePacmanImage();
            }
        }

        // Move Pac-Man by its current velocity.
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Check for wall collision and revert if needed.
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Ghost collisions
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }
            // If ghost is stuck on the middle line, adjust its direction.
            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[randon.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        // Food collision and score update.
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
                break;
            }
        }
        if (foodEaten != null) {
            foods.remove(foodEaten);
        }

        // If all food is eaten, reload the map.
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    // Update Pac-Man's image based on its current direction.
    private void updatePacmanImage() {
        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        } else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        } else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        } else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }

    // Collision detection between two blocks.
    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    // Reset positions for Pac-Man and ghosts.
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[randon.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        
    }

    @Override
    public void keyPressed(KeyEvent e) {
        
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // If game over, reset everything on any key press.
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // set the intendedDirection on key press
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.intendedDirection = 'U';
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.intendedDirection = 'D';
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.intendedDirection = 'L';
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.intendedDirection = 'R';
        }
    }
}
