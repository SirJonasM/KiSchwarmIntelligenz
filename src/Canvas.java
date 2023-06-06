import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.*;


public class Canvas extends JPanel {
	double savedTime = 0.05;
	int size = 5;
	ArrayList<Vehicle> 	allVehicles;
	Map<Integer,Team> teams;
	double pix;	
	
	Canvas(ArrayList<Vehicle> allVehicles, double pix, Map<Integer,Team> teams){
		this.allVehicles = allVehicles;
		this.pix         = pix;
		this.teams 		 = teams;
		this.setBackground(Color.BLACK);
		setSize(Simulation.width,Simulation.hight);
		JButton faster = new JButton("Faster");
		JButton slower = new JButton("Slower");
		JButton stopAndGO = new JButton("Play/Pause");
		JButton addSomeone = new JButton("Add Someone");
		JSlider slider = new JSlider();
		slider.setValue(100 - Simulation.sleep*10);

		slower.addActionListener(event -> slowerActionListener());
		faster.addActionListener(event -> fasterActionListener());
		stopAndGO.addActionListener(event ->stopAndGoActionListener());
		addSomeone.addActionListener(event -> addSomeoneActionListener());
		slider.addChangeListener(event -> sliderChangeListener(slider));

		this.add(slower);
		this.add(faster);
		this.add(stopAndGO);
		this.add(addSomeone);
		this.add(slider);
	}

	private void sliderChangeListener(JSlider slider) {
		int sleep = slider.getValue()/10;
		Simulation.sleep = 10-sleep;
	}


	public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	Graphics2D g2d = (Graphics2D) g;
		g2d.drawOval(Simulation.mouseTracker.getLocation().x,Simulation.mouseTracker.getLocation().y,20,20);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawString("Speed x" + ((Simulation.sleep)),820,20);
		g2d.drawString("Velocity: " + Math.round(Simulation.time*100),10,20);
		for(Team team : teams.values()){
			paintTeam(g2d,team);

		}
		g2d.setColor(Color.BLACK);
		g2d.drawString("Time: " + (int) Simulation.day,10,30);
    }

	private void paintTeam(Graphics2D g2d,Team team) {
		g2d.setColor(team.getColor());
		for(Vehicle vehicle : team.teamMembers){
			if(vehicle.id == team.currentInfecter){
				g2d.fillOval((int)(vehicle.pos[0]/pix),(int)(vehicle.pos[1]/pix),size*2,(int)(size*1.5));
				continue;
			}
			if(vehicle.id == team.currentUninfecter){
				g2d.drawOval((int)(vehicle.pos[0]/pix),(int)(vehicle.pos[1]/pix),size*2,(int)(size*1.5));
				continue;
			}
			g2d.fillOval((int)(vehicle.pos[0]/pix),(int)(vehicle.pos[1]/pix),size,size);
		}
	}
//TODO: Todo
	private void addSomeoneActionListener() {
		if(Simulation.time != 0) stopAndGoActionListener();
		String teamNameInput = JOptionPane.showInputDialog("Welches Team?").trim();
		int team = Integer.parseInt(teamNameInput);
		if(team >= Simulation.teamCount || team < 0) return;
		String numberOfVehiclesInput = JOptionPane.showInputDialog("Wie viele?").trim();
		int numberOfVehicles = Integer.parseInt(numberOfVehiclesInput);

		for(int i = 0;i< numberOfVehicles;i++){
			Vehicle vehicle = new Vehicle(team);
			vehicle.updateVelocity();
			teams.get(team).addTeamMember(vehicle);
			allVehicles.add(vehicle);
		}
		stopAndGoActionListener();

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
		savedTime = currentTime;
		if(currentTime > Simulation.maximalTime) return;
		Simulation.time  = currentTime;
		allVehicles.forEach(Vehicle::updateVelocity);
	}

	private void slowerActionListener() {
		double currentTime = Simulation.time;
		currentTime -= 0.01;
		currentTime = Math.round(currentTime*100)/100.0;
		savedTime = currentTime;
		if(currentTime < Simulation.minimalTime) currentTime = 0;
		Simulation.time = currentTime;
		allVehicles.forEach(Vehicle::updateVelocity);
	}

	private int round(double time) {
		return (int)Math.round(time*100);
	}

}
