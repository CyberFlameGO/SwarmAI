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
  void show() {
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

      color shade = color(
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
  void update() {
    for (SwarmingDot d : this.members) {    // Iterate through the population of members
      d.update();
      d.move();
    }
  }

  void setBest(SwarmingDot d) {
    this.gDotBest.setAsBest(false);
    d.setAsBest(true);
    this.gDotBest = d;
  }

  void reset () {
    for (SwarmingDot d : this.members) {
      d.resetBests();
    }
  }
}