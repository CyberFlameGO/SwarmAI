import g4p_controls.*; //<>//

PVector goal;
PVector mouse;

Population birbs;
boolean goalLock = false;
boolean pause = false;
boolean firstRun = true;

PImage underlay;

float COG_CONST = 1; // Cognitive constant
float SOC_CONST = 1; // Social constant

int INERTIA;
int SPEED_LIMIT;

Evaluator EVAL_FUNC;// = new LinearEval();


void setup() {
  
  if (firstRun) {
    createGUI();
    firstRun = false;
  }
  
  SPEED_LIMIT = speedSlider.getValueI();
  INERTIA = inertiaSlide.getValueI();
  
  pause = true;
  size(1000, 750);
  textSize(12); 
  goal = new PVector(width/2, height/2);
  birbs = new Population(1000, goal);
  
  setEvaluator();
  underlay = mapHeat();

  mouse = new PVector(mouseX, mouseY);
  
}

void draw() {
  mouse = new PVector(mouseX, mouseY);
  background(255);
  image(underlay, 0, 0);
  // Display goal
  fill(255, 0, 0);
  ellipse(goal.x, goal.y, 10, 10);
  birbs.show();



  if (goalLock) {     // If the goal is locked to the mouse

    // Set the goal's position equal to the mouse's
    goal.x=mouseX;
    goal.y=mouseY;

    birbs.reset();    // Reset the 'best' values
  }
  if (!pause) { // Don't do any processing if the user pauses
    birbs.update(goal);
  }
  fill(0);
  text("Target: " + goal, 3 * width/4.0 - 50, 10);
}

void setEvaluator() {
  switch (evalList.getSelectedText()) {
  case "Linear Distance":
    EVAL_FUNC = new LinearEval();
    break;
  case "Absolute Difference":
    EVAL_FUNC = new AbsDiffEval();
    break;
  case "Logarithmic":
    EVAL_FUNC = new LogEval();
    break;
  case "Increasing Sine Function":
    EVAL_FUNC = new SinEval();
    break;
  case "Distance/Velocity":
    EVAL_FUNC = new DistVelEval();
    break;
  case "Eric, this one is yours":
    EVAL_FUNC = new EricEval();
    break;
  case "Avoid mouse, seek goal":
    EVAL_FUNC = new MouseEval();
    break;
  default:
    EVAL_FUNC = new LinearEval();
  }
  birbs.reset();
}

PImage mapHeat() {
  PGraphics cache = createGraphics(width, height);
  cache.beginDraw();
  cache.background(255);
  PVector point = new PVector(0,0);
  float pointVal;
  PVector zeroVector = new PVector(0, 0);
  zeroVector.limit(0);
  
  float[] cardinals = {goal.x, width-goal.x, goal.y, width-goal.y};
  
  for (int x = 0; x < width; x++) {
    for (int y = 0; y < height; y++) {
      point.set(x, y);
      pointVal = EVAL_FUNC.evalFunction(goal, zeroVector, point);
      cache.stroke(map(pointVal, 0, max(cardinals), 0, 255));
      cache.point(x,y);
    }
  }
  
  cache.endDraw();
  return cache.get();
}

void mousePressed() {
  if (mouse.dist(goal) < 10) {
    goalLock = !goalLock;
    //birbs.reset();
  }
}

void keyPressed() { // Hotkey definitions
  if (key == ' ') {
    pause = !pause;
  } else if (key == 'r') {
    setup();
  } else if (key == 'l') {
    goalLock = !goalLock;
    //birbs.reset();
  }
}
