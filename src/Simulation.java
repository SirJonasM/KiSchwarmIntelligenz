import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.*;

public class Simulation extends JFrame {
	public static final double standartTime = 0.01;
	public static final double maximalTime = 0.2;
	public static final double minimalTime = 0.0;

	Random random  = new Random();
	static double time = 0.01;
	int i = 2;
	static double day = 0;
	static int daySwitch = 0;
	static int infected = 0;

	static int sleep = 8; // 8
	static double pix = 0.2;// 0.2
	int anzTeamMembers = 3;
	int anzRed = 20;
	ArrayList<Vehicle> allVehicles = new ArrayList<Vehicle>();
	ArrayList<Vehicle> blueVehicles = new ArrayList<Vehicle>();
	ArrayList<Vehicle> redVehicles = new ArrayList<Vehicle>();
	Map<Integer, Team> teams = new HashMap<>();
	JPanel canvas;

	Simulation() {
		setTitle("Swarm");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		for (int k = 0; k < anzTeamMembers; k++) {
			Vehicle car1 = new Vehicle(0);
			Vehicle car2 = new Vehicle(1);
			blueVehicles.add(car1);
			redVehicles.add(car2);

			allVehicles.add(car1);
			allVehicles.add(car2);
		}
		teams.put(0,new Team(blueVehicles));
		teams.put(1,new Team(redVehicles));

		canvas = new Canvas(allVehicles,pix,teams);
		add(canvas);
		setSize(1000, 800);
		setVisible(true);

	}

	public static void main(String args[]) {
		Simulation xx = new Simulation();
		xx.run();
	}

	public void run() {
		Vehicle v;
		while (true) {
			for (int i = 0; i < allVehicles.size(); i++) {
				v = allVehicles.get(i);
				v.steuern(allVehicles);
				if(v.team == 1)infected++;
			}
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException ignored) {
			}repaint();
			day += time;
		}
	}
	private void virus(ArrayList<Vehicle> allVehicles) {
		for(Vehicle vehicle: allVehicles){
			vehicle.virus(allVehicles);
		}
	}
}
