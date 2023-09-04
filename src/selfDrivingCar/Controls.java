package selfDrivingCar;

public class Controls {

	public boolean forward;
	public boolean left;
	public boolean right;
	public boolean reverse;
	private String controlType;

	public Controls(String type) {
		this.controlType = type;
		this.forward = false;
		this.left = false;
		this.right = false;
		this.reverse = false;
	};

	public void addKeyboardListeners(int carCenterX, int carCenterY, double carAngle, int eventX, int eventY) {
		switch (controlType) {
			case "KEYS":
				if (eventX != 0 && eventY != 0) {
					carReflectionOfMouse(carCenterX, carCenterY, carAngle, eventX, eventY);
				} else {
					carReflection(eventX);
				}
				break;
			case "DUMMY":
				forward = true;
				break;
		}

	}

	private void carReflectionOfMouse(int carCenterX, int carCenterY, double carAngle, int eventX, int eventY) {
		// double angletomouse
		if (eventX == 0 && eventY == 0) {
			left = false;
			right = false;
			forward = false;
			reverse = false;
			return;
		}

		double carAngleToUseInCompare = 0;
		double toMouseAngleToUseInCompare = 0;
		if (carAngle >= 0) {
			carAngleToUseInCompare = (carAngle % (2 * Math.PI)) - (2 * Math.PI);
		} else {
			carAngleToUseInCompare = (carAngle % (2 * Math.PI));
		}
		double uuu = (eventY - (double) gameSelfDrivingCar.CAR_CANVAS_HEIGHT * 0.7 + carCenterY) - carCenterY;

		double angleToMouse = Math.atan2(uuu, (double) (eventX - carCenterX));
		double rangeToMouse = Math.hypot(eventX - carCenterX, uuu);
		if (angleToMouse > 0) {
			toMouseAngleToUseInCompare = angleToMouse - Math.PI * 2;
		} else {
			toMouseAngleToUseInCompare = angleToMouse;
		}
		double deltaAngle = toMouseAngleToUseInCompare - carAngleToUseInCompare;
		if (deltaAngle < -Math.PI) {
			deltaAngle += 2 * Math.PI;
		}
		// System.out.println("ramgeToMouse " + rangeToMouse + " deltaAngle " + deltaAngle);
		if (rangeToMouse > 0 && Math.abs(deltaAngle) <= (Math.PI / 2)) {
			forward = true;
			reverse = false;
			if (deltaAngle > 0) {
				right = true;
				left = false;
			}
			if (deltaAngle < 0) {
				right = false;
				left = true;
			}
		} else if (rangeToMouse > 0 && Math.abs(deltaAngle) > (Math.PI / 2)) {
			forward = false;
			reverse = true;
			if (deltaAngle > 0) {
				right = false;
				left = true;
			}
			if (deltaAngle < 0) {
				right = true;
				left = false;
			}
		} else {
			left = false;
			right = false;
			forward = false;
			reverse = false;
		}
	}

	public void carReflection(int keyevent) {
		switch (keyevent) {
			// arrows - left, right, forward move , reverse move
			case 37:
				left = true;
				break;
			case 39:
				right = true;
				break;
			case 38:
				forward = true;
				break;
			case 40:
				reverse = true;
				break;
			case 0:
				left = false;
				right = false;
				forward = false;
				reverse = false;
				break;
		}
	}
}
