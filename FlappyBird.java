import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    int boardWidth = 360;
    int boardHeight = 640;

    // Parallax background images
    private Image parallaxSky;
    private Image parallaxFarMountains;
    private Image parallaxMountains;
    private Image parallaxTrees;
    private Image parallaxForegroundTrees;

    // Parallax positions
    private int skyX = 0;
    private int farMountainsX = 0;
    private int mountainsX = 0;
    private int treesX = 0;
    private int foregroundTreesX = 0;

    // Parallax speeds
    private final int skySpeed = -1;
    private final int farMountainsSpeed = -2;
    private final int mountainsSpeed = -3;
    private final int treesSpeed = -4;
    private final int foregroundTreesSpeed = -5;

    // images
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // bird class
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // pipe class
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64; // scaled by 1/6
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // game logic
    Bird bird;
    int velocityX = -4; // move pipes to the left speed (simulates bird moving right)
    int velocityY = 0; // move bird up/down speed.
    int gravity = 1;

    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;
    boolean gameOver = false;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // load images
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();
        // Load parallax background images
        parallaxSky = new ImageIcon(getClass().getResource("/layers/parallax-mountain-bg.png")).getImage();
        parallaxFarMountains = new ImageIcon(getClass().getResource("/layers/parallax-mountain-montain-far.png"))
                .getImage();
        parallaxMountains = new ImageIcon(getClass().getResource("/layers/parallax-mountain-mountains.png")).getImage();
        parallaxTrees = new ImageIcon(getClass().getResource("/layers/parallax-mountain-trees.png")).getImage();
        parallaxForegroundTrees = new ImageIcon(
                getClass().getResource("/layers/parallax-mountain-foreground-trees.png")).getImage();

        // bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();

        // place pipes timer
        placePipeTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        placePipeTimer.start();

        // game timer
        gameLoop = new Timer(1000 / 60, this); // 60 FPS
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw parallax background layers
        g.drawImage(parallaxSky, skyX, 0, boardWidth, boardHeight, null);
        g.drawImage(parallaxSky, skyX + boardWidth, 0, boardWidth, boardHeight, null);

        g.drawImage(parallaxFarMountains, farMountainsX, 0, boardWidth, boardHeight, null);
        g.drawImage(parallaxFarMountains, farMountainsX + boardWidth, 0, boardWidth, boardHeight, null);

        g.drawImage(parallaxMountains, mountainsX, 0, boardWidth, boardHeight, null);
        g.drawImage(parallaxMountains, mountainsX + boardWidth, 0, boardWidth, boardHeight, null);

        g.drawImage(parallaxTrees, treesX, 0, boardWidth, boardHeight, null);
        g.drawImage(parallaxTrees, treesX + boardWidth, 0, boardWidth, boardHeight, null);

        g.drawImage(parallaxForegroundTrees, foregroundTreesX, 0, boardWidth, boardHeight, null);
        g.drawImage(parallaxForegroundTrees, foregroundTreesX + boardWidth, 0, boardWidth, boardHeight, null);

        draw(g); // Draw the bird and pipes here
    }

    public void draw(Graphics g) {
        // Draw the bird
        g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);

        // Draw the pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Draw the score
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf((int) score), 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    public void move() {
        // bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0); // apply gravity to current bird.y, limit the bird.y to top of the canvas

        // pipes
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;

            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5; // 0.5 because there are 2 pipes! so 0.5*2 = 1, 1 for each set of pipes
                pipe.passed = true;
            }

            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }

        if (bird.y > boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && // a's top left corner doesn't reach b's top right corner
                a.x + a.width > b.x && // a's top right corner passes b's top left corner
                a.y < b.y + b.height && // a's top left corner doesn't reach b's bottom left corner
                a.y + a.height > b.y; // a's bottom left corner passes b's top left corner
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();

        // Update parallax positions
        skyX += skySpeed;
        farMountainsX += farMountainsSpeed;
        mountainsX += mountainsSpeed;
        treesX += treesSpeed;
        foregroundTreesX += foregroundTreesSpeed;

        // Wrap layers to create infinite scrolling effect
        if (skyX <= -boardWidth)
            skyX = 0;
        if (farMountainsX <= -boardWidth)
            farMountainsX = 0;
        if (mountainsX <= -boardWidth)
            mountainsX = 0;
        if (treesX <= -boardWidth)
            treesX = 0;
        if (foregroundTreesX <= -boardWidth)
            foregroundTreesX = 0;

        repaint();

        if (gameOver) {
            placePipeTimer.stop();
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocityY = -9;

            if (gameOver) {
                bird.y = boardHeight / 2;
                velocityY = 0;
                pipes.clear();
                score = 0;
                gameOver = false;
                placePipeTimer.start();
                gameLoop.start();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

/*
 * import java.awt.*;
 * import java.awt.event.*;
 * import java.util.ArrayList;
 * import java.util.Random;
 * import javax.swing.*;
 * 
 * public class FlappyBird extends JPanel implements ActionListener, KeyListener
 * {
 * int boardWidth = 360;
 * int boardHeight = 640;
 * 
 * // Parallax background images
 * private Image parallaxSky;
 * private Image parallaxFarMountains;
 * private Image parallaxMountains;
 * private Image parallaxTrees;
 * private Image parallaxForegroundTrees;
 * 
 * // Parallax positions
 * private int skyX = 0;
 * private int farMountainsX = 0;
 * private int mountainsX = 0;
 * private int treesX = 0;
 * private int foregroundTreesX = 0;
 * 
 * // Parallax speeds
 * private final int skySpeed = -1;
 * private final int farMountainsSpeed = -2;
 * private final int mountainsSpeed = -3;
 * private final int treesSpeed = -4;
 * private final int foregroundTreesSpeed = -5;
 * 
 * // Images
 * Image birdImg;
 * Image topPipeImg;
 * Image bottomPipeImg;
 * 
 * // Bird class
 * int birdX = boardWidth / 8;
 * int birdY = boardHeight / 2;
 * int birdWidth = 34;
 * int birdHeight = 24;
 * 
 * class Bird {
 * int x = birdX;
 * int y = birdY;
 * int width = birdWidth;
 * int height = birdHeight;
 * Image img;
 * 
 * Bird(Image img) {
 * this.img = img;
 * }
 * }
 * 
 * // Pipe class
 * int pipeX = boardWidth;
 * int pipeY = 0;
 * int pipeWidth = 64; // scaled by 1/6
 * int pipeHeight = 512;
 * 
 * class Pipe {
 * int x = pipeX;
 * int y = pipeY;
 * int width = pipeWidth;
 * int height = pipeHeight;
 * Image img;
 * boolean passed = false;
 * 
 * Pipe(Image img) {
 * this.img = img;
 * }
 * }
 * 
 * // Game logic
 * Bird bird;
 * int velocityX = -4; // move pipes to the left speed (simulates bird moving
 * right)
 * int velocityY = 0; // move bird up/down speed
 * int gravity = 1;
 * 
 * ArrayList<Pipe> pipes;
 * Random random = new Random();
 * 
 * Timer gameLoop;
 * Timer placePipeTimer;
 * boolean gameOver = false;
 * double score = 0;
 * 
 * FlappyBird() {
 * setPreferredSize(new Dimension(boardWidth, boardHeight));
 * setFocusable(true);
 * addKeyListener(this);
 * 
 * // Load images
 * birdImg = new
 * ImageIcon(getClass().getResource("./flappybird.png")).getImage();
 * topPipeImg = new
 * ImageIcon(getClass().getResource("./toppipe.png")).getImage();
 * bottomPipeImg = new
 * ImageIcon(getClass().getResource("./bottompipe.png")).getImage();
 * // Load parallax background images
 * parallaxSky = new
 * ImageIcon(getClass().getResource("/layers/parallax-mountain-bg.png")).
 * getImage();
 * parallaxFarMountains = new
 * ImageIcon(getClass().getResource("/layers/parallax-mountain-montain-far.png")
 * )
 * .getImage();
 * parallaxMountains = new
 * ImageIcon(getClass().getResource("/layers/parallax-mountain-mountains.png")).
 * getImage();
 * parallaxTrees = new
 * ImageIcon(getClass().getResource("/layers/parallax-mountain-trees.png")).
 * getImage();
 * parallaxForegroundTrees = new ImageIcon(
 * getClass().getResource("/layers/parallax-mountain-foreground-trees.png")).
 * getImage();
 * 
 * // Initialize bird
 * bird = new Bird(birdImg);
 * pipes = new ArrayList<Pipe>();
 * 
 * // Timer for placing pipes
 * placePipeTimer = new Timer(1500, new ActionListener() {
 * 
 * @Override
 * public void actionPerformed(ActionEvent e) {
 * placePipes();
 * }
 * });
 * placePipeTimer.start();
 * 
 * // Game timer
 * gameLoop = new Timer(1000 / 60, this); // 60 FPS
 * gameLoop.start();
 * }
 * 
 * void placePipes() {
 * int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight
 * / 2));
 * int openingSpace = boardHeight / 4;
 * 
 * Pipe topPipe = new Pipe(topPipeImg);
 * topPipe.y = randomPipeY;
 * pipes.add(topPipe);
 * 
 * Pipe bottomPipe = new Pipe(bottomPipeImg);
 * bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
 * pipes.add(bottomPipe);
 * }
 * 
 * @Override
 * public void paintComponent(Graphics g) {
 * super.paintComponent(g);
 * 
 * // Draw parallax background layers
 * drawParallaxLayer(g, parallaxSky, skyX);
 * drawParallaxLayer(g, parallaxFarMountains, farMountainsX);
 * drawParallaxLayer(g, parallaxMountains, mountainsX);
 * drawParallaxLayer(g, parallaxTrees, treesX);
 * drawParallaxLayer(g, parallaxForegroundTrees, foregroundTreesX);
 * 
 * draw(g); // Draw the bird and pipes here
 * }
 * 
 * private void drawParallaxLayer(Graphics g, Image image, int xOffset) {
 * int imageWidth = image.getWidth(null);
 * int imageHeight = image.getHeight(null);
 * 
 * double aspectRatio = (double) imageWidth / imageHeight;
 * int drawHeight = boardHeight;
 * int drawWidth = (int) (drawHeight * aspectRatio);
 * 
 * // Draw the image twice to create the parallax effect
 * g.drawImage(image, xOffset, 0, drawWidth, drawHeight, null);
 * g.drawImage(image, xOffset + drawWidth, 0, drawWidth, drawHeight, null);
 * }
 * 
 * public void draw(Graphics g) {
 * // Draw the bird
 * g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
 * 
 * // Draw the pipes
 * for (Pipe pipe : pipes) {
 * g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
 * }
 * 
 * // Draw the score
 * g.setColor(Color.white);
 * g.setFont(new Font("Arial", Font.PLAIN, 32));
 * if (gameOver) {
 * g.drawString("Game Over: " + (int) score, 10, 35);
 * } else {
 * g.drawString(String.valueOf((int) score), 10, 35);
 * }
 * }
 * 
 * public void move() {
 * // Bird movement
 * velocityY += gravity;
 * bird.y += velocityY;
 * bird.y = Math.max(bird.y, 0); // Apply gravity and limit the bird.y to the
 * top of the canvas
 * 
 * // Pipe movement
 * for (Pipe pipe : pipes) {
 * pipe.x += velocityX;
 * 
 * if (!pipe.passed && bird.x > pipe.x + pipe.width) {
 * score += 0.5; // Score increment
 * pipe.passed = true;
 * }
 * 
 * if (collision(bird, pipe)) {
 * gameOver = true;
 * }
 * }
 * 
 * if (bird.y > boardHeight) {
 * gameOver = true;
 * }
 * }
 * 
 * boolean collision(Bird a, Pipe b) {
 * return a.x < b.x + b.width && // a's top left corner doesn't reach b's top
 * right corner
 * a.x + a.width > b.x && // a's top right corner passes b's top left corner
 * a.y < b.y + b.height && // a's top left corner doesn't reach b's bottom left
 * corner
 * a.y + a.height > b.y; // a's bottom left corner passes b's top left corner
 * }
 * 
 * @Override
 * public void actionPerformed(ActionEvent e) {
 * move();
 * 
 * // Update parallax positions
 * skyX += skySpeed;
 * farMountainsX += farMountainsSpeed;
 * mountainsX += mountainsSpeed;
 * treesX += treesSpeed;
 * foregroundTreesX += foregroundTreesSpeed;
 * 
 * // Wrap layers to create infinite scrolling effect
 * if (skyX <= -boardWidth)
 * skyX = 0;
 * if (farMountainsX <= -boardWidth)
 * farMountainsX = 0;
 * if (mountainsX <= -boardWidth)
 * mountainsX = 0;
 * if (treesX <= -boardWidth)
 * treesX = 0;
 * if (foregroundTreesX <= -boardWidth)
 * foregroundTreesX = 0;
 * 
 * repaint();
 * 
 * if (gameOver) {
 * placePipeTimer.stop();
 * gameLoop.stop();
 * }
 * }
 * 
 * @Override
 * public void keyPressed(KeyEvent e) {
 * if (e.getKeyCode() == KeyEvent.VK_SPACE) {
 * velocityY = -9;
 * 
 * if (gameOver) {
 * bird.y = boardHeight / 2;
 * velocityY = 0;
 * pipes.clear();
 * score = 0;
 * gameOver = false;
 * placePipeTimer.start();
 * gameLoop.start();
 * }
 * }
 * }
 * 
 * @Override
 * public void keyReleased(KeyEvent e) {
 * }
 * 
 * @Override
 * public void keyTyped(KeyEvent e) {
 * }
 * 
 * public static void main(String[] args) {
 * JFrame frame = new JFrame("Flappy Bird");
 * FlappyBird game = new FlappyBird();
 * frame.add(game);
 * frame.pack();
 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 * frame.setVisible(true);
 * }
 * }
 */
