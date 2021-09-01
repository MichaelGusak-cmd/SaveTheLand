import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Main extends PApplet {


SoundFile winLevelJingle;
SoundFile selection;
SoundFile music;

final int tileSize = 60; //bigger = more tiles per side
float freezeDuration = 2; //time in seconds
float lightDimSpeed = 0.9991f; //how quickly lights dim, closer to 1 == slower
float lightRange = tileSize*3;

int[][] goals = {{1,1,1,1},
                 {300,300,300,300},
                 {600,600,600,600}};
int levelCounter = 0;
boolean levelBeaten = false;
PFont font;
float freezeTime = freezeDuration*1000;   //time in millis

int _SCREEN_WIDTH = 900; //5:4 aspect ratio
int _SCREEN_HEIGHT = 720; 

PImage TEXTURE_BACKGROUND;
PImage menu;
MobParticle player;
World world;
boolean play = false;

int selectionNum = -1;
boolean selectSound = false;
ArrayList<Particle> particles = new ArrayList<Particle>();
ArrayList<Light> lights = new ArrayList<Light>();

ArrayList<PImage[]> flamerAnimations; 
ArrayList<PImage[]> slimeAnimations;
  
public void settings() {
  size(_SCREEN_WIDTH, _SCREEN_HEIGHT, P2D);
  //fullScreen(P2D, 2);//, monitor#);
}

public void setup() {
  
  _SCREEN_WIDTH = width;
  _SCREEN_HEIGHT = height;
  
  frameRate(60);
  colorMode(RGB, 1.0f); //color range, 0f..1f
  textureMode(NORMAL); //textures from 0f..1f
  textureWrap(REPEAT);
  noStroke();
  noiseDetail(1, 0.1f);
  
  String[] playerFilenames = { "idle", "run", "turn"};
  player = new Player(0,0, _SCREEN_HEIGHT/100, (int)(_SCREEN_HEIGHT/25), loadTextures("", playerFilenames, 32));
  particles.add(player);
  
  String[] worldFileNames = { "tilemap" };
  world = new World(loadTextures("", worldFileNames, 16).get(0), height/tileSize);
  
  //if pixel art:
  ((PGraphicsOpenGL)g).textureSampling(2);
  
  background(0);
}

final float fadeRate = 0.06f;
boolean clearScreen = false;
boolean firstLoad = true;
boolean victory = false;
float victoryTime;
float victoryMessageDuration = 20*1000; //10 seconds
int menuNum = 0;

public void load() {
  font = loadFont("LucidaConsole-8.vlw");
  textFont(font, 32);
  winLevelJingle = new SoundFile(this, "levelBeat.mp3");
  selection = new SoundFile(this, "selection.wav");
  music = new SoundFile(this, "music.mp3");
  menu = loadImage("images/menu.png");
  firstLoad = false;
}

public void draw() {
  if (firstLoad)
    load();
  if (!play) {
    image(menu, 0,0,width, height);
    if (menuNum == 0) {
      if (overRect(mouseX, mouseY, width*0.03f, height*0.4f, width*0.142f, height*0.37f, 1)) {
        if (mouseDown) {
          clearScreen = true;
          menuNum = 1;
        }
      }
      push();
      fill(0, 0, 0);
      text("By Mike G.", width*0.15f, height*0.365f);
      text("PLAY!", width*0.03f, height*0.4f);
      pop();
    }
    else if (menuNum == 1) {
      play = true;
      background(0);
      music.loop();
    }
  }
  else {
    if (levelBeaten) {
      music.pause();
      winLevelJingle.play();
      if (levelCounter < goals.length-1) {
        levelCounter++;
        freezeDuration/=4;
        freezeTime = freezeDuration*1000;
        player.changeSpeedBy(2);
        lightRange/=1.2f;
      }
      else {
        lights.clear();
        lights.add(((Player)player).getLight());
        if (!victory) {
          victory = true;
          victoryTime = millis();
        }
      }
      levelBeaten = false;
    }
    
    if (!winLevelJingle.isPlaying() && !music.isPlaying())
      music.loop();
    
    if (frameCount % 2 == 0) {
      push(); 
      fill(0,0,0, fadeRate);
      rect(0,0,width, height);
      pop();
    }
    
    world.draw();
    
    for (int i = 0; i < particles.size(); i++) {
      if (particles.get(i).isAlive() || particles.get(i) instanceof Player) {
        particles.get(i).update();
        particles.get(i).draw();
      }
      else {
        particles.remove(i);
      }
    }
    for (int i = 0; i < lights.size(); i++)
      if (lights.get(i).isAlive())
        lights.get(i).update();
      else {
        lights.remove(i);
      }
      
    if (victory && victoryTime+victoryMessageDuration-millis() > 0) {
        push();
        fill(1, 1, 1);
        text("Good Game, Well Played!\nYou have completed:\nSave The Land!\nBy Mike G.", width*0.3f, height*0.4f);
        pop();
    }
  }
}

public ArrayList<PImage[]> loadTextures(String folderName, String[] filenames, int numPixels) {
  ArrayList<PImage[]> textures = new ArrayList<PImage[]>();
  
  for (int i = 0; i < filenames.length; i++) {
    PImage tex = loadImage("images/"+folderName+"/"+filenames[i]+".png");
    PImage[] images = new PImage[tex.height/numPixels];
    
    for (int j = 0; j < images.length; j++) {
      images[j] = tex.get(0, j*numPixels, tex.width, numPixels);
    }
    textures.add(images);
  }
  return textures;
}

public boolean overRect(float x, float y, float a, float b, float c, float d, int num) {
  boolean out = false;
  if (x >= a && x <= c &&
      y <= b && y >= d) {
    selectSound = true;
    out = true;
    push();
      beginShape();
      fill(color(0.5f,0.5f,0.5f));
      vertex(a,b,0,0);
      vertex(c,b,1,0);
      vertex(c,d,1,1);
      
      vertex(a,b,0,0);
      vertex(c,d,1,1);
      vertex(a,d,0,1);
      endShape();
    pop();
    if (selectSound && selectionNum == -1) {
      selection.play();
      selectionNum = num;
    }
  }
  else {
    push();
      beginShape();
      fill(color(1,1,1));
      vertex(a,b,0,0);
      vertex(c,b,1,0);
      vertex(c,d,1,1);
      
      vertex(a,b,0,0);
      vertex(c,d,1,1);
      vertex(a,d,0,1);
      endShape();
    pop();
    if (selectionNum == num) {
      selectSound = false;
      selectionNum = -1;
    }
  }
  return out;
}
int lightColor = color(1,1,0);

class Light extends MobParticle {
  int c;
  float range;
  boolean playerLight;
  public Light(float x, float y, int col, float dist, boolean playerOwner) {
    super(x, y, 0.0f, 0.0f, new ArrayList<PImage[]>());
    c = col;
    range = dist;
    playerLight = playerOwner;
    alive = true;
  }
  
  public int getColor() { return c; }
  public float getRange() { return range; }
  
  public void setColor(int col) { c = col; }
  public void setRange(float r) { range = r; }
  
  public void zoom(float s) {
    range *= s;
  }
  
  public void update() {
    if (playerLight) {
      x = player.getX();
      y = player.getY();
    } else {
      range*=lightDimSpeed; //0.9995
    }
    if (range < height/tileSize) { alive = false; }
  }
  
  public void implementSkills(int n) {
  }
  
  public PImage[] getAnimations() {
      return null;
  }
  
  public PImage updateTexture() {
    PImage[] animations = getAnimations();
    if (state!=prevState) { frameNum = 0; }
    
    if (alive) {
      if (state==prevState) { //increment frameNum based on time
        if (frameBool && millis()%frameInterval < frameInterval/2) {
          frameBool = false;
          frameNum = (frameNum+1)%animations.length;
        }
        else if (!frameBool && millis()%frameInterval > frameInterval/2) {
          frameBool = true;
        }
      }
    }
    else { //show the last frame (of the death animation)
      frameNum = animations.length-1;
    }
    
    implementSkills(frameNum); 
    
    if (frameNum == animations.length-1) {
      lockState = false;
      if (health <= 0)
        alive = false;
    }
    return animations[frameNum];
  }
}
enum State {
  ATTACK,
  CHARGE, 
  DEATH,
  HIT,
  IDLE,
  RUN,
  TRANSITION_TO_CHARGE
}

abstract class Particle {
  State state;
  float x, y;
  float xSpeed, ySpeed;
  //Collision collision;
  boolean collide;
  int lifetime;
  int age;
  float size;
  float xSize;
  float ySize;
  boolean alive;
  
  
  PImage[] animations = {};
  boolean lockState;
  boolean flip;
  boolean frameBool;
  int frameNum;
  int frameInterval = 100;
  
  public float getX() { return x; }
  public float getY() { return y; }
  public float getXSize() { return xSize; }
  public float getYSize() { return ySize; }
  //public Collision getCollision() { return collision; }
  public boolean isAlive() { return alive; }
  //void collision(Particle p) {
  //  if (collision.collidesWith(p.getCollision()))
  //    collide = true;
  //}
  //void resetCollision() { collide = false; }
  
  public abstract void update();
  public abstract void draw();
  public abstract PImage updateTexture();
}

abstract class MobParticle extends Particle {
  
  float sizeRatio;
  float xPosRatio;
  float yPosRatio;
  
  float xhitboxSizeRatio;
  float yhitboxSizeRatio;
  
  float maxSpeed;
  int maxHealth = 1;
  int health;
  State prevState;
  ArrayList<PImage[]> animationsList;
  public MobParticle(float xPos, float yPos, float mobSpeed, float mobSize, ArrayList<PImage[]> animations) {
    x = xPos; 
    y = yPos;
    maxSpeed = mobSpeed;
    size = mobSize;
    xSize = mobSize;
    ySize = mobSize;
    animationsList = animations;
  }
  
  public void zoom(float s) { 
    size *= s;
    xSize *= s;
    ySize *= s;
  }
  public void changeHealth(int hp) { health -= hp; }
  public void changeSpeedBy(float newSpeed) { maxSpeed += newSpeed; } 
  public boolean moving() { return abs(xSpeed)+abs(ySpeed) < maxSpeed/3; }
  
  public void draw() {
    PImage texture = updateTexture();
    
    //plane(null, x-xSize/2, y-ySize/2, x+xSize/2, y+ySize/2);
    if (flip) {
      //flip texture pixels, Code segment from stackoverflow user: George Profenza, taken on May 1, 2021
      //code from: https://stackoverflow.com/questions/29334348/processing-mirror-image-over-x-axis
      PImage flipped = createImage(texture.width,texture.height,ARGB);//create a new image with the same dimensions
      for(int i = 0 ; i < flipped.pixels.length; i++){       //loop through each pixel
        int srcX = i % flipped.width;                        //calculate source(original) x position
        int dstX = flipped.width-srcX-1;                     //calculate destination(flipped) x position = (maximum-x-1)
        int y    = i / flipped.width;                        //calculate y coordinate
        flipped.pixels[y*flipped.width+dstX] = texture.pixels[i];//write the destination(x flipped) pixel based on the current pixel  
      }
      //y*width+x is to convert from x,y to pixel array index
      flipped.updatePixels();
      //end of copied code segment
      
      image(flipped, x-flipped.width*xSize/xPosRatio, y-flipped.height*ySize/yPosRatio, flipped.width*xSize/sizeRatio, flipped.height*ySize/sizeRatio);
    }
    else {
      image(texture, x-texture.width*xSize/xPosRatio, y-texture.height*ySize/yPosRatio, texture.width*xSize/sizeRatio, texture.height*ySize/sizeRatio);
    }
  }
  
  public abstract PImage[] getAnimations();
  public abstract void implementSkills(int n);
}
float _PLAYER_SPEED = 7;
boolean addI = false;
boolean addJ = false;

class Player extends MobParticle {
  Light light;
  
  int timer;
  float accel;
  float de_accel;
  
  float xAtkOffset = 2.1f;
  float yAtkOffset = 0.3f;
  float xAtkLen = 3.2f;
  float yAtkLen = 1;
  
  int atkDur = frameInterval*2;
  int chargeTime = 0;
  
  boolean newAttack;
  
  Player(float xStart, float yStart, float pSpeed, float pSize, ArrayList<PImage[]> animations) {
    super(xStart, yStart, pSpeed, pSize, animations);
    
    light = new Light(x,y, lightColor,size*3, true);
    lights.add(light);
    
    sizeRatio = 13.7f;
    xPosRatio = 27.16f;
    yPosRatio = 25.08f;
    alive = true;
    timer = -1;
    
    state = State.IDLE; //default state
    prevState = State.DEATH; //random state 
    
    flip = false; //horizontal direction
    frameNum = 0; //current frame index
    frameBool = false; //manager for showing the next frame
    
    xSpeed = 0;
    ySpeed = 0;
    accel = pSpeed/5;
    de_accel = 0.8f;
    health = maxHealth;
    
    updateTexture();
  }
  
  public Light getLight() { return light; }
  
  public void update() {
    
    prevState = state;
    if (health > 0) {
      if (collide) {
          state = State.HIT;
          charge = false;
          transitionToCharge = false;
          lockState = false;
      }
      else if (!lockState) {
        if (charge) {
          state = State.CHARGE;
        }
        else if (transitionToCharge)
          state = State.TRANSITION_TO_CHARGE;
        else if (move_left || move_right || move_up || move_down)
          state = State.RUN;
        else
          state = State.IDLE;
      }
      if (freezePlayer && timer < 0) {
      timer = millis();
      }
      if (timer+freezeTime - millis() < 0) {
        timer = -1;
        freezePlayer = false;
      }
      if (!freezePlayer) {
        if (state == State.RUN || state == State.IDLE || state == State.HIT) {
          if (move_left && move_right) {
            if (move_dir_horiz)
              xSpeed = constrain(xSpeed+accel,-maxSpeed,maxSpeed);
            else
              xSpeed = constrain(xSpeed-accel,-maxSpeed,maxSpeed);
          }
          else if (move_left)
            xSpeed = constrain(xSpeed-accel,-maxSpeed,maxSpeed);
          else if (move_right) 
            xSpeed = constrain(xSpeed+accel,-maxSpeed,maxSpeed);
          
          if (move_up && move_down) {
            if (move_dir_vert)
              ySpeed = constrain(ySpeed-accel,-maxSpeed,maxSpeed);
            else
              ySpeed = constrain(ySpeed+accel,-maxSpeed,maxSpeed);
          }
          if (move_up) {
            ySpeed = constrain(ySpeed-accel,-maxSpeed,maxSpeed);
          }
          if (move_down)
            ySpeed = constrain(ySpeed+accel,-maxSpeed,maxSpeed);
          }
        
        xSpeed *= de_accel;
        ySpeed *= de_accel;
        
        x = constrain(x+xSpeed,xSize/2, _SCREEN_WIDTH - xSize/2);
        y = constrain(y+ySpeed,ySize/2, _SCREEN_HEIGHT - ySize/2);
      }
      else { 
        state = State.IDLE;
        xSpeed = 0;
        ySpeed = 0;
      }
      if (xSpeed < 0)
        flip = true;
      else
        flip = false;
    }
    else
      state = State.DEATH;
  }
  public PImage[] getAnimations() {
    int stateNum = -1;
    switch(state){
      case ATTACK:
        stateNum = 0;
        xSpeed = 0;
        if (flip)
          xSpeed = -0.00001f;
        ySpeed = 0;
        break;
      case CHARGE:
        stateNum = 1;
        break;
      case DEATH:
        stateNum = 2;
        frameInterval = 400;
        break;
      case HIT:
        stateNum = 3;
        break;
      case IDLE:
        stateNum = 0;
        break;
      case RUN:
        stateNum = 1;
        break;
      case TRANSITION_TO_CHARGE:
        stateNum = 2;
        break;
    }
    return animationsList.get(stateNum);
  }
  
  public PImage updateTexture() {
    PImage[] animations = getAnimations();
    if (state!=prevState) { frameNum = 0; }
    
    if (alive) {
      if (state==prevState) { //increment frameNum based on time
        if (frameBool && millis()%frameInterval < frameInterval/2) {
          frameBool = false;
          frameNum = (frameNum+1)%animations.length;
          newAttack = true;
        }
        else if (!frameBool && millis()%frameInterval > frameInterval/2) {
          frameBool = true;
        }
      }
    }
    else { //show the last frame (of the death animation)
      frameNum = animations.length-1;
    }
    
    implementSkills(frameNum); 
    
    if (frameNum == animations.length-1) {
      lockState = false;
      if (health <= 0)
        alive = false;
    }
    
    return animations[frameNum];
  }
  
  public void implementSkills(int frameNum) {
  //THESE ARE THE SPRITE SPECIFIC FRAME SKILLS 
    switch(state){
      case ATTACK:
        break;
      case CHARGE:
        break;
      case DEATH:
        break;
      case HIT:
        break;
      case IDLE:
        break;
      case RUN:
        break;
      case TRANSITION_TO_CHARGE:
        if (frameNum == 1) {
          transitionToCharge = false;
          charge = true;
        }
        break;
    }
  }
}
final float noiseScale = 0.01f;
int xNoise = 0;
int yNoise = 0;
int noiseChange = 1;

class World {
  int[] tileCounter;
  PImage[] tileMap;
  int[][] worldMap;
  boolean[][] boolMap;
  float size;
  World(PImage[] map, float worldSize) {
    tileMap = map;
    tileCounter = new int[tileMap.length];
    for (int i = 0; i < tileCounter.length; i++) 
      tileCounter[i] = 0;
      
    size = worldSize;
    worldMap = new int[(int)(width/size)][(int)(height/size)];
    boolMap = new boolean[(int)(width/size)][(int)(height/size)];
    for (int i = 0; i < worldMap.length; i++) {
      for (int j = 0; j < worldMap[0].length; j++) {
        worldMap[i][j] = -1;
        boolMap[i][j] = false;
      }
    }
  }
  public void zoom(float s) {
    size *= s;
  }
  
  public float getSize() { return size; }
  
  public void draw() {
    if (frameCount % 1 == 0 && player.moving()) {
      xNoise+=1;
      yNoise+=1;
    }
    for (int x = 0; x < worldMap.length; x++) {
        for (int y = 0; y < worldMap[0].length; y++) {
            boolMap[x][y] = false;
        }
    }
    for (int i = 0; i < lights.size(); i++) {
      //only use box inscribing the light (x-range,y-range, x+range, y+range)
      int xPos = (int)(lights.get(i).getX()/size);
      int yPos = (int)(lights.get(i).getY()/size);
      int r = (int)(lights.get(i).getRange()/size);
      for (int x = xPos-r; x < xPos+r; x++) {
        for (int y = yPos-r; y < yPos+r; y++) {
          if (x >= 0 && y >= 0 && x < worldMap.length && y < worldMap[0].length) {
            if (((x-xPos)*(x-xPos) + (y-yPos)*(y-yPos)) < r*r) {
              boolMap[x][y] = true;
              if (worldMap[x][y] == -1) {
                float tile = tileMap.length*1.9f*noise((x+xNoise)*noiseScale, (y+yNoise)*noiseScale);
                worldMap[x][y] = (int)tile;
              }
              image(tileMap[worldMap[x][y]], x*size, y*size, size, size);
            }
          }
        }
      }
    }
    for (int x = 0; x < worldMap.length; x++) {
      for (int y = 0; y < worldMap[0].length; y++) {
        if (worldMap[x][y] != -1) {
          tileCounter[worldMap[x][y]]++;
        }
        if (!boolMap[x][y] && worldMap[x][y] != -1) {
          worldMap[x][y] = -1; 
        }
      }
    }
    boolean winLevel = true;
    push();
    fill(1, 1, 1);
    text("Level: "+(levelCounter+1), width*0.65f, height*0.05f);
    for (int i = 0; i < tileCounter.length; i++) {
      if (tileCounter[i] < goals[levelCounter][i])
        winLevel = false;
      String name = "error";
      if (i == 0) name = "Sand";
      else if (i == 1) name = "Stone";
      else if (i == 2) name = "Grass";
      else if (i == 3) name = "Life Stone";
      text(name+": "+tileCounter[i]+"/"+goals[levelCounter][i], width*0.50f, height*(0.05f*i)+height*0.1f);
     
      tileCounter[i] = 0;
    } 
    pop();  
    if (winLevel) {
      levelBeaten = true;
    }
  }
}
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

public void mousePressed() {
  mouseDown = true;
}
public void mouseReleased() {
  mouseDown = false;
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Main" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
