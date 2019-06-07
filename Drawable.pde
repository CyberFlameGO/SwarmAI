/**
 * The drawable abstract class describes an object that can
 * draw a representation of itself to the screen. To do this
 * it uses a renderer, which it calls in its draw() method.
 * 
 */
abstract class Drawable<R extends Renderer> {
    protected R renderer;
    public color objStroke, objFill;

    Drawable(R renderest, color strokeMe, color fillMe) {
        this.renderer = renderest;
        this.renderer.setTarget(this);
        
        this.objStroke = strokeMe;
        this.objFill = fillMe;
    }

    public void draw() {
        this.renderer.draw();
    }
}


/**
 * A drawable that's some kind of geometric shape with a 
 * position on-screen.
 */
abstract class Shape extends Drawable {

    PVector position;       // Defined on an individual basis–for example a circle uses the center while a rectangle uses a corner
    float mainDimension;    // The largest dimension. A circle's radius, a rectangle's long side, etc
    int sides;              // The number of sides the shape has–0 for a circle

    public Shape(Renderer renderest, color strokeMe, color fillMe, PVector position) {
        super(renderest, strokeMe, fillMe);
        this.position = position;
    }
}



// -------------------------------------------------------------
// Rendering Classes

/**
 * An abstract class to outline an object that draws
 * a drawable to the screen. Serves as a dependency 
 * injection for Drawables to use.
 *
 * (I found it most helpful to think about the renderer as
 * the "tool" the Drawable uses to draw itself)
 */
abstract class Renderer<T extends Drawable> {
    T target;

    abstract void draw() throws RenderTargetNotSetException;
    void setTarget(T target) {
        this.target = target;
    }

}


/**
 * RenderDot renders anything that inherits the Dot class.
 * As a result, it can handle drawing most objects that take
 * the shape of a circle.
 */
class RenderDot extends Renderer<Dot>  {
    
    public void draw() {
        if (target == null) {
            throw new RenderTargetNotSetException();
        }
        fill(target.objFill);
        stroke(target.objStroke);

        ellipse(target.position.x, target.position.y, target.radius, target.radius);
    }
}

class RenderTargetNotSetException extends RuntimeException {
        
    public RenderTargetNotSetException() {
        super("Failed to set a valid target for the renderer");
    }
}