package edu.cg.scene.objects;

import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;

	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}

	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}

	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}

	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}

	
	public double substitute(Point point) {
		double distance = point.distSqr(this.center);
		return distance  - Math.pow(radius, 2);
	}
	
	private Vec normal(Point point) {
		Vec pointCenter = point.sub(this.center);
		return pointCenter.normalize();
	}

	@Override
	public Hit intersect(Ray ray) 
	{
		double eqParam, firstSolution, secondSolution;
		boolean isInside = false;
		Point raySource = ray.source();
		Vec rayDirection = ray.direction();
		Vec sourceToCenterVector = raySource.sub(center);
		Vec rayDirectionMultipliedByTwo = rayDirection.mult(2);
		double b = rayDirectionMultipliedByTwo.dot(sourceToCenterVector);
		double bSquared = Math.pow(b,2);
		double d = Math.sqrt(bSquared - ((substitute(raySource)*4)));
		if (Double.isNaN(d) == false) 
		{
			firstSolution = (- b - d) / 2;
			secondSolution = (- b + d) / 2;
			if (secondSolution >= Ops.epsilon) 
			{
				eqParam = firstSolution;
				Vec normalToSurface = normal(ray.add(firstSolution));
				if (firstSolution < Ops.epsilon) 
				{
					isInside = true;
					Vec negativeNormalToSurface = normal(ray.add(secondSolution));
					normalToSurface = negativeNormalToSurface.neg();
					eqParam = secondSolution;
				}
				if (eqParam <= Ops.infinity) 
				{
					Hit resultIntersection = new Hit(eqParam, normalToSurface);
					resultIntersection.setIsWithin(isInside);
					return resultIntersection;
				}
				else
				{
					return null;
				}
			}
			else
			{
				return null;
			}
		}
		else
		{
			return null;
		}
	}

}
