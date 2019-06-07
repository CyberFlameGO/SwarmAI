interface Moveable {

  public void move();
  public void update();
  public PVector getVelocity();

}

public static Accelerator genAccelerator(String stepType) {
  stepType = stepType.toLowerCase();
  if (stepType.equals("full model")) {
    return new FullModel();
  } else if (stepType.equals("cognitive only")) {
    return new CogOnly();
  } else if (stepType.equals("social only")) {
    return new SocOnly();
  } else {
    throw new IllegalArgumentException();
  }
}


// -------------------------------------------------------
// Accelerator generic abstract class and its implementations
abstract class Accelerator<T extends Moveable> {

  T d;

  public void setTarget(T d) {
    this.d = d;
  }
  public abstract void updateVelocity();

}

// Don't need the generics???
class FullModel extends Accelerator<SwarmingDot> {
    
  public void updateVelocity() {

    float r1 = random(1);
    float r2 = random(1);
    
    PVector momentum = PVector.mult(this.d.vel, INERTIA);
    PVector cognitive = (PVector.sub(this.d.bestPosition, this.d.position)).mult(COG_CONST * r1); 
    PVector social = (PVector.sub(this.d.parent.gDotBest.position, this.d.position)).mult(SOC_CONST * r2);

    this.d.vel = PVector.add(momentum, cognitive).add(social);
    this.d.vel.limit(SPEED_LIMIT);
  }
}


class CogOnly<T extends SwarmingDot> extends Accelerator {

  public void update_velocity() {

    float r1 = random(1); // Keeping names in convention, r1 is the random number multiplied with the cognitive constant
    
    PVector momentum = PVector.mult(this. d.vel, INERTIA);
    PVector cognitive = (PVector.sub(this.d.bestPosition, this.d.position)).mult(COG_CONST * r1);
    
    this.d.vel = PVector.add(momentum, cognitive);
    this.d.vel.limit(SPEED_LIMIT);
  }
}


class SocOnly<T extends SwarmingDot> extends Accelerator {

  public void update_velocity() {
    float r2 = random(1); // Keeping names in convention, r2 is the random number multiplied with the social constant
    
    PVector momentum = PVector.mult(this.d.vel, INERTIA);
    
    PVector social = (PVector.sub(this.d.parent.gDotBest.position, this.d.position)).mult(SOC_CONST * r2);
    
    this.d.vel = PVector.add(momentum, social);
    this.d.vel.limit(SPEED_LIMIT);
  }
}
