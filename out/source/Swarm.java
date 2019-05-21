import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import g4p_controls.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Swarm extends PApplet {

 //<>// //<>//


// State variables for tracking the goal
PVector goal;              // The machine learning target
PVector mouse;             // Vector to track the mouse
boolean goalLock = false;  // Whether the goal is locked to the mouse

// Simulation state variables
boolean pause = false;    // Whether the simulation is paused
boolean firstRun = true;  // False after the first draw() loop
PImage underlay;          // Graphics buffer to hold the heatmap background

// Global state variables for the population
Population birbs;
float COG_CONST = 1; // Cognitive constant
float SOC_CONST = 1; // Social constant

// Physical constants
int INERTIA;        // Bird inertia
int SPEED_LIMIT;    // Speed limit on each bird

// Dependency injections
Evaluator EVAL_FUNC;    // Fitness function for the birdos
String VEL_FUNC;  // Velocity update function for each burd


public void setup() {
  
  if (firstRun) {
    createGUI();
    firstRun = false;
  }
  
  SPEED_LIMIT = speedSlider.getValueI();
  INERTIA = inertiaSlide.getValueI();
  VEL_FUNC = "Full Model";  //  Change this to change how the dots update their velocity
  
  pause = true;
  
  textSize(12); 
  goal = new PVector(width/2, height/2);

  birbs = new SwarmPopulation(1000);

  setEvaluator();
  underlay = mapHeat();

  mouse = new PVector(mouseX, mouseY);
  
}

public void draw() {
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
  text("Target: " + goal, 3 * width/4.0f - 50, 10);
}

public void setEvaluator() {
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

// public Velociraptor getAccelerator(String stepType) {
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

public PImage mapHeat() {
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

public void mousePressed() {
  if (mouse.dist(goal) < 10) {
    goalLock = !goalLock;
    //birbs.reset();
  }
}

public void keyPressed() { // Hotkey definitions
  if (key == ' ') {
    pause = !pause;
  } else if (key == 'r') {
    setup();
  } else if (key == 'l') {
    goalLock = !goalLock;
    //birbs.reset();
  }
}
abstract class Brain {
    Moveable thinker;
    public abstract void evaluate();
}


class SwarmBrain extends Brain {
    PVector bestPosition;
    float bestFit;
    boolean isBest;

    /**
     * Evaluate the fitness and update the internal state.
     * If the fitness is better than the current best,
     * replace the current best with the fitness
     */
    public abstract float evaluate() {
        this.fit = EVAL_FUNC.evalFunction(goal, this.vel, this.position);  // Calculate fitness using the fitness function
        
        if (this.fit < this.fitBest || fitBest == -1) {                 // If the new fitness value is better than the previous best:
        this.fitBest = this.fit;                                      //   replace the previous best fitness
        this.bestPosition = this.position;                            //   replace the previous best position
        
        if (parent.gDotBest == null 
            || this.fitBest < parent.gDotBest.fitBest) {                // If the fitness (guaranteed to differ) is better than the
            parent.setBest(this);
        }
        }    return this.fit;
    }
}
/**
 * Dot describes a drawable object that will display as a circle of a fixed size.
 * The drawable class adds a renderer object and a draw method that calls the
 * renderer to draw to the screen.
 */
class Dot extends Drawable {
  int radius;
  Population container;

  Dot(Population container, int radius, 
      Renderer renderest, int strokeMe, int fillMe, PVector position) {

    super(renderest, strokeMe, fillMe, position); // Initialize properties of Drawable class

    int xDistr = (int) random(width);
    int yDistr = (int) random(height);

    this.container = container;
    this.radius = radius;

    position = new PVector(xDistr, yDistr);

    bestPosition = position;
    vel = PVector.random2D();
    vel.limit(SPEED_LIMIT);
    fitBest = -1;
  }

  //--------------------------------------------------------------------------------

  public void show() {
    this.renderer.draw();
  }
  
  public void update_velocity() {

    this.accelerator.update_velocity();
  }

  //---------------------------------------------------------------------------------
  public void move() {
    this.position = PVector.add(position, vel);
  }

  //--------------------------------------------------------------------------------

  /**
   * Evaluate the fitness and update the internal state.
   * If the fitness is better than the current best,
   * replace the current best with the fitness
   */
  public abstract float evaluate() {
    this.fit = EVAL_FUNC.evalFunction(goal, this.vel, this.position);  // Calculate fitness using the fitness function
    
    if (this.fit < this.fitBest || fitBest == -1) {                 // If the new fitness value is better than the previous best:
      this.fitBest = this.fit;                                      //   replace the previous best fitness
      this.bestPosition = this.position;                            //   replace the previous best position
      
      if (container.gDotBest == null 
        || this.fitBest < container.gDotBest.fitBest) {                // If the fitness (guaranteed to differ) is better than the
        container.setBest(this);
      }
    }    return this.fit;
  }

}


class SwarmingDot extends Dot implements Moveable {
    
    Brain controller;
    Velociraptor accelerator;

    public SwarmingDot(String accStepType, Population container, int radius, Renderer renderest, int strokeMe, int fillMe, PVector position) {
        
        super(container, radius, renderest, shade, shade, position);

        this.accelerator = Velociraptor.getAccelerator(accStepType);
    }
    
}
/**
 * The drawable abstract class describes an object that can
 * draw a representation of itself to the screen. To do this
 * it uses a renderer, which it calls in its draw() method.
 */
abstract class Drawable {
    private Renderer renderer;
    int objStroke, objFill;
    PVector position;

    Drawable(Renderer renderest, int strokeMe, int fillMe, PVector position) {
        this.renderer = renderest;
        this.renderer.setTarget = this;
        
        this.objStroke = strokeMe;
        this.objFill = fillMe;
        this.position = position;
    }

    public void draw() {
        this.renderer.draw();
    }
}

/**
 * An abstract class to outline an object 
 */
abstract class Renderer<T extends Drawable> {
    T target;

    public Renderer(T target) {
        this.target = target;
    }

    public abstract void draw();
    public abstract void setTarget(T target);
}

class RenderDot<T extends Dot> extends Renderer {

    T target;

    public RenderDot(T target){
        super(target);
    }
    
    public void draw() {
        fill(target.objFill);
        stroke(target.objStroke);

        ellipse(target.position.x, target.position.y, target.radius, target.radius);
    }
}


abstract class Evaluator {
  public abstract float evalFunction(PVector goal, PVector vel, PVector pos);
}

class LinearEval extends Evaluator {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    
    float posX = pos.x;
    float posY = pos.y;
    
    return pos.dist(goal);
  }
}

class AbsDiffEval extends Evaluator {  //    | |dX| - |dY| |
  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    float xterm = abs(goal.x - pos.x);
    float yterm = abs(goal.y - pos.y);
    return abs(xterm - yterm);
    
  }
}

class DistVelEval extends Evaluator {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    return sqrt(pow(pos.dist(goal) + vel.mag() - 100, 2));
  }
}

