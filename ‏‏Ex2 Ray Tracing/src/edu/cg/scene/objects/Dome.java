package edu.cg.scene.objects;


import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.algebra.*;

public class Dome extends Shape {
	private Sphere sphere;
	private Plain plain;
	
	public Dome() {
		sphere = new Sphere().initCenter(new Point(0, -0.5, -6));
		plain = new Plain(new Vec(-1, 0, -1), new Point(0, -0.5, -6));
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Dome:" + endl + 
				sphere + plain + endl;
	}

    private Hit insideOfTheDomeIntersection(Ray ray, Hit intersection) {
        Point intersectionPoint = ray.getHittingPoint(intersection);
        double distanceFromSource = plain.substitute(ray.source());
        double distanceFromIntersectionPoint = plain.substitute(intersectionPoint);
		if (distanceFromSource <= Ops.epsilon) 
		{
			if (distanceFromIntersectionPoint > 0)
			{
                return intersection;
            }
			
            intersection = plain.intersect(ray);
            if (intersection == null) 
            {
                return null;
            }
            else
            {
            	return intersection.setWithin();
            }

		}
		else
		{
			if (distanceFromIntersectionPoint > 0) {
	            return plain.intersect(ray);
	        }
	        return null;
		}
        
    }

    private Hit outSideOfTheDomeIntersection(Ray ray, Hit intersection)
    {
        Point intersectionPoint = ray.getHittingPoint(intersection);
        if (plain.substitute(intersectionPoint) <= 0) {
            return intersection;
        }
        intersection = plain.intersect(ray);
        if (intersection == null) {
            return null;
        }
        else
        {
        	intersectionPoint = ray.getHittingPoint(intersection);
            if (sphere.substitute(intersectionPoint) <= 0) {
                return intersection;
            }
            return null;
        }
    }
    
    @Override
    public Hit intersect(Ray ray) {
        Hit intersection = sphere.intersect(ray);
        Hit result;
        if (intersection == null) 
        {
            return null;
        }
        
        if(intersection.isWithinTheSurface()) 
        {
        	result = insideOfTheDomeIntersection(ray, intersection);
        }
        else
        {
        result = outSideOfTheDomeIntersection(ray, intersection);	
        }
        return result;
    }
}
