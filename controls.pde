final char KEY_LEFT = 'a';
final char KEY_RIGHT = 'd';
final char KEY_UP = 'w';
final char KEY_DOWN = 's';
final char KEY_ATTACK = ' ';

boolean move_dir_horiz = false;
boolean move_dir_vert = false;
boolean move_left;
boolean move_right;
boolean move_up;
boolean move_down;

boolean mouseDown = false;

boolean freezePlayer;
boolean charge;
boolean transitionToCharge;

public void keyPressed()
{
  key = Character.toLowerCase(key);
  if (key == KEY_ATTACK && !freezePlayer) {
    freezePlayer = true;
    lights.add(new Light(player.getX(), player.getY(), lightColor, lightRange, false));
  }
  if (key == KEY_LEFT) {
    move_left = true;
    move_dir_horiz = false;
  }
  if (key == KEY_RIGHT) {
    move_right = true;
    move_dir_horiz = true;
  }
  if (key == KEY_UP) {
    move_up = true;
    move_dir_vert = true;
  }
  if (key == KEY_DOWN) {
    move_down = true;
    move_dir_vert = false;
  }
}

public void keyReleased() {
  key = Character.toLowerCase(key);
  if (key == KEY_LEFT)
    move_left = false;
  if (key == KEY_RIGHT)
    move_right = false;
  if (key == KEY_UP)
    move_up = false;
  if (key == KEY_DOWN)
    move_down = false;
}

void mousePressed() {
  mouseDown = true;
}
void mouseReleased() {
  mouseDown = false;
}