class LogEval extends Evaluator {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    float val = pos.dist(goal);
    return val != 0? 6*log(val): 0;
  }
}

class SinEval extends Evaluator {
  
  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    float val = pos.dist(goal);
    return (.25f * val) * (sin(.025f * val));
  }
}

class EricEval extends Evaluator {
  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    return (PVector.sub(goal, pos)).mag();
  }
}

class MouseEval extends Evaluator {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    
    float posX = pos.x;
    float posY = pos.y;
    float val = pos.dist(goal);
    if (pos.dist(mouse) < 100){
      val += 100;
    }
    return val;
  }
}
/**
 * Container class to hold a population of drawable objects
 */
abstract class Population<T extends Drawable> {
  T[] members;
  int size;
  
  // Constructor boye
  Population(int size) {
    this.size = size;
    members = new T[this.size];
  }

  //---------------------------------------------------------------
  public void show() {
    for (T d : members) {
      d.draw();
    }
  }

  /**
   * Loop through the collection of particles and have each one evaluate itself.
   * For each dot, call its eval function, then test if its best
   * fitness is better than the global best. If it is, update the global
   * best and global best positions.
   */
  public void update() {
    for (T d : this.members) {    // Iterate through the population of members
      d.update();
      d.evaluate();     // Evaluate the current dot's fitness

      d.update_velocity();
      d.move();
    }
  }

}


class SwarmPopulation extends Population<SwarmingDot> {
  SwarmingDot gDotBest;
  
  // Runtime constants
  float COG_CONST = 1; // Cognitive constant
  float SOC_CONST = 1; // Social constant
  
  // Global bests
  float gFitBest = -1;  // Global best fitness
  PVector gPosBest;     // Global best position
  
  // Constructor boye
  SwarmPopulation(int size) {
    super(size);
    for (int i = 0; i < size; i++) {

      int shade = color(
            random(256),
            random(256),
            random(256)
            );
      PVector randomPos = new PVector((int) random(width), (int) random(height));

      members[i] = new SwarmingDot(VEL_FUNC, this, new RenderDot(), shade, shade, randomPos);
    }
    gDotBest = members[0];
  }

