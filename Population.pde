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
  void show() {
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
  void update() {
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

      color shade = color(
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
  void show() {
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
  void update() {
    for (SwarmingDot d : this.members) {    // Iterate through the population of members

      d.evaluate();     // Evaluate the current dot's fitness if it's still alive

      d.update_velocity();
      d.move();
    }
  }

  void setBest(SwarmingDot d) {
    this.gDotBest.isBest = false;
    d.isBest = true;
    this.gDotBest = d;
  }

  void reset () {
    for (SwarmingDot d : this.members) {
      d.bestPosition = d.position;
      d.fitBest = d.fit;
    }
  }
}