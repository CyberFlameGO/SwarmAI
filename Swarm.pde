import g4p_controls.*; //<>//

PVector goal;
PVector mouse;

Population birbs;
Dot closest;
boolean goalLock = false;
boolean pause = false;
int INERTIA;
int SPEED_LIMIT;


Evaluator evalr = new EricEval();


void setup() {
  createGUI();
  SPEED_LIMIT = speedSlider.getValueI();
  INERTIA = inertiaSlide.getValueI();
  pause = true;
  size(1000, 750);
  textSize(12); 
  goal = new PVector(width/2, height/2);
  birbs = new Population(1000, goal, evalr);

  
  mouse = new PVector(mouseX, mouseY);
}

void draw() {
  mouse = new PVector(mouseX, mouseY);

  background(255);
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


void mousePressed() {
  if (mouse.dist(goal) < 10) {
    goalLock = !goalLock;
    birbs.reset();
  }
}

void keyPressed() { // Hotkey definitions
  if (key == ' ') {
    pause = !pause;
  } else if (key == 'r') {
    setup();
  } else if (key == 'l') {
    goalLock = !goalLock;
    birbs.reset();
  }
}
