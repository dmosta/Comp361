package Math;

/**
 * Custom math class that provides two versions of operations. The 'self' version, eg selfAdd,
 * modifies the object on which it is called. The other version returns a new object with the modification.
 * So if Vector a=new Vector(0,0), Vector b=new Vector(1,1), then a.selfAdd(b) changes a to (1,1),
 * but a.add(b) does not modify a but returns a new vector equivalent to a+b.
 */
public class Vector {
	
	private double x;
	private double y;
	
	public Vector(double x, double y){
		set(x,y);
	}
	
	public void set(double x, double y){
		this.x=x;
		this.y=y;
	}
	
	public Vector(){
		set(0,0);
	}
	
	public Vector(Vector vec){
		set(vec.getX(), vec.getY());
	}
	
	public void selfAdd(Vector vec){
		set(x+vec.getX(), y+vec.getY());
	}
	
	public Vector add(Vector vec){
		Vector result=new Vector(this);
		result.selfAdd(vec);
		return result;
	}
	
	public void selfSub(Vector vec){
		set(x-vec.getX(), y-vec.getY());
	}
	
	public Vector sub(Vector vec){
		Vector result=new Vector(this);
		result.selfSub(vec);
		return result;
	}
	
	public void selfScale(double scl){
		set(x*scl, y*scl);
	}
	
	public Vector scale(double scl){
		Vector result=new Vector(this);
		result.selfScale(scl);
		return result;
	}
	
	public void normalize(){
		double scale=1.0/length();
		set(x*scale, y*scale);
	}
	
	public double length2(){
		return x*x+y*y;
	}
	
	public double length(){
		return Math.sqrt(x*x+y*y);
	}
	
	public void setX(double x){
		this.x=x;
	}
	
	public void setY(double y){
		this.y=y;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public double distanceTo(Vector other){
		Vector dist=other.sub(this);
		return dist.length();
	}
	
	public double getAngleDegrees(){
		double angle=Math.toDegrees(Math.atan(y/x));
		if(y<=0)
			angle+=180;
		return angle;
	}
	
	@Override
	public String toString() {
		return "("+x+", "+y+")";
	}
}
