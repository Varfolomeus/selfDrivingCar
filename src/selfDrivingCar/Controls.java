package selfDrivingCar;

public class Controls {

	public boolean forward;
	public boolean left;
	public boolean right;
	public boolean reverse;
	private int userKeyEvent;
	private String controlType;

	public Controls(String type) {
		this.controlType = type;
		this.userKeyEvent = 0;
		this.forward = false;
		this.left = false;
		this.right = false;
		this.reverse = false;
	};

	public void addKeyboardListeners(int keyevent) {
		switch (controlType) {
			case "KEYS":
				carReflection(keyevent);
				// System.out.println(keyevent);
				break;
			// case "AI":
			// carReflection (gameclass);
			// break;
			case "DUMMY":
				forward = true;
				break;
		}

	}

	public void carReflection(int keyevent) {
		userKeyEvent = keyevent;
		switch (userKeyEvent) {
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