  //---------------------------------------------------------------
  public void show() {
    for (SwarmingDot d : members) {
      d.draw();
    }
  }

  /**
   * Loop through the collection of particles and have each one evaluate itself.
   * For each dot, call its eval function, then test if its best
   * fitness is better than the global best. If it is, update the global
   * best and global best positions.
   */
  public void update() {
    for (SwarmingDot d : this.members) {    // Iterate through the population of members

      d.evaluate();     // Evaluate the current dot's fitness if it's still alive

      d.update_velocity();
      d.move();
    }
  }

  public void setBest(SwarmingDot d) {
    this.gDotBest.isBest = false;
    d.isBest = true;
    this.gDotBest = d;
  }

  public void reset () {
    for (SwarmingDot d : this.members) {
      d.bestPosition = d.position;
      d.fitBest = d.fit;
    }
  }
}
interface Moveable {

  public void move();
  public void update_velocity();

}



// -------------------------------------------------------
// Accelerator generic abstract class and its implementations
abstract class Velociraptor<T> {

  T d;

  public Velociraptor(T d) {
    this.d = d;
  }

  public abstract void update_velocity();

  public static Velociraptor getAccelerator(String stepType) {
  stepType = stepType.toLowerCase();
  if (stepType.equals("full model")) {
    return new FullModel(this);
  } else if (stepType.equals("cognitive only")) {
    return new CogOnly(this);
  } else if (stepType.equals("social only")) {
    return new SocOnly(this);
  } else {
    throw new IllegalArgumentException();
  }
}

}

class FullModel<T> extends Velociraptor<T> {
  public FullModel(T d) {
    super(d);
  }

  public void update_velocity() {

    float r1 = random(1);
    float r2 = random(1);
    
    PVector momentum = PVector.mult(this.d.vel, INERTIA);
    PVector cognitive = (PVector.sub(this.d.bestPosition, this.d.position)).mult(COG_CONST * r1); 
    PVector social = (PVector.sub(this.d.parent.gDotBest.position, this.d.position)).mult(SOC_CONST * r2);

    this.d.vel = PVector.add(momentum, cognitive).add(social);
    this.d.vel.limit(SPEED_LIMIT);
  }
}


class CogOnly<T> extends Velociraptor<T> {

  public CogOnly(T d) {
    super(d);
  }

  public void update_velocity() {

    float r1 = random(1); // Keeping names in convention, r1 is the random number multiplied with the cognitive constant
    
    PVector momentum = PVector.mult(this. d.vel, INERTIA);
    PVector cognitive = (PVector.sub(this.d.bestPosition, this.d.position)).mult(COG_CONST * r1);
    
    this.d.vel = PVector.add(momentum, cognitive);
    this.d.vel.limit(SPEED_LIMIT);
  }
}


class SocOnly<T> extends Velociraptor<T> {

  public SocOnly(T d) {
    super(d);
  }

  public void update_velocity() {
    float r2 = random(1); // Keeping names in convention, r2 is the random number multiplied with the social constant
    
    PVector momentum = PVector.mult(this.d.vel, INERTIA);
    
    PVector social = (PVector.sub(this.d.parent.gDotBest.position, this.d.position)).mult(SOC_CONST * r2);
    
    this.d.vel = PVector.add(momentum, social);
    this.d.vel.limit(SPEED_LIMIT);
  }
}
/* =========================================================
 * ====                   WARNING                        ===
 * =========================================================
 * The code in this tab has been generated from the GUI form
 * designer and care should be taken when editing this file.
 * Only add/edit code inside the event handlers i.e. only
 * use lines between the matching comment tags. e.g.

 void myBtnEvents(GButton button) { //_CODE_:button1:12356:
     // It is safe to enter your event code here  
 } //_CODE_:button1:12356:
 
 * Do not rename this tab!
 * =========================================================
 */

public void resetHit(GImageButton source, GEvent event) { //_CODE_:resetButton:623085:
  setup();
} //_CODE_:resetButton:623085:

public void pauseHis(GImageButton source, GEvent event) { //_CODE_:button1:677062:
  pause = !pause;
} //_CODE_:button1:677062:

public void evalListClicked(GDropList source, GEvent event) { //_CODE_:evalList:764184:
  setEvaluator();
  underlay = mapHeat();
  birbs.reset();
} //_CODE_:evalList:764184:

public void speedChange(GCustomSlider source, GEvent event) { //_CODE_:speedSlider:810070:
  SPEED_LIMIT = source.getValueI();
} //_CODE_:speedSlider:810070:

