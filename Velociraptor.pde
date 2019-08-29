interface Moveable<P extends Controller> {

  public void move();
  public void update();
  public PVector getVelocity();

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
    
    PVector momentum = PVector.mult(this.d.getVelocity(), INERTIA);
    PVector cognitive = (PVector.sub(this.d.getBestPos(), this.d.getPosition())).mult(COG_CONST * r1); 
    PVector social = (PVector.sub(this.d.getContainer().getGBestPos(), this.d.getPosition())).mult(SOC_CONST * r2);

    this.d.setVelocity(PVector.add(momentum, cognitive).add(social));
  }
}


class CogOnly extends Accelerator<SwarmingDot> {

  public void updateVelocity() {

    float r1 = random(1); // Keeping names in convention, r1 is the random number multiplied with the cognitive constant
    
    PVector momentum = PVector.mult(this. d.getVelocity(), INERTIA);
    PVector cognitive = (PVector.sub(this.d.getBestPos(), this.d.getPosition())).mult(COG_CONST * r1);
    
    this.d.setVelocity(PVector.add(momentum, cognitive));
  }
}


class SocOnly extends Accelerator<SwarmingDot> {

  public void updateVelocity() {
    float r2 = random(1); // Keeping names in convention, r2 is the random number multiplied with the social constant
    
    PVector momentum = PVector.mult(this.d.getVelocity(), INERTIA);
    
    PVector social = (PVector.sub(this.d.getContainer().getGBestPos(), this.d.getPosition())).mult(SOC_CONST * r2);
    
    this.d.setVelocity(PVector.add(momentum, social));
  }
}
