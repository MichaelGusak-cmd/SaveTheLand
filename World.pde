final float noiseScale = 0.01;
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
  void zoom(float s) {
    size *= s;
  }
  
  float getSize() { return size; }
  
  void draw() {
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
                float tile = tileMap.length*1.9*noise((x+xNoise)*noiseScale, (y+yNoise)*noiseScale);
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
    text("Level: "+(levelCounter+1), width*0.65, height*0.05);
    for (int i = 0; i < tileCounter.length; i++) {
      if (tileCounter[i] < goals[levelCounter][i])
        winLevel = false;
      String name = "error";
      if (i == 0) name = "Sand";
      else if (i == 1) name = "Stone";
      else if (i == 2) name = "Grass";
      else if (i == 3) name = "Life Stone";
      text(name+": "+tileCounter[i]+"/"+goals[levelCounter][i], width*0.50, height*(0.05*i)+height*0.1);
     
      tileCounter[i] = 0;
    } 
    pop();  
    if (winLevel) {
      levelBeaten = true;
    }
  }
}
