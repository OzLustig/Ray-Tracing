package edu.cg.scene.objects;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Triangle extends Shape {
	private Point p1, p2, p3;
	public Plain plain = null;
	public Triangle() {
		p1 = p2 = p3 = null;
	}
	
	public Triangle(Point p1, Point p2, Point p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Triangle:" + endl +
				"p1: " + p1 + endl + 
				"p2: " + p2 + endl +
				"p3: " + p3 + endl;
	}

    private boolean isInside(Hit intersection, Ray ray){
        Vec p1RayVector = p1.sub(ray.source());
        Vec p2RayVector = p2.sub(ray.source());
        Vec p3RayVector = p2.sub(ray.source());
        Vec VectorProductBetweenP1VAndP2V = p2RayVector.mult(p1RayVector);
        
		double P1RayP2RayLength = p2RayVector.mult(p1RayVector).length();
		Vec normalizedVectorProductBetweenP1VAndP2V = VectorProductBetweenP1VAndP2V .mult(1 / P1RayP2RayLength );
        double P3RayP2RayLength = p3RayVector.mult(p2RayVector).length();
		Vec normalizedVectorProductBetweenP3VAndP2V = p3RayVector.mult(p2RayVector).mult(1 / P3RayP2RayLength );
        double P2RayP1RayLength = p1RayVector.mult(p2RayVector).length();
		Vec normalizedVectorProductBetweenP2VAndP1V = p1RayVector.mult(p2RayVector).mult(1 / P2RayP1RayLength);
		
        Point intersectionPoint = ray.getHittingPoint(intersection);
		Vec sourceIntersectionVector = intersectionPoint.sub(ray.source());

        double sourceP1P2DotProduct = sourceIntersectionVector.dot(normalizedVectorProductBetweenP1VAndP2V);
        double sourceP3P2DotProduct = sourceIntersectionVector.dot(normalizedVectorProductBetweenP3VAndP2V);
		double sourceP2P1DotProduct =sourceIntersectionVector.dot(normalizedVectorProductBetweenP2VAndP1V);
		boolean isInside;
		if (sourceP1P2DotProduct >= 0)
		{
            
			isInside = (sourceP3P2DotProduct >= 0) && (sourceP2P1DotProduct >= 0);
        }
        else
        {
        	isInside = (sourceP3P2DotProduct < 0) && (sourceP2P1DotProduct < 0);
        }
		return isInside;

    }
    
    @Override
    public Hit intersect(Ray ray) 
    {
        Hit intersection = plain().intersect(ray);
        boolean isInside = isInside(intersection, ray);
		if (intersection != null && isInside ) 
		{
            return intersection;
        }
		else 
		{
			return null;	
		}
    }
    
    private Plain plain() 
	{
		// If this is the first time the Triangle is being initialized.
        if (this.plain == null) 
        {
            Vec nomal = p2.sub(p1).cross(p3.sub(p1)).normalize();
            plain = new Plain(nomal, p1);
            return plain;
        }
        else
        {
        	return plain;
        }
    }
}
