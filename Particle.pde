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
  
  void zoom(float s) { 
    size *= s;
    xSize *= s;
    ySize *= s;
  }
  public void changeHealth(int hp) { health -= hp; }
  public void changeSpeedBy(float newSpeed) { maxSpeed += newSpeed; } 
  boolean moving() { return abs(xSpeed)+abs(ySpeed) < maxSpeed/3; }
  
  void draw() {
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
  
  abstract PImage[] getAnimations();
  abstract void implementSkills(int n);
}
