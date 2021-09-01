float _PLAYER_SPEED = 7;
boolean addI = false;
boolean addJ = false;

class Player extends MobParticle {
  Light light;
  
  int timer;
  float accel;
  float de_accel;
  
  float xAtkOffset = 2.1;
  float yAtkOffset = 0.3;
  float xAtkLen = 3.2;
  float yAtkLen = 1;
  
  int atkDur = frameInterval*2;
  int chargeTime = 0;
  
  boolean newAttack;
  
  Player(float xStart, float yStart, float pSpeed, float pSize, ArrayList<PImage[]> animations) {
    super(xStart, yStart, pSpeed, pSize, animations);
    
    light = new Light(x,y, lightColor,size*3, true);
    lights.add(light);
    
    sizeRatio = 13.7;
    xPosRatio = 27.16;
    yPosRatio = 25.08;
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
  
  Light getLight() { return light; }
  
  void update() {
    
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
  PImage[] getAnimations() {
    int stateNum = -1;
    switch(state){
      case ATTACK:
        stateNum = 0;
        xSpeed = 0;
        if (flip)
          xSpeed = -0.00001;
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
  
  PImage updateTexture() {
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
  
  void implementSkills(int frameNum) {
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
