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
public void setup() {
  
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

        // Set the window dimensions in pixels
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
 * state
 * 
 * Update the position of the mouse for later use, it will
 * factor into whether the goal moves and where.
 * Redraw the 
 */
public void draw() {
  mouse = new PVector(mouseX, mouseY);
  // background(255);
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

public void setProblem() {
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
      pointVal = PROBLEM.evalFunction(goal, zeroVector, point);
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
abstract class Controller<T, P extends Population> {
    T thinker;

    public Controller(T thinker) {
        this.thinker = thinker;
    }
    public abstract float evaluate(PVector position, PVector velocity, P container);
}


/**
 * Controller for a dot using swarm intelligence
 */
class SwarmBrain extends Controller<SwarmingDot, SwarmPopulation> {

    PVector bestPosition;
    PVector velocity;
    
    SwarmPopulation flock;

    float fit;
    float bestFit;
    boolean isBest;

    public SwarmBrain(SwarmingDot thinker) {
        super(thinker);

        this.bestPosition = this.thinker.position;
        this.bestFit = -1;
        this.isBest = false;
    }

    

    /**
     * Evaluate the fitness and update the internal state.
     * If the fitness is better than the current best,
     * replace the current best with the fitness
     */
    public float evaluate(PVector position, PVector velocity, SwarmPopulation flock) {
        this.fit = PROBLEM.evalFunction(goal, thinker.velocity, position);  // Calculate fitness using the fitness function
        
        if (this.fit < this.bestFit || bestFit == -1) { // If the new fitness value is better than the previous best:
            this.bestFit = this.fit;                    //   replace the previous best fitness
            this.bestPosition = position;               //   replace the previous best position
            
            if (flock.gDotBest == null 
                || this.bestFit < flock.gDotBest.controller.bestFit) {                // If the fitness (guaranteed to differ) is better than the
                flock.setBest(this.thinker);
            }
        }
        return this.fit;
    }

    public void resetBests() {
        this.bestPosition = thinker.position;
        this.bestFit = this.fit;
    }
}
/**
 * A drawable object that will display as a circle of fixed size.
 * The drawable class adds a renderer object and a draw method that calls the
 * renderer to draw to the screen.
 */
class Dot<P extends Population> extends Shape {
  int radius;
  P container;

  Dot(P container, int radius, 
      RenderDot renderest, int strokeMe, int fillMe, PVector position) {

    super(renderest, strokeMe, fillMe, position); // Initialize properties of Drawable class

    this.container = container;
    this.radius = radius;

  }

}

/**
 * A dot with a velocity, swarm controller, and accelerator.
 */
class SwarmingDot<A extends Accelerator> extends Dot<SwarmPopulation> implements Moveable {

  // Velocity is a 2D vector
  PVector velocity;
  SwarmBrain controller;
  A accelerator;

  public SwarmingDot(A accel, SwarmPopulation container, int radius, RenderDot renderest, int strokeMe, int fillMe, PVector position) {

    super(container, radius, renderest, strokeMe, fillMe, position);

    this.accelerator = accel;
    accel.setTarget(this);

    this.controller = new SwarmBrain(this);
    this.velocity = PVector.random2D();

    this.velocity.limit(SPEED_LIMIT);
  }

  //---------------------------------------------------------------------------------
  public void move() {
    this.position = PVector.add(this.position, this.velocity);
  }

  public void update() {
    this.controller.evaluate(this.position, this.velocity, this.container);
    this.accelerator.updateVelocity();
  }

  public void resetBests() {
    this.controller.resetBests();
  }

  public void setAsBest(boolean isBest) {
    this.controller.isBest = isBest;
  }

  public void setVelocity(PVector newVel) {
    this.velocity = newVel;
    this.velocity.limit(SPEED_LIMIT);
  }

  public PVector getPosition() {
    return this.position;
  }

  public PVector getVelocity() {
    return this.velocity;
  }

  public PVector getBestPos() {
    return this.controller.bestPosition;
  }

  public SwarmPopulation getContainer() {
    return this.container;
  }


}
/**
 * The drawable abstract class describes an object that can
 * draw a representation of itself to the screen. To do this
 * it uses a renderer, which it calls in its draw() method.
 * 
 */
abstract class Drawable<R extends Renderer> {
    protected R renderer;
    public int objStroke, objFill;

    Drawable(R renderest, int strokeMe, int fillMe) {
        this.renderer = renderest;
        this.renderer.setTarget(this);
        
        this.objStroke = strokeMe;
        this.objFill = fillMe;
    }

    public void draw() {
        this.renderer.draw();
    }
}


/**
 * A drawable that's some kind of geometric shape with a 
 * position on-screen.
 */
abstract class Shape extends Drawable {

    PVector position;       // Defined on an individual basis–for example a circle uses the center while a rectangle uses a corner
    float mainDimension;    // The largest dimension. A circle's radius, a rectangle's long side, etc
    int sides;              // The number of sides the shape has–0 for a circle

    public Shape(Renderer renderest, int strokeMe, int fillMe, PVector position) {
        super(renderest, strokeMe, fillMe);
        this.position = position;
    }
}



// -------------------------------------------------------------
// Rendering Classes

/**
 * An abstract class to outline an object that draws
 * a drawable to the screen. Serves as a dependency 
 * injection for Drawables to use.
 *
 * (I found it most helpful to think about the renderer as
 * the "tool" the Drawable uses to draw itself)
 */
abstract class Renderer<T extends Drawable> {
    T target;

    public abstract void draw() throws RenderTargetNotSetException;
    public void setTarget(T target) {
        this.target = target;
    }

}


/**
 * RenderDot renders anything that inherits the Dot class.
 * As a result, it can handle drawing most objects that take
 * the shape of a circle.
 */
class RenderDot extends Renderer<Dot>  {
    
    public void draw() {
        if (target == null) {
            throw new RenderTargetNotSetException();
        }
        fill(target.objFill);
        stroke(target.objStroke);

        ellipse(target.position.x, target.position.y, target.radius, target.radius);
    }
}

class RenderTargetNotSetException extends RuntimeException {
        
    public RenderTargetNotSetException() {
        super("Failed to set a valid target for the renderer");
    }
}
/**
 * Container class to hold a population of drawable objects
 */
abstract class Population<T extends Drawable> {
  ArrayList<T> members;
  int size;
  
  // Constructor boye
  Population(int size) {
    this.size = size;
    members = new ArrayList<T>(size);
  }

  //---------------------------------------------------------------
  public void show() {
    for (T d : members) {
      d.draw();
    }
  }

}

/**
 * A population of a swarm of dots
 */
class SwarmPopulation extends Population<SwarmingDot> {
  SwarmingDot gDotBest;
  
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

      members.add(new SwarmingDot(genAccelerator(VEL_FUNC), this, DOT_RADIUS, new RenderDot(), shade, shade, randomPos));
    }
    gDotBest = members.get(0);
  }

  /**
   * Loop through the collection of particles and have each one evaluate itself.
   * For each dot, call its eval function, then test if its best
   * fitness is better than the global best. If it is, update the global
   * best and global best positions.
   */
  public void update() {
    for (SwarmingDot d : this.members) {    // Iterate through the population of members
      d.update();
      d.move();
    }
  }

  public void setBest(SwarmingDot d) {
    this.gDotBest.setAsBest(false);
    d.setAsBest(true);
    this.gDotBest = d;
  }

  public void reset () {
    for (SwarmingDot d : this.members) {
      d.resetBests();
    }
  }

  public PVector getGBestPos() {
    return this.gDotBest.getBestPos();
  }
}
abstract class Problem {
  public abstract float evalFunction(PVector goal, PVector vel, PVector pos);
}

class LinearEval extends Problem {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    
    float posX = pos.x;
    float posY = pos.y;
    
    return pos.dist(goal);
  }
}

