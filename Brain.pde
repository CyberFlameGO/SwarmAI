abstract class Brain {
    Moveable thinker;
    abstract void evaluate();
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
    abstract float evaluate() {
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