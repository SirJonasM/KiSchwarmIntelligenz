import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Team {
    static int idCount = 0;
    private final Color color;
    int id;
    ArrayList<Vehicle> teamMembers;
    int currentInfecter;
    int currentUninfecter;
    Random random = new Random();
    private double[] randomVector = new double[2];

    public Team(ArrayList<Vehicle> teamMembers, Color color){
        this.color = color;
        id = idCount;
        idCount++;
        this.teamMembers = teamMembers;
    }
    public void setUp(){
        currentInfecter = teamMembers.get(0).id;
        currentUninfecter = teamMembers.get(1).id;
    }
    public ArrayList<Vehicle> getTeamMembers(){
        return teamMembers;
    }

    public void addTeamMember(Vehicle vehicle) {
        vehicle.updateVelocity();
        teamMembers.add(vehicle);
    }
    public Color getColor(){
        return color;
    }
    public void setRandomVector(){
        randomVector[0] = (Math.random()-0.5)/100.0;
        randomVector[1] = (Math.random()-0.5)/100.0;
    }
    public double[] getRandomVector(){
        return randomVector;
    }
}
