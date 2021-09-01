color lightColor = color(1,1,0);

class Light extends MobParticle {
  color c;
  float range;
  boolean playerLight;
  public Light(float x, float y, color col, float dist, boolean playerOwner) {
    super(x, y, 0.0, 0.0, new ArrayList<PImage[]>());
    c = col;
    range = dist;
    playerLight = playerOwner;
    alive = true;
  }
  
  color getColor() { return c; }
  float getRange() { return range; }
  
  void setColor(color col) { c = col; }
  void setRange(float r) { range = r; }
  
  void zoom(float s) {
    range *= s;
  }
  
  void update() {
    if (playerLight) {
      x = player.getX();
      y = player.getY();
    } else {
      range*=lightDimSpeed; //0.9995
    }
    if (range < height/tileSize) { alive = false; }
  }
  
  void implementSkills(int n) {
  }
  
  PImage[] getAnimations() {
      return null;
  }
  
  PImage updateTexture() {
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
