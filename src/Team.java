import java.util.ArrayList;

public class Team {
    static int idCount = 0;
    int id;
    ArrayList<Vehicle> teamMembers;
    int currentInfecter;
    int currentUninfecter;
    public Team(ArrayList<Vehicle> teamMembers){
        id = idCount;
        this.teamMembers = teamMembers;
        currentInfecter = 0;
        currentUninfecter = 1;
    }
    public ArrayList<Vehicle> getTeamMembers(){
        return teamMembers;
    }

    public void addTeamMember(Vehicle vehicle) {
        vehicle.updateVelocity();
        teamMembers.add(vehicle);
    }
}
