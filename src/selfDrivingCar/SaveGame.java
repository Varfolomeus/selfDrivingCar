package selfDrivingCar;

import java.lang.reflect.Field;

import javax.swing.JOptionPane;

public class SaveGame {
	public int RAYS_COUNT;
	public double RAYS_SPREAD_ANGLE_timser;
	public String NNLayersInput;
	public double FLOP;
	public double mutationStep;
	public int CarsNumber;
	public boolean mutations;
	public double botMaxSpeed;
	public double dummyMaxSpeed;
	public boolean reloadRoadIfRestart;
	public double humanBotMaxSpeed;
	public boolean userWantsToPlay;
	public boolean chainedmutations;
	public boolean useSavedBrain;
	public int sleeptime;
	public int WINDOW_HEIGHT;
	public int WINDOW_WIDTH;
	public NNetwork savedBrain;

	public SaveGame(gameSelfDrivingCar gameclass) {
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			try {
				Field gamefield = gameclass.getClass().getDeclaredField(field.getName());
				field.setAccessible(true);
				gamefield.setAccessible(true);

				if (field.getName().equalsIgnoreCase("savedBrain") && field.get(this) instanceof NNetwork) {
					NNetwork clonedSavedBrain = (NNetwork) ((NNetwork) gamefield.get(gameclass)).clone();
					field.set(this, clonedSavedBrain);
				} else {
					field.set(this, gamefield.get(gameclass));
				}
			} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException
					| CloneNotSupportedException e) {
								JOptionPane.showMessageDialog(gameclass, "Problems whth saving game");

				// e.printStackTrace();
			}
		}
	}
}
