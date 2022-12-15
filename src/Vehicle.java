import java.util.ArrayList;
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

	double rad_zus; // Radius f�r Separieren
	int team; // Fahrzeug-Type (0: Verfolger; 1: Anf�hrer)
	final double FZL; // L�nge
	final double FZB; // Breite
	double[] pos; // Position
	double[] vel; // Geschwindigkeit
	double max_acc; // Maximale Beschleunigung meter/Simulation.time^2
	double max_vel; // Maximale Geschwindigkeit meter/Simulation.time soll immer 1 meter proSimulationszeit entsprechen
	final double rad_Virus;
	boolean hadContact = false;


	Vehicle(int team) {
		allId++;
		this.id = allId;
		this.FZL = 1;
		this.FZB = 1;
		this.radiusSeperateFromEnemies = 40;
		this.radiusSeperateFromAllies = 25;
		this.rad_zus = 100;// 25
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

	ArrayList<Vehicle> nachbarErmitteln(ArrayList<Vehicle> all, double radius1, double radius2) {
		ArrayList<Vehicle> neighbours = new ArrayList<>();
		for (int i = 0; i < all.size(); i++) {
			Vehicle v = all.get(i);
			if (v.id != this.id) {
				double dist = Math.sqrt(Math.pow(v.pos[0] - this.pos[0], 2) + Math.pow(v.pos[1] - this.pos[1], 2));
				if (dist >= radius1 && dist < radius2) {
					neighbours.add(v);
				}
			}
		}
		return neighbours;
	}

	double[] beschleunigungErmitteln(double[] vel_dest) {
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

//cohesion
	double[] zusammenbleiben(ArrayList<Vehicle> all) {

		ArrayList<Vehicle> neighbours;
		double[] pos_dest = new double[2];
		double[] vel_dest = new double[2];
		double[] acc_dest = new double[2];

		acc_dest[0] = 0;
		acc_dest[1] = 0;
		neighbours = getOnlySpecificType(all, radiusSeperateFromEnemies, rad_zus,0);
		if (neighbours.isEmpty()) return acc_dest;

		// 1. Zielposition pos_dest berechnen
		pos_dest[0] = 0;
		pos_dest[1] = 0;
		for (Vehicle v : neighbours) {
			if (v.team == 1) continue;
			pos_dest[0] = pos_dest[0] + v.pos[0];
			pos_dest[1] = pos_dest[1] + v.pos[1];
		}
		pos_dest[0] = pos_dest[0] / neighbours.size();
		pos_dest[1] = pos_dest[1] / neighbours.size();

		// 2. Zielgeschwindigkeit vel_dest berechnen
		vel_dest[0] = pos_dest[0] - pos[0];
		vel_dest[1] = pos_dest[1] - pos[1];

		// 3. Zielbeschleunigung acc_dest berechnen
		acc_dest = beschleunigungErmitteln(vel_dest);
		return acc_dest;
	}

	private ArrayList<Vehicle> getOnlySpecificType(ArrayList<Vehicle> allVehicles, double radius1, double radius2,int t) {
		ArrayList<Vehicle> neighbours = nachbarErmitteln(allVehicles,radius1,radius2);
		ArrayList<Vehicle> specificNeighbours = new ArrayList<>();
		for (Vehicle neighbour : neighbours) {
			if (neighbour.team == t) specificNeighbours.add(neighbour);
		}
		return specificNeighbours;
	}
	private ArrayList<Vehicle> getOnlySpecificTypeReversed(ArrayList<Vehicle> allVehicles, double radius1, double radius2,int t){
		ArrayList<Vehicle> neighbours = nachbarErmitteln(allVehicles,radius1,radius2);
		ArrayList<Vehicle> specificNeighbours = new ArrayList<>();
		for (Vehicle neighbour : neighbours) {
			if (neighbour.team != t) specificNeighbours.add(neighbour);
		}
		return specificNeighbours;
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
		acc_dest = beschleunigungErmitteln(vel_dest);

		return acc_dest;

	}
	double[] separierenVonEnemies(ArrayList<Vehicle> allVehicles) {
		ArrayList<Vehicle> neighbours  = getOnlySpecificTypeReversed(allVehicles, 0, radiusSeperateFromEnemies,0);
		return separieren(neighbours, radiusSeperateFromEnemies);
	}
	private double[] separierenVonTeam(ArrayList<Vehicle> allVehicles) {
		ArrayList<Vehicle> neighbours  = getOnlySpecificType(allVehicles, 0, radiusSeperateFromAllies,1);
		return separieren(neighbours, radiusSeperateFromAllies);
	}


	double[] ausrichten(ArrayList<Vehicle> all) {
		ArrayList<Vehicle> neighbours;
		double[] vel_dest = new double[2];
		double[] acc_dest = new double[2];
		acc_dest[0] = 0;
		acc_dest[1] = 0;

		neighbours = getOnlySpecificType(all, 0, rad_zus,team);
		if(neighbours.isEmpty()) return acc_dest;
		for (Vehicle neighbour : neighbours) {
				if (neighbour.team == 1) continue;
				double[] richtung = neighbour.vel;
				vel_dest[0] += richtung[0];
				vel_dest[1] += richtung[1];
			}

		vel_dest[0] = vel_dest[0] / neighbours.size();
		vel_dest[1] = vel_dest[1] / neighbours.size();
		acc_dest = beschleunigungErmitteln(vel_dest);

		return acc_dest;
	}

	double[] zufall() {
		double[] acc_dest = new double[2];
		acc_dest[0] = 0;
		acc_dest[1] = 0;

		if (Math.random() < 0.01) {
			acc_dest[0] = max_acc * Math.random();
			acc_dest[1] = max_acc * Math.random();
		}

		return acc_dest;
	}

	public double[] beschleunigung_festlegen(ArrayList<Vehicle> allVehicles) {
		//Setze die Faktoren wie die einzelnen Vektoren gewichtet werden sollen
		double groupFactor = 0.2; // 0.05
		double separateFromEnemiesFactor = 0.4;
		double separateFromAlliesFactor = 0.2;
		double adjustFactor = 0.2; // 0.4

		//berechnet die einzelnen Vektore n
		double[] group = zusammenbleiben(allVehicles);
		double[] separateFromEnemies = separierenVonEnemies(allVehicles);
		double[] separateFromAllies = separierenVonTeam(allVehicles);
		double[] adjust = ausrichten(allVehicles);

		//gewichtet diese Vektoren
		group[0] = group[0] * groupFactor;
		group[1] = group[1] * groupFactor;

		separateFromEnemies[0] = separateFromEnemies[0] * separateFromEnemiesFactor;
		separateFromEnemies[1] = separateFromEnemies[1] * separateFromEnemiesFactor;

		separateFromAllies[0] = separateFromAllies[0] * separateFromAlliesFactor;
		separateFromAllies[0] = separateFromAllies[0] * separateFromAlliesFactor;

		adjust[0] = adjust[0] * adjustFactor;
		adjust[1] = adjust[0] * adjustFactor;

		//Berechne den gewichteten Beschleunigunsvektor
		double[] acceleration = new double[]{
				group[0] + separateFromAllies[0] + separateFromEnemies[0] + adjust[0],
				group[1] + separateFromAllies[1] + separateFromEnemies[1] + adjust[1]};
		acceleration = Vektorrechnung.truncate(acceleration, max_acc);
		return acceleration;
	}

	public void virus(ArrayList<Vehicle> allVehicles) {
		ArrayList<Vehicle> neighbours = nachbarErmitteln(allVehicles,0,rad_Virus);
		double probability = 0;
		for(Vehicle vehicle : neighbours){
			if(vehicle.team ==1)probability+=0.001;;
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

	void steuern(ArrayList<Vehicle> allVehicles) {
		double[] acc_dest = beschleunigung_festlegen(allVehicles);
	
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
	

	
	double[] folgen(ArrayList<Vehicle> all) {
		double[] pos_dest = new double[2];
		double[] vel_dest = new double[2];
		double[] acc_dest = new double[2];
		acc_dest[0] = 0;
		acc_dest[1] = 0;
		Vehicle v = null;

		if (team == 0) {
			for (Vehicle vehicle : all) {
				v = vehicle;
				if (v.team == 1)
					break;
			}
			double dist = Math.sqrt(Math.pow(v.pos[0] - this.pos[0], 2) + Math.pow(v.pos[1] - this.pos[1], 2));

			if (dist < rad_zus && inFront(v)) {
				double[] pkt = new double[2];
				double[] ort1 = new double[2];
				double[] ort2 = new double[2];
				double[] ort3 = new double[2];
				pkt[0] = pos[0];
				pkt[1] = pos[1];
				ort1[0] = v.pos[0];
				ort1[1] = v.pos[1];
				ort2[0] = v.pos[0] + (rad_zus * v.vel[0]);
				ort2[1] = v.pos[1] + (rad_zus * v.vel[1]);
				ort3 = Vektorrechnung.punktVektorMINAbstand_punkt(pkt, ort1, ort2);

				vel_dest[0] = pos[0] - ort3[0];// UUU
				vel_dest[1] = pos[1] - ort3[1];// III

				vel_dest = Vektorrechnung.normalize(vel_dest);
				vel_dest[0] = vel_dest[0] * max_vel;
				vel_dest[1] = vel_dest[1] * max_vel;

				acc_dest[0] = vel_dest[0] - vel[0];
				acc_dest[1] = vel_dest[1] - vel[1];
			} else if (dist < rad_zus && !inFront(v)) {
				pos_dest[0] = v.pos[0] + v.vel[0];
				pos_dest[1] = v.pos[1] + v.vel[0];
				vel_dest[0] = pos_dest[0] - pos[0];
				vel_dest[1] = pos_dest[1] - pos[1];
				vel_dest = Vektorrechnung.normalize(vel_dest);
				vel_dest[0] = vel_dest[0] * max_vel;
				vel_dest[1] = vel_dest[1] * max_vel;
				acc_dest[0] = vel_dest[0] - vel[0];
				acc_dest[1] = vel_dest[1] - vel[1];
			} else {
				acc_dest = zusammenbleiben(all);
			}
		}

		return acc_dest;
	}

	boolean inFront(Vehicle v) {
		//
		boolean erg = false;
		double[] tmp = new double[2];
		tmp[0] = pos[0] - v.pos[0];
		tmp[1] = pos[1] - v.pos[1];

		erg = Vektorrechnung.winkel(tmp, v.vel) < Math.PI / 2;

		return erg;
	}


	public void infect() {
		max_vel = 0;
		wasInfected = true;
		team = 1;

	}
	public void setVelocity(){
		double fraction = Simulation.time / Simulation.standartTime;
		max_acc = accDefined * Math.sqrt(fraction);
		max_vel = velDefined * fraction;
	}
}
