import java.util.ArrayList;
import java.util.Objects;
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
	int anzFz = 300;
	int anzRed = 20;
	ArrayList<Vehicle> allVehicles = new ArrayList<Vehicle>();
	JPanel canvas;

	Simulation() {
		setTitle("Swarm");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);

		for (int k = 0; k < anzFz; k++) {
			Vehicle car = new Vehicle();
			allVehicles.add(car);
		}
		canvas = new Canvas(allVehicles,pix);
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
				if(v.type == 1)infected++;
			}
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException ignored) {
			}
			if(day>daySwitch+1){
				virus(allVehicles);
				daySwitch++;
			}repaint();
			day += time;
			infected = 0;
		}
	}
	private void virus(ArrayList<Vehicle> allVehicles) {
//		if(random.nextDouble()<1/day){
//			Vehicle vehicle =allVehicles.get(random.nextInt(allVehicles.size()-1));
//			vehicle.infect();
//		}
		for(Vehicle vehicle: allVehicles){
			vehicle.virus(allVehicles);
		}
	}
}
