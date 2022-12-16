import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.swing.*;
import java.awt.Toolkit;

public class Simulation extends JFrame {
	public static final double standartTime = 0.01;
	public static final double maximalTime = 0.10;
	public static final double minimalTime = 0.0;
	public static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	public static int hight = Toolkit.getDefaultToolkit().getScreenSize().height;
	Random random  = new Random();
	static double time = 0.0;
	static double day = 0;
	static int daySwitch = 0;
	static int sleep = 8; // 8
	static double pix = 0.2;// 0.2
	int anzTeamMembers =40;
	static int teamCount = 20;
	Color[] colors = {Color.BLUE,Color.RED,Color.GREEN,Color.YELLOW,Color.PINK,Color.CYAN,Color.ORANGE,Color.WHITE,Color.LIGHT_GRAY,Color.MAGENTA,Color.DARK_GRAY};
	ArrayList<Vehicle> allVehicles = new ArrayList<>();
	final int MAXTEAMS = colors.length;
	Map<Integer, Team> teams = new HashMap<>();
	JPanel canvas;

	Simulation() {
		setTitle("Swarm");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(null);
		if(teamCount >MAXTEAMS) teamCount = MAXTEAMS;
		for(int i = 0; i<teamCount;i++){
			Team team = new Team(new ArrayList<>(),colors[i]);
			teams.put(team.id,team);
		}
		for (int k = 0; k < anzTeamMembers; k++) {
			for (int i = 0; i < teamCount; i++) {
				Vehicle car = new Vehicle(i);
				car.updateVelocity();
				teams.get(i).addTeamMember(car);
				allVehicles.add(car);
			}
		}
		for(Team team : teams.values()){
			team.setUp();
		}
		canvas = new Canvas(allVehicles,pix,teams);
		add(canvas);
		setSize(width, hight);
		canvas.setSize(width,hight);
		setVisible(true);
	}

	public static void main(String[] args) throws Exception {
		Simulation xx = new Simulation();
		xx.run();
	}

	public void run() throws Exception {
		Vehicle v;
		while (true) {
			teams.values().forEach(Team::setRandomVector);
			for (int i = 0; i < allVehicles.size(); i++) {
				v = allVehicles.get(i);
				v.steuern(allVehicles,teams);
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
