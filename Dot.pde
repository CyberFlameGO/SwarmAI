/**
 * A drawable object that will display as a circle of fixed size.
 * The drawable class adds a renderer object and a draw method that calls the
 * renderer to draw to the screen.
 */
class Dot<P extends Population> extends Shape {
  int radius;
  P container;

  Dot(P container, int radius, 
      RenderDot renderest, color strokeMe, color fillMe, PVector position) {

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

  public SwarmingDot(A accel, SwarmPopulation container, int radius, RenderDot renderest, color strokeMe, color fillMe, PVector position) {

    super(container, radius, renderest, strokeMe, fillMe, position);

    this.accelerator = accel;
    accel.setTarget(this);
    
    this.controller = new SwarmBrain(this);
    this.velocity = PVector.random2D();

    this.velocity.limit(SPEED_LIMIT);
  }

  //---------------------------------------------------------------------------------
  void move() {
    this.position = PVector.add(this.position, this.velocity);
  }

  public void update() {
    this.controller.evaluate(this.position, this.velocity, this.container);
    this.accelerator.updateVelocity();
  }

  public void setAsBest(boolean isBest) {
    this.controller.isBest = isBest;
  }

  public void resetBests() {
    this.controller.resetBests();
  }

  public PVector getBestPos() {
    return this.controller.bestPosition;
  }

  PVector getVelocity() {
    return this.velocity;
  }
}
