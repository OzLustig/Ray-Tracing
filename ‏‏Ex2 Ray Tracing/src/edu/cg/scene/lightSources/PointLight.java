package edu.cg.scene.lightSources;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class PointLight extends Light {
	protected Point position;
	
	//Decay factors:
	protected double kq = 0.01;
	protected double kl = 0.1;
	protected double kc = 1;
	
	protected String description() {
		String endl = System.lineSeparator();
		return "Intensity: " + intensity + endl +
				"Position: " + position + endl +
				"Decay factors: kq = " + kq + ", kl = " + kl + ", kc = " + kc + endl;
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Point Light:" + endl + description();
	}
	
	@Override
	public PointLight initIntensity(Vec intensity) {
		return (PointLight)super.initIntensity(intensity);
	}
	
	public PointLight initPosition(Point position) {
		this.position = position;
		return this;
	}
	
	public PointLight initDecayFactors(double kq, double kl, double kc) {
		this.kq = kq;
		this.kl = kl;
		this.kc = kc;
		return this;
	}


	@Override
	public Vec rayAndPointIntesectionIntensity(Point intersectionPoint, Ray ray) {
		 double distance = intersectionPoint.dist(position);
	     double decayFormula = distance*(kl + kq * distance) + kc;
	     decayFormula = 1 / decayFormula;
	     return intensity.mult(decayFormula);
	}
	
	@Override
	public Ray rayFromPointToLightSource(Point pointToLight) {
		Ray resultRay = new Ray (pointToLight, position);
		return resultRay;
	}

	@Override
	public boolean isBlocked(Surface surface, Ray ray) {
		Hit surfaceRayIntersectionHit = surface.intersect(ray);
        if (surfaceRayIntersectionHit == null) 
        {
            return false;
        }
        Point sourcePoint = ray.source();
        Point surfaceRayIntersectionHitPoint = ray.getHittingPoint(surfaceRayIntersectionHit);
        double distancefromPosition = sourcePoint.dist(position);
        double distancefromIntersectionPoint = sourcePoint.distSqr(surfaceRayIntersectionHitPoint);
        if (distancefromPosition <= distancefromIntersectionPoint) {
            return false;
        }
        return true;
	}


}
