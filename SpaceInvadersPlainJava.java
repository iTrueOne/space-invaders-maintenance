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
    

    private static final int INITIAL_LIVES = 3;
    private int lives = INITIAL_LIVES;

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
        setBackground(new Color(15, 18, 32));
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
                    lives--;
                    s.dead = true;

                    if (lives <= 0) {
                    player.dead = true;
                    }                        
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
    lives = INITIAL_LIVES;

    nextLevel();

    if (!timer.isRunning()) {
        timer.start();
    }

    requestFocusInWindow();
}

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        for (Sprite s : sprites) {
            g2.setColor(s.color);
            g2.fillRect(s.x, s.y, s.width, s.height);
        }

        drawHUD(g2);

        if (player.dead) {
            drawCenteredGameMessage(g2, "GAME OVER", Color.RED, "Press R to restart");
            timer.stop();
            return;
        }

        if (allEnemiesDead()) {
            drawCenteredGameMessage(g2, "YOU WIN!", new Color(0, 190, 0), "Press R to restart");
            timer.stop();
            return;
        }
    }
    
    private void drawHUD(Graphics2D g2) {
        int barX = 15;
        int barY = 15;
        int barW = getWidth() - 30;
        int barH = 90;

        // Main HUD background
        g2.setColor(new Color(20, 24, 38, 230));
        g2.fillRoundRect(barX, barY, barW, barH, 25, 25);

        // Top accent line
        g2.setColor(new Color(0, 180, 255));
        g2.fillRoundRect(barX, barY, barW, 6, 20, 20);

        // Game title
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(Color.WHITE);
        g2.drawString("SPACE INVADERS", barX + 20, barY + 32);

        // Controls text
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(210, 210, 210));
        g2.drawString("A / D = Move   |   SPACE = Shoot   |   R = Restart", barX + 20, barY + 55);

        // Score box
        drawInfoBox(g2, "SCORE", String.valueOf(score), barX + 20, barY + 62, 150, 20,
            new Color(255, 193, 7), new Color(255, 248, 225));

    // Lives box
    drawInfoBox(g2, "LIVES", String.valueOf(lives), barX + 190, barY + 62, 150, 20,
            new Color(76, 175, 80), new Color(232, 245, 233));
}

    private void drawInfoBox(Graphics2D g2, String label, String value, int x, int y, int w, int h,
                         Color accentColor, Color valueBg) {

        // Outer box
        g2.setColor(new Color(35, 40, 58));
        g2.fillRoundRect(x, y, w, h + 18, 18, 18);

        // Accent strip
        g2.setColor(accentColor);
        g2.fillRoundRect(x, y, w, 6, 12, 12);

        // Label
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(new Color(220, 220, 220));
        g2.drawString(label, x + 12, y + 20);

        // Value background
        g2.setColor(valueBg);
        g2.fillRoundRect(x + 85, y + 8, 50, 22, 12, 12);

        // Value text
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(25, 25, 25));
        g2.drawString(value, x + 102, y + 25);
    }

    private void drawCenteredGameMessage(Graphics g, String title, Color titleColor, String subtitle) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, getWidth(), getHeight());

        Font titleFont = new Font("Arial", Font.BOLD, 64);
        Font subtitleFont = new Font("Arial", Font.BOLD, 24);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Title background box
        int boxW = 420;
        int boxH = 140;
        int boxX = centerX - boxW / 2;
        int boxY = centerY - 90;

        g2.setColor(new Color(20, 24, 38, 235));
         g2.fillRoundRect(boxX, boxY, boxW, boxH, 30, 30);

        g2.setColor(titleColor);
        g2.fillRoundRect(boxX, boxY, boxW, 8, 20, 20);

        // Title
        g2.setFont(titleFont);
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleX = centerX - titleMetrics.stringWidth(title) / 2;
        int titleY = boxY + 68;

        g2.setColor(titleColor);
        g2.drawString(title, titleX, titleY);

        // Subtitle
        g2.setFont(subtitleFont);
        FontMetrics subtitleMetrics = g2.getFontMetrics();
        int subtitleX = centerX - subtitleMetrics.stringWidth(subtitle) / 2;
        int subtitleY = titleY + 42;

        g2.setColor(Color.WHITE);
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
