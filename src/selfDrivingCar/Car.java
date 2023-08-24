package selfDrivingCar;

import java.awt.*;
import java.util.*;

public class Car {
	public int x;
	private int x1;
	public int y;
	private int y1;
	public boolean bestCar;
	public Color normalColor;
	public Color damagedColor;
	public Color currentColor;
	private int freezedFramesMaxCount = 135;
	private int freezedFrames;
	protected int width;
	protected int height;
	public int roadListYIndexAreaMax;
	public int roadListYIndexAreaMin;
	private int toBestCarMaxDistance;
	private double friction = 0.05;
	private double speed;
	private double acceleration = 0.2;
	private double maxSpeed;
	public int[] layersNNetwork;
	public double angle;
	private double angleSpeed;
	private double angleMaxSpeed = 0.09;
	private double angleAcceleration = angleMaxSpeed / 2;
	private double[] offsets;
	public boolean damaged;
	public boolean useBrain;
	public boolean hunamDrives;
	private int raysCount;
	private double raysSpreadAngle;
	public int[] xDotsPolygonCoords;
	public int[] yDotsPolygonCoords;
	public int[] xDotsCachedPolygonCoords;
	public int[] yDotsCachedPolygonCoords;
	public Sensor sensor;
	public Image carImage;
	public Image bestCarImage;
	public NNetwork brain;
	private Controls controls;

	public Car(int x, int y, int carWidth, int carHeight, int raysCount, double raysSpreadAngle,
			int carDecisionCount, String NNLayersInput, String controlType, double maxSpeed, Color color,
			gameSelfDrivingCar gameclass) {
		this.x = x;
		this.roadListYIndexAreaMax = 0;
		this.roadListYIndexAreaMin = 0;
		this.x1 = this.x;
		this.y = y;
		this.bestCar = false;
		this.normalColor = color;
		this.currentColor = this.normalColor;
		this.damagedColor = Color.red;
		this.raysCount = raysCount;
		this.raysSpreadAngle = raysSpreadAngle;
		this.y1 = this.y;
		this.angle = -Math.PI / 2;
		this.freezedFrames = 0;
		this.angleSpeed = 0;
		this.width = carWidth;
		this.height = carHeight;
		this.xDotsPolygonCoords = new int[4];
		this.yDotsPolygonCoords = new int[4];
		this.xDotsCachedPolygonCoords = new int[4];
		this.yDotsCachedPolygonCoords = new int[4];
		createPolygon();
		this.speed = 0;
		this.maxSpeed = maxSpeed;
		this.toBestCarMaxDistance = this.height * 10;
		this.damaged = false;
		this.useBrain = controlType.equalsIgnoreCase("AI");
		this.hunamDrives = controlType.equalsIgnoreCase("KEYS");

		ArrayList<Integer> NNLayers = new ArrayList<Integer>();
		if (!NNLayersInput.equals("")) {
			NNLayers.add(raysCount + 1);
			String[] sttringToArray = NNLayersInput.split(",");
			for (String number : sttringToArray) {
				NNLayers.add(Integer.parseInt(number));
			}
			;
			NNLayers.add(carDecisionCount);
		} else {
			NNLayers.add(raysCount + 1);
			NNLayers.add(carDecisionCount);
		}
		layersNNetwork = new int[NNLayers.size()];
		layersNNetwork = NNLayers.stream().mapToInt(d -> d).toArray();
		if (!controlType.equalsIgnoreCase("DUMMY")) {
			this.sensor = new Sensor(this.x, this.y, this.raysCount, this.angle, this.raysSpreadAngle, this.height);
			this.brain = new NNetwork(layersNNetwork, gameclass.FLOP);
		}
		this.controls = new Controls(controlType, gameclass);
		this.carImage = imageProcessor.getImage(this.currentColor, gameclass.random).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
		this.bestCarImage = imageProcessor.getImage(Color.GREEN, gameclass.random).getScaledInstance((int) carWidth,
				(int) carHeight, Image.SCALE_DEFAULT);
	}

	protected void createPolygon() {
		double rad = Math.hypot(this.width, this.height) / 2;
		double alpha = Math.atan2(this.height, this.width);

		this.xDotsPolygonCoords[0] = (int) Math.round(this.x - Math.sin(Math.PI - this.angle - alpha) * rad);
		this.yDotsPolygonCoords[0] = (int) Math.round(this.y - Math.cos(Math.PI - this.angle - alpha) * rad);
		this.xDotsPolygonCoords[1] = (int) Math.round(this.x - Math.sin(Math.PI - this.angle + alpha) * rad);
		this.yDotsPolygonCoords[1] = (int) Math.round(this.y - Math.cos(Math.PI - this.angle + alpha) * rad);
		this.xDotsPolygonCoords[2] = (int) Math.round(this.x - Math.sin(-this.angle - alpha) * rad);
		this.yDotsPolygonCoords[2] = (int) Math.round(this.y - Math.cos(-this.angle - alpha) * rad);
		this.xDotsPolygonCoords[3] = (int) Math.round(this.x - Math.sin(-this.angle + alpha) * rad);
		this.yDotsPolygonCoords[3] = (int) Math.round(this.y - Math.cos(-this.angle + alpha) * rad);
	}

