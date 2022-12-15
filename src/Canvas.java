import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.swing.*;


public class Canvas extends JPanel {
	double savedTime = 0.1;
	Random random = new Random();
	ArrayList<Vehicle> 	allVehicles;
	Map<Integer,Team> teams;
	double pix;	
	
	Canvas(ArrayList<Vehicle> allVehicles, double pix, Map<Integer,Team> teams){
		this.allVehicles = allVehicles;
		this.pix         = pix;
		this.teams 		 = teams;
		this.setBackground(Color.WHITE);
		setSize(1000,800);
		JButton faster = new JButton("Faster");
		JButton slower = new JButton("Slower");
		JButton stopAndGO = new JButton("Play/Pause");
		JButton addSomeoneBlue = new JButton("Add Someone to Blue");
		JButton addSomeoneRed = new JButton("Add Someone to Red");


		slower.addActionListener(event -> slowerActionListener());
		faster.addActionListener(event -> fasterActionListener());
		stopAndGO.addActionListener(event ->stopAndGoActionListener());
		addSomeoneBlue.addActionListener(event -> addSomeoneActionListener(0));
		addSomeoneRed.addActionListener(event -> addSomeoneActionListener(1));

		this.add(slower);
		this.add(faster);
		this.add(stopAndGO);
		this.add(addSomeoneBlue);
		this.add(addSomeoneRed);
	}


	public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2d = (Graphics2D) g;
		//g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawString("Speed x" + ((int)(Simulation.time*100)),700,20);

		for (Vehicle fz : allVehicles) {
			if (fz.team == 1) {
				g2d.setColor(Color.RED);
			}
			else if(fz.team == 0){
				g2d.setColor(Color.BLUE);
			}
			g2d.fillOval((int)(fz.pos[0]/pix),(int)(fz.pos[1]/pix),5,5);
		}
		g2d.setColor(Color.BLACK);
		g2d.drawString("Infected: " + Simulation.infected,900,720);
		g2d.drawString("Tag: " + (int) Simulation.day,900,700);
    }

	private void addSomeoneActionListener(int team) {
		Vehicle vehicle = new Vehicle(team);
		vehicle.updateVelocity();
		allVehicles.add(vehicle);
		teams.get(team).addTeamMember(vehicle);
	}

	private void stopAndGoActionListener() {
		if((round(Simulation.time) == 0 && round(savedTime) == 0)) Simulation.time = 0.01;
		else if(round(Simulation.time )==0) Simulation.time = savedTime;
		else Simulation.time = 0;
		allVehicles.forEach(Vehicle::updateVelocity);
	}

	private void fasterActionListener() {
		double currentTime = Simulation.time;
		currentTime += 0.01;
		currentTime = Math.round(currentTime*100)/100.0;
		if(currentTime > Simulation.maximalTime) return;
		Simulation.time  = currentTime;
		allVehicles.forEach(Vehicle::updateVelocity);
	}

	private void slowerActionListener() {
		double currentTime = Simulation.time;
		currentTime -= 0.01;
		currentTime = Math.round(currentTime*100)/100.0;
		if(currentTime < Simulation.minimalTime) currentTime = 0;
		Simulation.time = currentTime;
		allVehicles.forEach(Vehicle::updateVelocity);
	}

	private int round(double time) {
		return (int)Math.round(time*100);
	}

}
