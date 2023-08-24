package selfDrivingCar;

//import java.awt.Component;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;

public class Controls {

	public boolean forward;
	public boolean left;
	public boolean right;
	public boolean reverse;
	private int userKeyEvent;
	private String controlType;

	public Controls(String type, gameSelfDrivingCar gameclass) {
		this.controlType = type;
		this.userKeyEvent = 0;
		this.forward = false;
		this.left = false;
		this.right = false;
		this.reverse = false;

	};

	public void addKeyboardListeners(gameSelfDrivingCar gameclass) {
		switch (this.controlType) {
			case "KEYS":
				carReflection(gameclass);
				break;
			// case "AI":
			// carReflection (gameclass);
			// break;
			case "DUMMY":
				this.forward = true;
				break;
		}

	}

	public void carReflection(gameSelfDrivingCar gameclass) {
//		List<Component> componentsList = null;
		this.userKeyEvent = gameclass.userKeyEvent;
		switch (this.userKeyEvent) {
			// arrows - left, right, forward move , reverse move
			case 37:
				this.left = true;
				gameclass.userKeyEvent = 0;
				break;
			case 39:
				this.right = true;
				gameclass.userKeyEvent = 0;
				break;
			case 38:
				this.forward = true;
				gameclass.userKeyEvent = 0;
				break;
			case 40:
				this.reverse = true;
				gameclass.userKeyEvent = 0;
				break;
			case 44:
				// < - 44 - chainedmutations enablet or not +
				gameclass.chainedmutations = !gameclass.chainedmutations;
//				componentsList = Arrays.stream(gameclass.paramsGamePannel.getComponents())
//                        .collect(Collectors.toList());
				if (gameclass.comboBox.getItemAt(gameclass.comboBox.getSelectedIndex()).equalsIgnoreCase("chainedmutations")){
					gameclass.yesOption.setSelected(gameclass.chainedmutations);
					gameclass.noOption.setSelected(!gameclass.chainedmutations);
				};
				// System.out.println("chainedmutations switched to " +
				// gameclass.chainedmutations);
				gameclass.userKeyEvent = 0;
				break;
			case 76:
				// l - 76 - load best car brain +
				gameclass.getSavedGame();
				gameclass.userKeyEvent = 0;
				break;
			case 77:
				// m - 77 - mutations enablet or not +
				gameclass.mutations = !gameclass.mutations;
//				componentsList = Arrays.stream(gameclass.paramsGamePannel.getComponents())
//                        .collect(Collectors.toList());
				if (gameclass.comboBox.getItemAt(gameclass.comboBox.getSelectedIndex()).equalsIgnoreCase("mutations")){
					gameclass.yesOption.setSelected(gameclass.mutations);
					gameclass.noOption.setSelected(!gameclass.mutations);
				};
				// System.out.println("mutations switched to " + gameclass.mutations);
				gameclass.userKeyEvent = 0;
				break;
			case 78:
				// n - 78 - new game +
				gameclass.gamereloading = true;
				gameclass.userKeyEvent = 0;
				break;
			case 83:
				// s - 83 - save best car brain +
				gameclass.saveGame();
				gameclass.userKeyEvent = 0;
				break;
			case 85:
				// u - 85 - use saved brain in new car generation --
				gameclass.useSavedBrain = !gameclass.useSavedBrain;
//				componentsList = Arrays.stream(gameclass.paramsGamePannel.getComponents())
//                        .collect(Collectors.toList());
				if (gameclass.comboBox.getItemAt(gameclass.comboBox.getSelectedIndex()).equalsIgnoreCase("useSavedBrain")){
					gameclass.yesOption.setSelected(gameclass.useSavedBrain);
					gameclass.noOption.setSelected(!gameclass.useSavedBrain);
				};
				// System.out.println("use brain in cars generation switched to " +
				// gameclass.useSavedBrain);
				this.userKeyEvent = 0;
				break;
			case 0:
				this.left = false;
				this.right = false;
				this.forward = false;
				this.reverse = false;
				break;
			default:
				gameclass.userKeyEvent = 0;
				break;
		}
	}
}