public void inertiaChange(GCustomSlider source, GEvent event) { //_CODE_:inertiaSlide:427060:
  INERTIA = source.getValueI();
} //_CODE_:inertiaSlide:427060:

public void constantChange(GSlider2D source, GEvent event) { //_CODE_:socCogAdj:776689:
  COG_CONST = source.getValueXF();
  SOC_CONST = source.getValueYF();
} //_CODE_:socCogAdj:776689:



// Create all the GUI controls. 
// autogenerated do not edit
public void createGUI(){
  G4P.messagesEnabled(false);
  G4P.setGlobalColorScheme(GCScheme.BLUE_SCHEME);
  G4P.setMouseOverEnabled(false);
  surface.setTitle("Sketch Window");
  inertiaLabel = new GLabel(this, 920, 70, 80, 20);
  inertiaLabel.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  inertiaLabel.setText("Inertia");
  inertiaLabel.setOpaque(true);
  speedLabel = new GLabel(this, 835, 70, 80, 20);
  speedLabel.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  speedLabel.setText("Speed");
  speedLabel.setOpaque(true);
  resetButton = new GImageButton(this, 864, 392, 100, 100, new String[] { "reset.png", "reset.png", "reset.png" } );
  resetButton.addEventHandler(this, "resetHit");
  button1 = new GImageButton(this, 852, 518, 126, 125, new String[] { "play-pause.png", "play-pause.png", "play-pause.png" } );
  button1.addEventHandler(this, "pauseHis");
  evalList = new GDropList(this, 834, 33, 154, 168, 5, 30);
  evalList.setItems(loadStrings("list_764184"), 0);
  evalList.setLocalColorScheme(GCScheme.GREEN_SCHEME);
  evalList.addEventHandler(this, "evalListClicked");
  speedSlider = new GCustomSlider(this, 908, 100, 200, 60, "grey_blue");
  speedSlider.setShowValue(true);
  speedSlider.setShowLimits(true);
  speedSlider.setTextOrientation(G4P.ORIENT_RIGHT);
  speedSlider.setRotation(PI/2, GControlMode.CORNER);
  speedSlider.setLimits(25.0f, 25.0f, 1.0f);
  speedSlider.setNbrTicks(25);
  speedSlider.setStickToTicks(true);
  speedSlider.setShowTicks(true);
  speedSlider.setNumberFormat(G4P.DECIMAL, 1);
  speedSlider.setOpaque(true);
  speedSlider.addEventHandler(this, "speedChange");
  inertiaSlide = new GCustomSlider(this, 989, 100, 200, 60, "grey_blue");
  inertiaSlide.setShowValue(true);
  inertiaSlide.setShowLimits(true);
  inertiaSlide.setRotation(PI/2, GControlMode.CORNER);
  inertiaSlide.setLimits(30.0f, 30.0f, 1.0f);
  inertiaSlide.setNbrTicks(30);
  inertiaSlide.setStickToTicks(true);
  inertiaSlide.setShowTicks(true);
  inertiaSlide.setNumberFormat(G4P.DECIMAL, 1);
  inertiaSlide.setOpaque(true);
  inertiaSlide.addEventHandler(this, "inertiaChange");
  socCogAdj = new GSlider2D(this, 14, 112, 125, 125);
  socCogAdj.setLimitsX(1.0f, 0.0f, 10.0f);
  socCogAdj.setLimitsY(1.0f, 0.0f, 10.0f);
  socCogAdj.setNumberFormat(G4P.DECIMAL, 1);
  socCogAdj.setOpaque(true);
  socCogAdj.addEventHandler(this, "constantChange");
  label1 = new GLabel(this, 28, 246, 80, 20);
  label1.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label1.setText("X - Cognitive");
  label1.setTextBold();
  label1.setOpaque(true);
  label2 = new GLabel(this, 149, 166, 80, 20);
  label2.setTextAlign(GAlign.CENTER, GAlign.MIDDLE);
  label2.setText("Y - Social");
  label2.setTextBold();
  label2.setOpaque(true);
}

// Variable declarations 
// autogenerated do not edit
GLabel inertiaLabel; 
GLabel speedLabel; 
GImageButton resetButton; 
GImageButton button1; 
GDropList evalList; 
GCustomSlider speedSlider; 
GCustomSlider inertiaSlide; 
GSlider2D socCogAdj; 
GLabel label1; 
GLabel label2; 
  public void settings() {  size(1000, 750); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Swarm" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
