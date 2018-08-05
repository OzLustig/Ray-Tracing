package edu.cg.scene.lightSources;

import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class Spotlight extends PointLight {
	private Vec direction;
	private double angle = 0.866; //cosine value ~ 30 degrees
	
	public Spotlight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}
	
	public Spotlight initAngle(double angle) {
		this.angle = angle;
		return this;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Spotlight: " + endl +
				description() + 
				"Direction: " + direction + endl +
				"Angle: " + angle + endl;
	}
	
	@Override
	public Spotlight initPosition(Point position) {
		return (Spotlight)super.initPosition(position);
	}
	
	@Override
	public Spotlight initIntensity(Vec intensity) {
		return (Spotlight)super.initIntensity(intensity);
	}
	
	@Override
	public Spotlight initDecayFactors(double q, double l, double c) {
		return (Spotlight)super.initDecayFactors(q, l, c);
	}
	
	@Override
    public Vec rayAndPointIntesectionIntensity(Point intersectionPoint, Ray ray) {
    	Vec normalizedDirection = direction.normalize();
        Vec negativeDirection = normalizedDirection.neg();
        Vec rayDirection = ray.direction();
        Vec rayAndPointIntersectionIntensity = super.rayAndPointIntesectionIntensity(intersectionPoint, ray);
		double proejction = negativeDirection.dot(rayDirection);
		Vec resultIntensity = rayAndPointIntersectionIntensity.mult(proejction);
		return resultIntensity;
    }
	@Override
    public boolean isBlocked(Surface surface, Ray ray) 
	{
		Vec rayNegativeDirection =  ray.direction().neg();
        Vec normalizedDirection = direction.normalize();
		double dotProduct = rayNegativeDirection.dot(normalizedDirection);
		if (dotProduct  >= angle) {
            boolean isBlocked = super.isBlocked(surface, ray);
			return isBlocked;
        }
        return true;
    }
}
