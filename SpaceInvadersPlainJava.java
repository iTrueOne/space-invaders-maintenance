import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpaceInvadersPlainJava extends JPanel implements ActionListener, KeyListener {

    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 800;

    private static final int PLAYER_START_X = 300;
    private static final int PLAYER_START_Y = 750;
    private static final int PLAYER_SIZE = 40;
    private static final int PLAYER_SPEED = 5;

    private static final int ENEMY_MOVE_SPEED = 2;
    private static final int ENEMY_DROP_DISTANCE = 20;

    private int enemyDirection = 1;
    
    private static final int ENEMY_COUNT = 5;
    private static final int ENEMY_START_X = 90;
    private static final int ENEMY_START_Y = 150;
    private static final int ENEMY_SIZE = 30;
    private static final int ENEMY_GAP = 100;

    private static final int BULLET_WIDTH = 5;
    private static final int BULLET_HEIGHT = 20;
    private static final int BULLET_SPEED = 5;
    private static final int PLAYER_BULLET_X_OFFSET = 18;
    private static final int ENEMY_BULLET_X_OFFSET = 12;
    private static final int ENEMY_BULLET_Y_OFFSET = 30;

    private Timer timer;
    private double t = 0;
    private int score = 0;

    private static final long PLAYER_SHOOT_COOLDOWN_MS = 300; // KAN-4: Controls how fast the player can shoot.
    private long lastPlayerShotTime = 0;

    private Sprite player;
    private List<Sprite> sprites = new ArrayList<>();

    public SpaceInvadersPlainJava() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        player = new Sprite(PLAYER_START_X, PLAYER_START_Y, PLAYER_SIZE, PLAYER_SIZE, "player", Color.BLUE);
        sprites.add(player);

        nextLevel();

        timer = new Timer(16, this);
        timer.start();
    }

    private void nextLevel() {
        for (int i = 0; i < ENEMY_COUNT; i++) {
            Sprite enemy = new Sprite(ENEMY_START_X + i * ENEMY_GAP, ENEMY_START_Y, ENEMY_SIZE, ENEMY_SIZE, "enemy", Color.RED);
            sprites.add(enemy);
        }
    }

    private void moveEnemies() {
    boolean shouldChangeDirection = false;

    for (Sprite s : sprites) {
        if (s.type.equals("enemy")) {
            s.x += ENEMY_MOVE_SPEED * enemyDirection;

            if (s.y + s.height >= player.y) {
            player.dead = true;
            }

            if (s.x <= 0 || s.x + s.width >= SCREEN_WIDTH) {
                shouldChangeDirection = true;
            }
        }
    }

    if (shouldChangeDirection) {
        enemyDirection *= -1;

        for (Sprite s : sprites) {
            if (s.type.equals("enemy")) {
                s.y += ENEMY_DROP_DISTANCE;
            }
        }
    }
}
    
    private void updateGame() {
        t += 0.016;
        
        moveEnemies();

        List<Sprite> newBullets = new ArrayList<>();

        for (Sprite s : sprites) {
            switch (s.type) {

                case "enemybullet":
                    s.moveDown();

                    if (s.getBounds().intersects(player.getBounds())) {
                        player.dead = true;
                        s.dead = true;
                    }
                    break;

                case "playerbullet":
                    s.moveUp();

                    for (Sprite enemy : sprites) {
                        if (!s.dead && enemy.type.equals("enemy")) {
                            if (s.getBounds().intersects(enemy.getBounds())) {
                                enemy.dead = true;
                                s.dead = true;
                                score += 10;
                            }
                        }    
                    }
                    
                    break;

                case "enemy":
                    if (t > 2) {
                        if (Math.random() < 0.3) {
                            newBullets.add(createBullet(s));
                        }
                    }
                    break;
            }
        }

        sprites.addAll(newBullets);

        Iterator<Sprite> iterator = sprites.iterator();
        while (iterator.hasNext()) {
            Sprite s = iterator.next();

            if (s.dead || s.y < 0 || s.y > SCREEN_HEIGHT) {
                iterator.remove();
            }
        }

        if (t > 2) {
            t = 0;
        }
    }

     private Sprite createBullet(Sprite who) {
         if (who.type.equals("player")) {
            return new Sprite(
                who.x + PLAYER_BULLET_X_OFFSET,
                who.y,
                BULLET_WIDTH,
                BULLET_HEIGHT,
                "playerbullet",
                Color.BLACK
            );
        } else {
            return new Sprite(
                who.x + ENEMY_BULLET_X_OFFSET,
                who.y + ENEMY_BULLET_Y_OFFSET,
                BULLET_WIDTH,
                BULLET_HEIGHT,
                "enemybullet",
                Color.BLACK
            );
        }    
    }

    private void shoot(Sprite who) {
    if (who.type.equals("player")) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastPlayerShotTime < PLAYER_SHOOT_COOLDOWN_MS) {
            return;
        }

        lastPlayerShotTime = currentTime;
    }

    sprites.add(createBullet(who));
  }
    private void resetGame() {
    sprites.clear();

    player = new Sprite(PLAYER_START_X, PLAYER_START_Y, PLAYER_SIZE, PLAYER_SIZE, "player", Color.BLUE);
    sprites.add(player);

    score = 0;
    t = 0;
    lastPlayerShotTime = 0;
    enemyDirection = 1;

    nextLevel();

    if (!timer.isRunning()) {
        timer.start();
    }

    requestFocusInWindow();
}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Sprite s : sprites) {
            g.setColor(s.color);
            g.fillRect(s.x, s.y, s.width, s.height);
        }

        g.setColor(Color.BLACK);
        g.drawString("Controls: A = Left, D = Right, SPACE = Shoot", 20, 20);
        g.drawString("Score: " + score, 20, 40);

        if (player.dead) {
            drawCenteredGameMessage(g, "GAME OVER", Color.RED, "Press R to restart");
            timer.stop();
            return;
        }

        if (allEnemiesDead()) {
            drawCenteredGameMessage(g, "YOU WIN!", new Color(0, 150, 0), "Press R to restart");
            timer.stop();
            return;
        }
    }

    private void drawCenteredGameMessage(Graphics g, String title, Color titleColor, String subtitle) {
    Graphics2D g2 = (Graphics2D) g;

    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    Font titleFont = new Font("Arial", Font.BOLD, 64);
    Font subtitleFont = new Font("Arial", Font.BOLD, 24);

    int centerX = getWidth() / 2;
    int centerY = getHeight() / 2;

    g2.setFont(titleFont);
    FontMetrics titleMetrics = g2.getFontMetrics();
    int titleX = centerX - titleMetrics.stringWidth(title) / 2;
    int titleY = centerY - 20;

    g2.setColor(titleColor);
    g2.drawString(title, titleX, titleY);

    g2.setFont(subtitleFont);
    FontMetrics subtitleMetrics = g2.getFontMetrics();
    int subtitleX = centerX - subtitleMetrics.stringWidth(subtitle) / 2;
    int subtitleY = titleY + 50;

    g2.setColor(Color.BLACK);
    g2.drawString(subtitle, subtitleX, subtitleY);
}

    private boolean allEnemiesDead() {
        for (Sprite s : sprites) {
            if (s.type.equals("enemy")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) {
            resetGame();
            repaint();
            return;
        }

        if (player.dead || allEnemiesDead()) {
            return;
        }

        switch (e.getKeyCode()) {
        case KeyEvent.VK_A:
        player.moveLeft();
        break;

        case KeyEvent.VK_D:
        player.moveRight();
        break;

        case KeyEvent.VK_SPACE:
        shoot(player);
        break;
    }

    player.keepInsideHorizontalBounds(0, SCREEN_WIDTH);
    repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    private static class Sprite {
        int x;
        int y;
        int width;
        int height;
        String type;
        Color color;
        boolean dead = false;

        Sprite(int x, int y, int width, int height, String type, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
            this.color = color;
        }

        Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
        
        void keepInsideHorizontalBounds(int minX, int maxX) {
            if (x < minX) {
                x = minX;
            }

            if (x + width > maxX) {
                x = maxX - width;
        }
    }

       void moveBy(int dx, int dy) {
            x += dx;
            y += dy;
        }    

        void moveLeft() {
            moveBy(-PLAYER_SPEED, 0);
        }

        void moveRight() {
            moveBy(PLAYER_SPEED, 0);
        }

        void moveUp() {
            moveBy(0, -BULLET_SPEED);
        }

        void moveDown() {
            moveBy(0, BULLET_SPEED);
        }
      }
    

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders - Plain Java Version");

        SpaceInvadersPlainJava game = new SpaceInvadersPlainJava();

        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