class AbsDiffEval extends Problem {  //    | |dX| - |dY| |
  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    float xterm = abs(goal.x - pos.x);
    float yterm = abs(goal.y - pos.y);
    return abs(xterm - yterm);
    
  }
}

class DistVelEval extends Problem {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    return sqrt(pow(pos.dist(goal) + vel.mag() - 100, 2));
  }
}

class LogEval extends Problem {

  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    float val = pos.dist(goal);
    return val != 0? 6*log(val): 0;
  }
}

class SinEval extends Problem {
  
  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    float val = pos.dist(goal);
    return (.25f * val) * (sin(.025f * val));
  }
}

class EricEval extends Problem {
  public float evalFunction(PVector goal, PVector vel, PVector pos) {
    return (PVector.sub(goal, pos)).mag();
  }
}

class MouseEval extends Problem {

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
interface Moveable {

  public void move();
  public void update();
  public PVector getVelocity();

}



// -------------------------------------------------------
// Accelerator generic abstract class and its implementations
abstract class Accelerator<T extends Moveable> {

  T d;

  public void setTarget(T d) {
    this.d = d;
  }
  public abstract void updateVelocity();

}

// Don't need the generics???
class FullModel extends Accelerator<SwarmingDot> {
    
  public void updateVelocity() {

    float r1 = random(1);
    float r2 = random(1);
    
    PVector momentum = PVector.mult(this.d.getVelocity(), INERTIA);
    PVector cognitive = (PVector.sub(this.d.getBestPos(), this.d.getPosition())).mult(COG_CONST * r1); 
    PVector social = (PVector.sub(this.d.getContainer().getGBestPos(), this.d.getPosition())).mult(SOC_CONST * r2);

    this.d.setVelocity(PVector.add(momentum, cognitive).add(social));
  }
}


class CogOnly extends Accelerator<SwarmingDot> {

  public void updateVelocity() {

    float r1 = random(1); // Keeping names in convention, r1 is the random number multiplied with the cognitive constant
    
    PVector momentum = PVector.mult(this. d.getVelocity(), INERTIA);
    PVector cognitive = (PVector.sub(this.d.getBestPos(), this.d.getPosition())).mult(COG_CONST * r1);
    
    this.d.setVelocity(PVector.add(momentum, cognitive));
  }
}


class SocOnly extends Accelerator<SwarmingDot> {

  public void updateVelocity() {
    float r2 = random(1); // Keeping names in convention, r2 is the random number multiplied with the social constant
    
    PVector momentum = PVector.mult(this.d.getVelocity(), INERTIA);
    
    PVector social = (PVector.sub(this.d.getContainer().getGBestPos(), this.d.getPosition())).mult(SOC_CONST * r2);
    
    this.d.setVelocity(PVector.add(momentum, social));
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
  setProblem();
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
