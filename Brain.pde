abstract class Controller<T, P extends Population> {
    T thinker;

    public Controller(T thinker) {
        this.thinker = thinker;
    }
    abstract float evaluate(PVector position, PVector velocity, P container);
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
    float evaluate(PVector position, PVector velocity, SwarmPopulation flock) {
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