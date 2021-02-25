package uk.ac.york.sepr4.object.building;

import lombok.Data;
import uk.ac.york.sepr4.object.crew.CrewMember;

@Data
public class Department extends Building {
    private Float buildingRange = 2500f;
    private Integer crewMemberId, healCost;

    //populated after load
    private CrewMember crewMember;

    public Department() {
        // Empty constructor for JSON DAO
    }


}
