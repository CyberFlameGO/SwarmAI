class Dot {
  PVector position;
  PVector vel;
  PVector bestPosition;

  float fit = 0;
  float fitBest;
  float c1 = 1; // Cognitive constant
  float c2 = 1; // Social constant
  float r = random(256);
  float g = random(256);
  float b = random(256);

  boolean dead = false;
  Population parent;



  Dot(Population parent) {
    int xDistr = (int) random(width);
    int yDistr = (int) random(height);
    
    this.parent = parent;
    position = new PVector(xDistr, yDistr);
    bestPosition = position;
    vel = PVector.random2D();
    vel.limit(SPEED_LIMIT);
    fitBest = -1;
  }

  //--------------------------------------------------------------------------------

  void show() {
    fill(r, g, b);
    stroke(r, g, b);
    ellipse(position.x, position.y, 4, 4);
    if (parent.gDotBest == this) ellipse(position.x, position.y, 20, 20);
  }

  //---------------------------------------------------------------------------------
  void move() {
    if (!dead) {
      this.position = PVector.add(position, vel);
    }
  }

  //---------------------------------------------------------------------------------   
  /** 
   * Update the internal velocity according to the fitness value.
   * Chnages internal state
   */
  void update_vel() {

    if (position.x < 2 || position.y < 2 || position.x > width - 2 || position.y > height - 2) {
      this.dead = true;
    }

    float r1 = random(1);
    float r2 = random(1);

    PVector momentum = PVector.mult(this.vel, INERTIA);

    PVector cognitive = (PVector.sub(this.bestPosition, this.position)).mult(this.c1 * r1);

    PVector social = (PVector.sub(parent.gDotBest.position, this.position)).mult(this.c2 * r2);


    this.vel = PVector.add(momentum, cognitive).add(social);
    this.vel.limit(SPEED_LIMIT);
  }

  //--------------------------------------------------------------------------------

  /**
   * Evaluate the fitness and update the internal state.
   * If the fitness is better than the current best,
   * replace the current best with the fitness
   */
  float evaluate(PVector goal) {
    this.fit = parent.eval.evalFunction(goal, this.vel, this.position);  // Calculate fitness using the fitness function
    
    if (this.fit < this.fitBest || fitBest == -1) {                      // If the new fitness value is better than the previous best:
      this.fitBest = this.fit;                                           //   replace the previous best fitness
      this.bestPosition = this.position;                                 //   replace the previous best position
      
      if (this.fitBest < parent.gDotBest.fitBest) {                      // If the fitness (guaranteed to differ) is better than the
        parent.gDotBest = this;                                          // best dot's fitness, replace that dot with this one
      }
    }
    return this.fit;
  }
}
