import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class Vehicle {
	final static double velDefined = 0.1;
	final static double accDefined = 0.1;

	private final Random random = new Random();
	boolean wasInfected = false;

	static int allId = 0;
	int id; // Fahrzeug-ID
	double radiusSeperateFromEnemies; // Radius f�r Zusammenbleiben
	double radiusSeperateFromAllies;

	double radiusToGroup; // Radius f�r Separieren
	int team; // Fahrzeug-Type (0: Verfolger; 1: Anf�hrer)
	double[] pos; // Position
	double[] vel; // Geschwindigkeit
	double max_acc; // Maximale Beschleunigung meter/Simulation.time^2
	double max_vel; // Maximale Geschwindigkeit meter/Simulation.time soll immer 1 meter proSimulationszeit entsprechen
	final double rad_Virus;
	boolean hadContact = false;
	private boolean dead;


	Vehicle(int team) {
		allId++;
		this.id = allId;

		this.radiusSeperateFromEnemies = 30;
		this.radiusSeperateFromAllies = 20;
		this.radiusToGroup = 50;// 25

		this.max_acc = 0.15;// 0.1
		this.max_vel = 0.1;
		this.rad_Virus = 25;
		this.team = team;

		pos = new double[2];
		vel = new double[2];
		pos[0] = Simulation.pix * 500 * Math.random();
		pos[1] = Simulation.pix * 500 * Math.random();
		vel[0] = max_vel * Math.random();
		vel[1] = max_vel * Math.random();
	}
	// Nachbarn ermitteln:
	// Methoden um entweder alle, eigene Teammitglieder oder gegnerische Personen
	// in der Nähe zu ermitteln.
	ArrayList<Vehicle> nachbarErmitteln(ArrayList<Vehicle> all, double radius1, double radius2) {
		ArrayList<Vehicle> neighbours = new ArrayList<>();
		for (Vehicle v : all) {
			if (v.id != this.id) {
				double dist = Math.sqrt(Math.pow(v.pos[0] - this.pos[0], 2) + Math.pow(v.pos[1] - this.pos[1], 2));
				if (dist >= radius1 && dist < radius2) {
					neighbours.add(v);
				}
			}
		}
		return neighbours;
	}

	private ArrayList<Vehicle> getTeamMembersInRadius(ArrayList<Vehicle> vehicles, double radius1, double radius2) {
		return nachbarErmitteln(vehicles,radius1,radius2);
	}
	private ArrayList<Vehicle> getEnemiesInRadius(ArrayList<Vehicle> allVehicles, double radius2, int t){
		ArrayList<Vehicle> neighbours = nachbarErmitteln(allVehicles,0,radius2);
		ArrayList<Vehicle> specificReversedNeighbours = new ArrayList<>();
		for (Vehicle neighbour : neighbours) {
			if (neighbour.team != t) specificReversedNeighbours.add(neighbour);
		}
		return specificReversedNeighbours;
	}

	//Nachbarn ermitteln änderen

	//Steuern Anfang: Methoden um das bewegen des Vehicles zu berechnen
	double[] calculateAcceleration(double[] vel_dest) {
		//Berechnet die notwendige Beschleunigung, um eine Zielgeschwindigkeit vel_dest zu erreichen
		double[] acc_dest = new double[2];

		// 1. Konstanter Geschwindigkeitsbetrag
		vel_dest = Vektorrechnung.normalize(vel_dest);
		vel_dest[0] = vel_dest[0] * max_vel;
		vel_dest[1] = vel_dest[1] * max_vel;

		// 2. acc_dest berechnen
		acc_dest[0] = vel_dest[0] - vel[0];
		acc_dest[1] = vel_dest[1] - vel[1];

		return acc_dest;
	}

	double[] zusammenbleiben(Map<Integer,Team> teams) {

		ArrayList<Vehicle> neighbours;
		double[] pos_dest = new double[2];
		double[] vel_dest = new double[2];
		double[] acc_dest = new double[2];

		acc_dest[0] = 0;
		acc_dest[1] = 0;
		neighbours = getTeamMembersInRadius(teams.get(team).getTeamMembers(), radiusSeperateFromAllies, radiusToGroup);
		if (neighbours.isEmpty()) return acc_dest;

		// 1. Zielposition pos_dest berechnen
		pos_dest[0] = 0;
		pos_dest[1] = 0;
		for (Vehicle v : neighbours) {
			pos_dest[0] = pos_dest[0] + v.pos[0];
			pos_dest[1] = pos_dest[1] + v.pos[1];
		}
		pos_dest[0] = pos_dest[0] / neighbours.size();
		pos_dest[1] = pos_dest[1] / neighbours.size();

		// 2. Zielgeschwindigkeit vel_dest berechnen
		vel_dest[0] = pos_dest[0] - pos[0];
		vel_dest[1] = pos_dest[1] - pos[1];

		// 3. Zielbeschleunigung acc_dest berechnen
		acc_dest = calculateAcceleration(vel_dest);
		return acc_dest;
	}
	double[] separieren(ArrayList<Vehicle> neighbours,double rad){
		double[] vel_dest = new double[2];
		double[] acc_dest = new double[2];

		acc_dest[0] = 0;
		acc_dest[1] = 0;
		if(neighbours.isEmpty()) return acc_dest;
		// 1. Zielgeschwindigkeit vel_dest berechnen
		vel_dest[0] = 0;
		vel_dest[1] = 0;
		for (Vehicle v : neighbours) {
			double[] vel = new double[2];
			double dist;

			vel[0] = v.pos[0] - pos[0];
			vel[1] = v.pos[1] - pos[1];

			dist = rad - Vektorrechnung.length(vel);
			if (dist < 0) System.out.println("fehler in rad");
			vel = Vektorrechnung.normalize(vel);
			vel[0] = -vel[0] * dist;
			vel[1] = -vel[1] * dist;

			vel_dest[0] = vel_dest[0] + vel[0];
			vel_dest[1] = vel_dest[1] + vel[1];
		}

		// 2. Zielbeschleunigung acc_dest berechnen
		acc_dest = calculateAcceleration(vel_dest);

		return acc_dest;

	}
	double[] separierenVonEnemies(ArrayList<Vehicle> allVehicles) {
		ArrayList<Vehicle> neighbours  = getEnemiesInRadius(allVehicles,  radiusSeperateFromEnemies,team);
		return separieren(neighbours, radiusSeperateFromEnemies);
	}
	private double[] separierenVonTeam(Map<Integer,Team> teams) {
		ArrayList<Vehicle> neighbours  = getTeamMembersInRadius(teams.get(team).teamMembers, 0, radiusSeperateFromAllies);
		return separieren(neighbours, radiusSeperateFromAllies);
	}


	double[] ausrichten(Map<Integer,Team> teams) {
		ArrayList<Vehicle> neighbours;
		double[] vel_dest = new double[2];
		double[] acc_dest = new double[2];
		acc_dest[0] = 0;
		acc_dest[1] = 0;

		neighbours = getTeamMembersInRadius(teams.get(team).teamMembers, 0, radiusToGroup);
		if(neighbours.isEmpty()) return acc_dest;
		for (Vehicle neighbour : neighbours) {
				double[] direction = neighbour.vel;
				vel_dest[0] += direction[0];
				vel_dest[1] += direction[1];
			}

		vel_dest[0] = vel_dest[0] / neighbours.size();
		vel_dest[1] = vel_dest[1] / neighbours.size();
		acc_dest = calculateAcceleration(vel_dest);

		return acc_dest;
	}

	double[] zufall() {
		double[] acc_dest = new double[2];
		acc_dest[0] = max_acc * (Math.random()-0.5)/100.0;
		acc_dest[1] = max_acc * (Math.random()-0.5)/100.0;
		return acc_dest;
	}

	public double[] beschleunigung_festlegen(ArrayList<Vehicle> allVehicles,Map<Integer,Team> teams) throws Exception {
		//Setze die Faktoren wie die einzelnen Vektoren gewichtet werden sollen muss insgesamt 100 ergeben
		int groupFactor = 30; // 0.05
		int separateFromEnemiesFactor = 20;
		int separateFromAlliesFactor = 5;
		int adjustFactor = 5; // 0.4
		int randomFactor = 38;
		int randomTeamVectorFactor = 2;

		if(groupFactor+separateFromAlliesFactor+separateFromEnemiesFactor+adjustFactor+randomFactor+randomTeamVectorFactor !=100)
			throw new Exception();


		//berechnet die einzelnen Vektoren
		double[] group = zusammenbleiben(teams);
		double[] separateFromEnemies = separierenVonEnemies(allVehicles);
		double[] separateFromAllies = separierenVonTeam(teams);
		double[] adjust = ausrichten(teams);
		double[] random = zufall();

		double[] randomFromTeam = teams.get(team).getRandomVector();

//Legenden Kommentar: '*+##+§$%

		//gewichtet diese Vektoren
		group[0] = group[0] * groupFactor;
		group[1] = group[1] * groupFactor;

		separateFromEnemies[0] = separateFromEnemies[0] * separateFromEnemiesFactor;
		separateFromEnemies[1] = separateFromEnemies[1] * separateFromEnemiesFactor;

		separateFromAllies[0] = separateFromAllies[0] * separateFromAlliesFactor;
		separateFromAllies[0] = separateFromAllies[0] * separateFromAlliesFactor;

		adjust[0] = adjust[0] * adjustFactor;
		adjust[1] = adjust[0] * adjustFactor;

		random[0] = random[0]*randomFactor;
		random[1] = random[1]*randomFactor;

		randomFromTeam[0] *= max_acc * randomTeamVectorFactor;
		randomFromTeam[1] *= max_acc * randomTeamVectorFactor;



		//Berechne den gewichteten Beschleunigunsvektor
		double[] acceleration = new double[]{
				(group[0] + separateFromAllies[0] + separateFromEnemies[0] + adjust[0] + random[0]+randomFromTeam[0])/100.0,
				(group[1] + separateFromAllies[1] + separateFromEnemies[1] + adjust[1] + random[1]+randomFromTeam[1])/100.0};
		acceleration = Vektorrechnung.truncate(acceleration, max_acc);
		return acceleration;
	}
	public void updateVelocity(){
		double fraction = Simulation.time / Simulation.standartTime;
		max_acc = accDefined * Math.sqrt(fraction);
		max_vel = velDefined * fraction;
	}

	void steuern(ArrayList<Vehicle> allVehicles,Map<Integer,Team> teams) throws Exception {
		double[] acc_dest = beschleunigung_festlegen(allVehicles,teams);
	
		// 2. Neue Geschwindigkeit berechnen
		vel[0] = vel[0] + acc_dest[0];
		vel[1] = vel[1] + acc_dest[1];
		vel    = Vektorrechnung.normalize(vel);
		vel[0] = vel[0] * max_vel;
		vel[1] = vel[1] * max_vel;


		// 3. Neue Position berechnen
		pos[0] = pos[0] + vel[0];
		pos[1] = pos[1] + vel[1];

		position_Umgebung_anpassen_Box();
	}

	public void position_Umgebung_anpassen_Box() {
		if (pos[0] < 10) {
			vel[0] = Math.abs(vel[0]);
			pos[0] = pos[0] + vel[0];
		}
		if (pos[0] > 1000 * Simulation.pix) {
			vel[0] = -Math.abs(vel[0]);
			pos[0] = pos[0] + vel[0];
		}
		if (pos[1] < 10) {
			vel[1] = Math.abs(vel[1]);
			pos[1] = pos[1] + vel[1];
		}
		if (pos[1] > 700 * Simulation.pix) {
			vel[1] = -Math.abs(vel[1]);
			pos[1] = pos[1] + vel[1];
		}
	}
	//Steuern ende


	public void virus(ArrayList<Vehicle> allVehicles) {
		ArrayList<Vehicle> neighbours = nachbarErmitteln(allVehicles,0,rad_Virus);
		double probability = 0;
		for(Vehicle vehicle : neighbours){
			if(vehicle.team ==1)probability+=0.001;
		}
		if(probability>0.7) probability = 0.7;

		if(wasInfected){
			probability /= 10;
		}
		if(random.nextDouble() < probability){
			infect();
		}
		wasInfected = team == 1;
	}
	public void infect() {
		max_vel = 0;
		wasInfected = true;
		dead = true;
	}


}