	public void carMove(gameSelfDrivingCar gameClass) {
		this.currentColor = (!this.damaged) ? this.normalColor : this.damagedColor;
		move(gameClass);
		if (this.useBrain || this.hunamDrives) {
			this.getroadArea(gameClass.road.roadMiddleLaneCoordsList, this);
			this.damaged = accessDamage(gameClass.road.roadMiddleLaneCoordsList, gameClass.traffic, gameClass)
					|| isTotallyFreezed(this, isFreezed(this)) || isOutsider(gameClass, this);
			if (this.damaged && this.hunamDrives) {
			this.damaged = false;
			}
		}
		if (this.sensor != null) {
			this.sensor.castCarRays(this.x, this.y, this.angle);
			this.sensor.getReading(this, gameClass);
			offsets = new double[this.layersNNetwork[0]];
			for (int i = 0; i < offsets.length - 1; ++i) {
				if (this.sensor.rays.get(i).intersectionPoint == null) {
					offsets[i] = 0;
				} else {
					offsets[i] = 1 - this.sensor.rays.get(i).intersectionPoint.getOffset();
				}
			}
			offsets[offsets.length - 1] = this.speed / this.maxSpeed;
			int[] outputs = new int[this.brain.NNlevels[this.brain.NNlevels.length - 1].levelOutputs.length];
			outputs = this.brain.feedForward(offsets, this.brain);
			if (this.useBrain) {
				if (this.useBrain) {
					this.controls.forward = (outputs[0] == 0) ? false : true;
					this.controls.left = (outputs[1] == 0) ? false : true;
					this.controls.right = (outputs[2] == 0) ? false : true;
					this.controls.reverse = (outputs[3] == 0) ? false : true;
				}
			}
		}
	}

	private boolean isOutsider(gameSelfDrivingCar gameClass, Car car) {
		if (gameClass.bestCar != null) {
			return !car.bestCar && car.y - gameClass.bestCar.y > car.toBestCarMaxDistance;
		} else {
			return false;
		}
	}

