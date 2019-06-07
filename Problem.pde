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
    return (.25 * val) * (sin(.025 * val));
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
