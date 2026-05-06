import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SpaceInvadersPlainJava extends JPanel implements ActionListener, KeyListener {

    private static final int SCREEN_WIDTH = 600;
    private static final int SCREEN_HEIGHT = 800;
    private static final int PLAYER_SPEED = 5;
    private static final int BULLET_SPEED = 5;
    private static final int ENEMY_COUNT = 5;
    private static final int ENEMY_START_X = 90;
    private static final int ENEMY_START_Y = 150;
    private static final int ENEMY_GAP = 100;

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

        player = new Sprite(300, 750, 40, 40, "player", Color.BLUE);
        sprites.add(player);

        nextLevel();

        timer = new Timer(16, this);
        timer.start();
    }

    private void nextLevel() {
        for (int i = 0; i < ENEMY_COUNT; i++) {
            Sprite enemy = new Sprite(ENEMY_START_X + i * ENEMY_GAP, ENEMY_START_Y, 30, 30, "enemy", Color.RED);
            sprites.add(enemy);
        }
    }

    private void updateGame() {
        t += 0.016;

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
            return new Sprite(who.x + 18, who.y, 5, 20, "playerbullet", Color.BLACK);
        } else {
            return new Sprite(who.x + 12, who.y + 30, 5, 20, "enemybullet", Color.BLACK);
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
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.RED);
            g.drawString("GAME OVER", 180, 400);
            timer.stop();
        }

        if (allEnemiesDead()) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.setColor(Color.GREEN);
            g.drawString("YOU WIN!", 220, 400);
            timer.stop();
        }
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
        if (player.dead) {
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