	private boolean isTotallyFreezed(Car car, boolean freezed) {
		if (freezed) {
			car.freezedFrames++;
		} else {
			car.freezedFrames = 0;
		}
		;
		if (car.freezedFrames >= car.freezedFramesMaxCount) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isFreezed(Car car) {

		if (car.x == car.x1 && car.y == car.y1) {
			return true;
		}
		car.x1 = car.x;
		car.y1 = car.y;
		return false;
	}

	private void getroadArea(ArrayList<ArrayList<Double>> roadMiddleLaneCoordsList, Car car) {
		double yInListMin = car.y + car.sensor.rayLength + car.height;
		double yInListMax = car.y - car.sensor.rayLength - car.height;
		car.roadListYIndexAreaMax = roadMiddleLaneCoordsList.size() - 1;
		car.roadListYIndexAreaMin = 0;
		int currentIndexmaxY = car.roadListYIndexAreaMax;
		int currentIndexminY = car.roadListYIndexAreaMin; // Indexes of List to use in intersections searches
		int currentIndexmiddleY = 0;
		double target = 0;

		for (int i = 0; i < 2; ++i) {
			int storedIndex = 0;
			if (i == 0) {
				target = yInListMin;
				currentIndexminY = car.roadListYIndexAreaMin;
			} else {
				target = yInListMax;
				storedIndex = 0;
				currentIndexmaxY = car.roadListYIndexAreaMax;
			}
			while (currentIndexmaxY != currentIndexminY) {
				currentIndexmiddleY = (currentIndexmaxY + currentIndexminY) / 2;
				double middleValueY = roadMiddleLaneCoordsList.get(currentIndexmiddleY).get(1);
				if (target < middleValueY) {
					currentIndexminY = currentIndexmiddleY;

				} else if (target > middleValueY) {
					currentIndexmaxY = currentIndexmiddleY;
				} else {
					currentIndexmaxY = currentIndexmiddleY;
					currentIndexminY = currentIndexmaxY;
				}
				if (storedIndex == currentIndexmiddleY) {
					currentIndexmaxY = currentIndexmiddleY;
					currentIndexminY = currentIndexmaxY;
				}
				storedIndex = currentIndexmiddleY;
			}
			if (i == 0) {
				car.roadListYIndexAreaMin = currentIndexmiddleY;
			} else {
				car.roadListYIndexAreaMax = currentIndexmiddleY;
			}
			if (car.roadListYIndexAreaMax == 0 && car.roadListYIndexAreaMin == 0) {
				car.roadListYIndexAreaMax += 1;
			}
			if (car.roadListYIndexAreaMax == roadMiddleLaneCoordsList.size() - 2
					&& car.roadListYIndexAreaMin == roadMiddleLaneCoordsList.size() - 2) {
				car.roadListYIndexAreaMax += 1;
			}
		}
	}

	private boolean accessDamage(ArrayList<ArrayList<Double>> roadBorders, ArrayList<Car> traffic,
			gameSelfDrivingCar gameclass) {

		for (int paintLineX = 0; paintLineX < roadBorders.get(0).size(); paintLineX += 2) {
			if (Utils.polysIntersect(this, roadBorders, paintLineX,
					null, gameclass)) {
				return true;
			}
		}
		if (traffic.size() > 0) {
			for (Car traffBot : traffic) {
				if (Utils.polysIntersect(this, null, 0,
						traffBot, gameclass)) {
					return true;
				}
			}
		}
		return false;
	}

	private void move(gameSelfDrivingCar gameclass) {
		this.controls.addKeyboardListeners(gameclass);
		if (this.controls.forward) {
			this.speed += acceleration;
		} else if (this.controls.reverse) {
			this.speed -= acceleration;
		}

		if (this.speed != 0) {
			int flip = (this.speed > 0) ? -1 : 1;
			this.angle -= this.angleSpeed * flip;
			if (this.controls.left) {
				this.angleSpeed -= this.angleAcceleration;
			} else if (this.controls.right) {
				this.angleSpeed += this.angleAcceleration;
			}
		}
		if (this.speed > this.maxSpeed) {
			this.speed = this.maxSpeed;
		}
		if (this.speed < -this.maxSpeed / 2) {
			this.speed = -this.maxSpeed / 2;
		}
		if (this.speed > 0) {
			this.speed -= this.friction;
		}
		if (this.speed < 0) {
			this.speed += this.friction;
		}
		if (Math.abs(this.speed) < this.friction) {
			this.speed = 0;
		}
		;
		if (this.angleSpeed > angleMaxSpeed) {
			this.angleSpeed = angleMaxSpeed;
		} else if (this.angleSpeed < -angleMaxSpeed) {
			this.angleSpeed = -angleMaxSpeed;
		}
		;
		if (this.angleSpeed > 0) {
			this.angleSpeed -= angleAcceleration / 3;
		} else if (this.angleSpeed < 0) {
			this.angleSpeed += angleAcceleration / 3;
		}

		if (Math.abs(this.angleSpeed) < angleAcceleration / 2) {
			this.angleSpeed = 0;
		}
		;

		this.x += (int) Math.round(this.speed * Math.cos(this.angle));
		this.y += (int) Math.round(this.speed * Math.sin(this.angle));

		;
		createPolygon();
		updateCachedPolygons();
	}

	private void updateCachedPolygons() {
		this.xDotsCachedPolygonCoords = this.xDotsPolygonCoords.clone();
		this.yDotsCachedPolygonCoords = this.yDotsPolygonCoords.clone();

	}

	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(this.currentColor);
		if (this.sensor != null && (this.bestCar || this.hunamDrives)) {
			this.sensor.paint(g2d);
		}
		if(this.damaged){
			g2d.setColor(this.damagedColor);
		}
		g2d.fillPolygon(this.xDotsPolygonCoords, this.yDotsPolygonCoords, this.xDotsPolygonCoords.length);
		g2d.translate(this.x, this.y);
		g2d.rotate(this.angle + Math.PI / 2);
		g2d.translate(-this.x, -this.y);
		if (this.bestCar) {
		g2d.drawImage(this.bestCarImage, this.x - (this.bestCarImage.getWidth(null) /
		2),
		this.y - (this.bestCarImage.getHeight(null) / 2), null);
		} else {
		g2d.drawImage(this.carImage, this.x - (this.carImage.getWidth(null) / 2),
		this.y - (this.carImage.getHeight(null) / 2), null);
		}

		g2d.translate(this.x, this.y);
		g2d.rotate(-this.angle - Math.PI / 2);
		g2d.translate(-this.x, -this.y);

	}
}