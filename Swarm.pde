import g4p_controls.*; //<>// //<>//


// State variables for tracking the goal
PVector goal;              // The machine learning target
PVector mouse;             // Vector to track the mouse
boolean goalLock = false;  // Whether the goal is locked to the mouse

// Simulation state variables
boolean pause = false;    // Whether the simulation is paused
boolean firstRun = true;  // False after the first draw() loop
PImage underlay;          // Graphics buffer to hold the heatmap background

// Global state variables for the population
SwarmPopulation birbs;
float COG_CONST = 1; // Cognitive constant
float SOC_CONST = 1; // Social constant

// Physical constants
int INERTIA;        // Bird inertia
int SPEED_LIMIT;    // Speed limit on each bird

// Dependency injections
Problem PROBLEM;    // Fitness function for the birdos
String VEL_FUNC;    // Velocity update function for each burd
int DOT_RADIUS = 2; // Drawn radius for the dots


void setup() {
  
  if (firstRun) {
    createGUI();
    firstRun = false;
  }
  
  SPEED_LIMIT = speedSlider.getValueI();
  INERTIA = inertiaSlide.getValueI();
  VEL_FUNC = "Full Model";  //  Change this to change how the dots update their velocity
  
  pause = true;
  size(1000, 750);
  textSize(12); 
  goal = new PVector(width/2, height/2);

  birbs = new SwarmPopulation(1000);

  setProblem();
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
    birbs.update();
  }
  fill(0);
  text("Target: " + goal, 3 * width/4.0 - 50, 10);
}

void setProblem() {
  switch (evalList.getSelectedText()) {
  case "Linear Distance":
    PROBLEM = new LinearEval();
    break;
  case "Absolute Difference":
    PROBLEM = new AbsDiffEval();
    break;
  case "Logarithmic":
    PROBLEM = new LogEval();
    break;
  case "Increasing Sine Function":
    PROBLEM = new SinEval();
    break;
  case "Distance/Velocity":
    PROBLEM = new DistVelEval();
    break;
  case "Eric, this one is yours":
    PROBLEM = new EricEval();
    break;
  case "Avoid mouse, seek goal":
    PROBLEM = new MouseEval();
    break;
  default:
    PROBLEM = new LinearEval();
  }

  birbs.reset();
}

// public Accelerator getAccelerator(String stepType) {
//   stepType = stepType.toLowerCase();
//   if (stepType.equals("full model")) {
//     return new FullModel(this);
//   } else if (stepType.equals("cognitive only")) {
//     return new CogOnly(this);
//   } else if (stepType.equals("social only")) {
//     return new SocOnly(this);
//   } else {
//     throw new IllegalArgumentException();
//   }
// }

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
      pointVal = PROBLEM.evalFunction(goal, zeroVector, point);
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
