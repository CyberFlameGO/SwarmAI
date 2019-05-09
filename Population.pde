class Population {
  Dot[] dots;
  Dot gDotBest;
  
  float gFitBest = -1;
  
  PVector gPosBest;
  PVector goal;

  Population(int size, PVector goal) {
    dots = new Dot[size]; 
    this.goal = goal;
    for (int i = 0; i < size; i++) {
      dots[i] = new Dot(this);
    }
    gDotBest = dots[0];
  }

  //---------------------------------------------------------------
  void show() {
    for (Dot d : dots) {
      d.show();
    }
  }

  //---------------------------------------------------------------
  void update(PVector goal) {
    this.goal = goal;
    evalMoveGroup();
  }

  /**
   * Loop through the collection of particles and have each one evaluate itself.
   * For each dot, call its eval function, then test if its best
   * fitness is better than the global best. If it is, update the global
   * best and global best positions.
   */
  void evalMoveGroup() {
    for (Dot d : this.dots) {    // Iterate through the population of dots
      
      if (!d.dead) d.evaluate(this.goal);     // Evaluate the current dot's fitness if it's still alive
      
      d.update_vel();    // Update the current dot's velocity
      d.move();
      
    }
  }

  void reset () {
    for (Dot d : this.dots) {
      d.bestPosition = d.position;
      d.fitBest = d.fit;
    }
    // Don't need to change the global best fitness and position
    // because we track those by aliasing the dot that generates those
  }
}
