import processing.sound.*;
SoundFile winLevelJingle;
SoundFile selection;
SoundFile music;

final int tileSize = 60; //bigger = more tiles per side
float freezeDuration = 2; //time in seconds
float lightDimSpeed = 0.9991; //how quickly lights dim, closer to 1 == slower
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
  
void settings() {
  size(_SCREEN_WIDTH, _SCREEN_HEIGHT, P2D);
  //fullScreen(P2D, 2);//, monitor#);
}

void setup() {
  
  _SCREEN_WIDTH = width;
  _SCREEN_HEIGHT = height;
  
  frameRate(60);
  colorMode(RGB, 1.0f); //color range, 0f..1f
  textureMode(NORMAL); //textures from 0f..1f
  textureWrap(REPEAT);
  noStroke();
  noiseDetail(1, 0.1);
  
  String[] playerFilenames = { "idle", "run", "turn"};
  player = new Player(0,0, _SCREEN_HEIGHT/100, (int)(_SCREEN_HEIGHT/25), loadTextures("", playerFilenames, 32));
  particles.add(player);
  
  String[] worldFileNames = { "tilemap" };
  world = new World(loadTextures("", worldFileNames, 16).get(0), height/tileSize);
  
  //if pixel art:
  ((PGraphicsOpenGL)g).textureSampling(2);
  
  background(0);
}

final float fadeRate = 0.06;
boolean clearScreen = false;
boolean firstLoad = true;
boolean victory = false;
float victoryTime;
float victoryMessageDuration = 20*1000; //10 seconds
int menuNum = 0;

void load() {
  font = loadFont("LucidaConsole-8.vlw");
  textFont(font, 32);
  winLevelJingle = new SoundFile(this, "levelBeat.mp3");
  selection = new SoundFile(this, "selection.wav");
  music = new SoundFile(this, "music.mp3");
  menu = loadImage("images/menu.png");
  firstLoad = false;
}

void draw() {
  if (firstLoad)
    load();
  if (!play) {
    image(menu, 0,0,width, height);
    if (menuNum == 0) {
      if (overRect(mouseX, mouseY, width*0.03, height*0.4, width*0.142, height*0.37, 1)) {
        if (mouseDown) {
          clearScreen = true;
          menuNum = 1;
        }
      }
      push();
      fill(0, 0, 0);
      text("By Mike G.", width*0.15, height*0.365);
      text("PLAY!", width*0.03, height*0.4);
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
        lightRange/=1.2;
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
        text("Good Game, Well Played!\nYou have completed:\nSave The Land!\nBy Mike G.", width*0.3, height*0.4);
        pop();
    }
  }
}

ArrayList<PImage[]> loadTextures(String folderName, String[] filenames, int numPixels) {
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

boolean overRect(float x, float y, float a, float b, float c, float d, int num) {
  boolean out = false;
  if (x >= a && x <= c &&
      y <= b && y >= d) {
    selectSound = true;
    out = true;
    push();
      beginShape();
      fill(color(0.5,0.5,0.5));
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
