package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;
import edu.cg.Logger;
import edu.cg.algebra.*;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; //gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;

	private Point camera = new Point(0, 0, 5);
	private Vec ambient = new Vec(1, 1, 1); //white
	private Vec backgroundColor = new Vec(0, 0.5, 1); //blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();


	//MARK: initializers
	public Scene initCamera(Point camera) {
		this.camera = camera;
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	//MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator(); 
		return "Camera: " + camera + endl +
				"Ambient: " + ambient + endl +
				"Background Color: " + backgroundColor + endl +
				"Max recursion level: " + maxRecursionLevel + endl +
				"Anti aliasing factor: " + antiAliasingFactor + endl +
				"Light sources:" + endl + lightSources + endl +
				"Surfaces:" + endl + surfaces;
	}

	private static class IndexTransformer {
		private final int max;
		private final int deltaX;
		private final int deltaY;

		IndexTransformer(int width, int height) {
			max = Math.max(width, height);
			deltaX = (max - width) / 2;
			deltaY = (max - height) / 2;
		}

		Point transform(int x, int y) {
			double xPos = (2*(x + deltaX) - max) / ((double)max);
			double yPos = (max - 2*(y + deltaY)) / ((double)max);
			return new Point(xPos, yPos, 0);
		}
	}

	private transient IndexTransformer transformaer = null;
	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
	}


	public BufferedImage render(int imgWidth, int imgHeight, Logger logger)
			throws InterruptedException, ExecutionException {

		initSomeFields(imgWidth, imgHeight, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		transformaer = new IndexTransformer(imgWidth, imgHeight);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][])(new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " +
				(imgHeight*imgWidth*antiAliasingFactor*antiAliasingFactor) +
				" rays over " + name);

		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for(int y = 0; y < imgHeight; ++y)
			for(int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		transformaer = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			//TODO: change this method implementation to implement super sampling
			Point pointOnScreenPlain = transformaer.transform(x, y);
			Ray ray = new Ray(camera, pointOnScreenPlain);
			return calcColor(ray, 0).toColor();
		});
	}

	public Hit intersection(Ray ray) {
		Hit closestRayIntersection = null;
		for (int i = 0; i < surfaces.size(); i++) {
			Surface currentSurface = surfaces.get(i);
			Hit currentRayIntersection = currentSurface.intersect(ray);
			// If we are in the first iteration
			if(closestRayIntersection==null) {
				closestRayIntersection = currentRayIntersection;
			}
			// If currentRayIntersection<closestRayIntersection
			else
			{
				if(currentRayIntersection!=null && currentRayIntersection.compareTo(closestRayIntersection) < 0)
					closestRayIntersection = currentRayIntersection;
			}
		}
		return closestRayIntersection;
	}
	
	private boolean isBlocked(Light light, Ray ray) 
	{
		for (int i = 0; i < surfaces.size(); i++)
		{
			boolean isBlocked = light.isBlocked(surfaces.get(i), ray);
			if (isBlocked != true) 
			{
				continue;
			}
			else 
			{
				// If a surface is blocking the ray.
				return true;
			}
		}
		// If no surfaces are blocking the ray.
		return false;
	}

	private Vec getDiffuse(Hit closestRayIntersection, Ray ray) 
	{
		Vec lightDirection = ray.direction();
		Vec normal = closestRayIntersection.getNormalToSurface();
		Surface surface = closestRayIntersection.getSurface();
		Point lightSource = ray.source();
		Vec diffuseCoeffiecnt = surface.Kd(lightSource);
		double dotProduct = normal.dot(lightDirection);
		if(dotProduct <= 0)
			dotProduct = 0;
		Vec result = diffuseCoeffiecnt.mult(dotProduct);
		return result;
	}


	public Vec getSpec(Hit closestIntersection, Ray lightRay, Ray rayFromViewer) 
	{
		Vec ray = Ops.reflect(lightRay.direction().neg(), closestIntersection.getNormalToSurface());
		Vec specularCoefficent = closestIntersection.getSurface().Ks();
		Vec viewerRay = rayFromViewer.direction();
		Surface surface = closestIntersection.getSurface();
		int power = surface.shininess();
		viewerRay = viewerRay.neg();
		double dotProduct = ray.dot(viewerRay);
		Vec result;
		if(dotProduct < 0)
		{
			result = new Vec();
		}
		else
		{
			double poweredDotProduct = Math.pow(dotProduct, power);
			result = specularCoefficent.mult(poweredDotProduct);
		}
		return result;
	}

	private Vec calcColor(Ray ray, int recusionLevel) 
	{
		// Base case:
		if(recusionLevel >= maxRecursionLevel) {
			Vec baseVector= new Vec();
			return baseVector;
		}

		Hit closestRayIntersection = this.intersection(ray);
		//if there is no intersection
		if(closestRayIntersection == null) {
			return backgroundColor;
		}

		Point closestIntersectionPoint = ray.getHittingPoint(closestRayIntersection);
		Surface closestRayIntersectionSurface = closestRayIntersection.getSurface();
		Vec resultColor = closestRayIntersectionSurface.Ka().mult(ambient);
		for (int i = 0; i < this.lightSources.size(); i++) {
			Light light = lightSources.get(i);
			Ray lightRay = light.rayFromPointToLightSource(closestIntersectionPoint);
			if (isBlocked(light, lightRay)) continue;
			else {
				Vec diffuseColor = getDiffuse(closestRayIntersection, lightRay);
				diffuseColor = diffuseColor.add(this.getSpec(closestRayIntersection, lightRay, ray));
				Vec Il = light.rayAndPointIntesectionIntensity(closestIntersectionPoint, lightRay);
				resultColor = resultColor.add(diffuseColor.mult(Il));
			}
		}
		if (renderReflections == true) {
			Vec reflectionWeightKs = closestRayIntersectionSurface.Ks();
			double refIntensity = closestRayIntersectionSurface.reflectionIntensity();
			Vec reflectionWeight  = reflectionWeightKs.mult(refIntensity);
			Vec rayDirection = ray.direction();
			Vec normal = closestRayIntersection.getNormalToSurface();
			Vec reflectionDirection = Ops.reflect(rayDirection, normal);
			Ray newRay = new Ray(closestIntersectionPoint, reflectionDirection);
			int nextRecursionLevel = recusionLevel++;
			Vec tempColor = calcColor(newRay, nextRecursionLevel);
			Vec reflectionColor = tempColor.mult(reflectionWeight);
			resultColor = resultColor.add(reflectionColor);
		}
		return resultColor;
	}
}
