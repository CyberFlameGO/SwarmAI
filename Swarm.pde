import g4p_controls.*; //<>// //<>//


// State variables for tracking the goal
PVector goal;              // The machine learning target
PVector mouse;             // Vector to track the mouse
boolean goalLock = false;  // Whether the goal is locked to the mouse

// Simulation state variables
boolean pause = false;    // Whether the simulation is paused
boolean firstRun = true;  // False after the first draw() loop
PImage underlay;          // Graphics buffer to hold the heatmap
                          // generated from the eval function

// Global state variables for the population
SwarmPopulation birbs;
float COG_CONST = 1; // Cognitive constant
float SOC_CONST = 1; // Social constant

// Physical constants
int INERTIA;        // Bird inertia
int SPEED_LIMIT;    // Speed limit on each bird

// Dependency injections
Problem PROBLEM;    // Fitness function for the birdos
String VEL_FUNC;    // Velocity update function for each burd,
                    // defines their acceleration behavior

int DOT_RADIUS = 2; // Drawn radius for the dots

/**Set up the simulation, defining the necessary constants,
 * objects, and environment attributes.
 * 
 * Create the GUI from g4p on the first run, then sets the
 * constants to the values given for them by the gui.
 * Define the window and various attributes/elements that
 * will display therein. After setting the problem and
 * drawing the background, start tracking the mouse and
 * exit to the repeatedly called "draw()" function.
 *
 * Run when the program is first executed, as well as
 * on any kind of reset.
 */
void setup() {
  
  // Create the GUI the first time it executes, then make sure
  // it isn't redrawn on every reset
  if (firstRun) {
    createGUI();
    firstRun = false;
  }
  
  // Set the constants, defined as they appear above
  SPEED_LIMIT = speedSlider.getValueI(); 
  INERTIA = inertiaSlide.getValueI();
  VEL_FUNC = "Full Model";
  
  pause = true;         // Start the simulation paused, so the
                        // user must start it manually

  size(1000, 750);      // Set the window dimensions in pixels
  textSize(12);         // Font size in the window

  goal = new PVector(   // Create the goal. This is a point
    width/2, height/2   // on the screen that represents the
    );                  // origin of the evaluation function.

  // Make a new population of SwarmingDots
  birbs = new SwarmPopulation(1000);

  setProblem();         // Define the evaluation function, the "problem"
  underlay = mapHeat(); // Use the eval function to create the background

  // Start tracking the position of the mouse in the window
  mouse = new PVector(mouseX, mouseY);
  
}

/**The update function, called every frame to update the simulation
 * stat
 * 
 */
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

public Accelerator genAccelerator(String stepType) {
  stepType = stepType.toLowerCase();
  if (stepType.equals("full model")) {
    return new FullModel();
  } else if (stepType.equals("cognitive only")) {
    return new CogOnly();
  } else if (stepType.equals("social only")) {
    return new SocOnly();
  } else {
    throw new IllegalArgumentException();
  }
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
