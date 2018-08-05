package edu.cg.scene.lightSources;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.scene.objects.Surface;

public class DirectionalLight extends Light {
	private Vec direction = new Vec(0, -1, -1);

	public DirectionalLight initDirection(Vec direction) {
		this.direction = direction;
		return this;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Directional Light:" + endl + super.toString() +
				"Direction: " + direction + endl;
	}

	@Override
	public DirectionalLight initIntensity(Vec intensity) {
		return (DirectionalLight)super.initIntensity(intensity);
	}
	
	@Override
    public Vec rayAndPointIntesectionIntensity(Point hittingPoint, Ray ray) {
    	Vec normalizedDirection = direction.normalize();
        Vec negativeDirection = normalizedDirection.neg();
        Vec L = ray.direction();
        double dotProduct = negativeDirection.dot(L);
		Vec intensityResult = intensity.mult(dotProduct);
		return intensityResult;
    }
	
	@Override
    public boolean isBlocked(Surface surface, Ray ray) {
    	Hit intersection = surface.intersect(ray);
        if (intersection == null) 
        {
            return false;
        }
        return true;
    }
	
	@Override
    public Ray rayFromPointToLightSource(Point point) {
		Vec negativeDirection = direction.neg();
		Ray result = new Ray(point, negativeDirection);
        return result;
    }

}
