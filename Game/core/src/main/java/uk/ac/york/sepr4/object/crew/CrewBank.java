package uk.ac.york.sepr4.object.crew;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CrewBank {

    private List<CrewMember> crew = new ArrayList<>();

    /***
     * Initialize CrewMembers
     */
    public CrewBank() {
        crew.add(new DoubleShotCrew()); //2
        crew.add(new FireShotCrew()); //3
        crew.add(new BoostCrew()); //4
        crew.add(new PowerShotCrew()); //5
        crew.add(new TripleShotCrew()); //6
    }

    /***
     * Get list of keyboard keys that are used for activating CrewMembers.
     * @return list of keyboard keys
     */
    public List<String> getCrewKeys() {
        List<String> keys = new ArrayList<>();
        crew.forEach(crewMember -> keys.add(crewMember.getKey()));
        return keys;
    }

    /***
     * Get CrewMember from integer ID.
     * @param id crew member ID
     * @return optional crewmember if id exists.
     */
    public Optional<CrewMember> getCrewFromID(Integer id) {
        for(CrewMember crewMember : crew) {
            if(crewMember.getId().equals(id)) {
                return Optional.of(crewMember);
            }
        }
        return Optional.empty();
    }

    /***
     * Get CrewMember from keyboard key.
     * @param key representing keyboard key
     * @return optional crewmember if key exists.
     */
    public Optional<CrewMember> getCrewFromKey(String key) {
        for(CrewMember crewMember : crew) {
            if(crewMember.getKey().equalsIgnoreCase(key)) {
                return Optional.of(crewMember);
            }
        }
        return Optional.empty();
    }

}
