/**
 * The drawable abstract class describes an object that can
 * draw a representation of itself to the screen. To do this
 * it uses a renderer, which it calls in its draw() method.
 */
abstract class Drawable {
    private Renderer renderer;
    color objStroke, objFill;
    PVector position;

    Drawable(Renderer renderest, color strokeMe, color fillMe, PVector position) {
        this.renderer = renderest;
        this.renderer.setTarget = this;
        
        this.objStroke = strokeMe;
        this.objFill = fillMe;
        this.position = position;
    }

    public void draw() {
        this.renderer.draw();
    }
}

/**
 * An abstract class to outline an object 
 */
abstract class Renderer<T extends Drawable> {
    T target;

    public Renderer(T target) {
        this.target = target;
    }

    abstract void draw();
    abstract void setTarget(T target);
}

class RenderDot<T extends Dot> extends Renderer {

    T target;

    public RenderDot(T target){
        super(target);
    }
    
    public void draw() {
        fill(target.objFill);
        stroke(target.objStroke);

        ellipse(target.position.x, target.position.y, target.radius, target.radius);
    }
}


