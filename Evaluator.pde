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
    return val != 0? 6*log(val) - (.005 * pow(val, 2)): 0;
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
