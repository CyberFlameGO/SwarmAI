/**
 * Dot describes a drawable object that will display as a circle of a fixed size.
 * The drawable class adds a renderer object and a draw method that calls the
 * renderer to draw to the screen.
 */
class Dot extends Drawable {
  int radius;
  Population container;

  Dot(Population container, int radius, 
      Renderer renderest, color strokeMe, color fillMe, PVector position) {

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

  void show() {
    this.renderer.draw();
  }
  
  void update_velocity() {

    this.accelerator.update_velocity();
  }

  //---------------------------------------------------------------------------------
  void move() {
    this.position = PVector.add(position, vel);
  }

  //--------------------------------------------------------------------------------

  /**
   * Evaluate the fitness and update the internal state.
   * If the fitness is better than the current best,
   * replace the current best with the fitness
   */
  abstract float evaluate() {
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

    public SwarmingDot(String accStepType, Population container, int radius, Renderer renderest, color strokeMe, color fillMe, PVector position) {
        
        super(container, radius, renderest, shade, shade, position);

        this.accelerator = Velociraptor.getAccelerator(accStepType);
    }
    
}