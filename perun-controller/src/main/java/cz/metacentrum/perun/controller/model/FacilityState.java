package cz.metacentrum.perun.controller.model;

import cz.metacentrum.perun.core.api.Facility;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class used to interpret facility propagation state in GUI
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */

public class FacilityState {
	
	public static enum FacilityPropagationState {
		OK, ERROR, PROCESSING, NOT_DETERMINED
	};

	private Facility facility;
	private FacilityPropagationState state;
    private Map<String, FacilityPropagationState> results = new HashMap<String, FacilityPropagationState>();
	
	public Facility getFacility() {
		return facility;
	}
	public void setFacility(Facility facility) {
		this.facility = facility;
	}
	public FacilityPropagationState getState() {
		return state;
	}
	public void setState(FacilityPropagationState state) {
		this.state = state;
	}

    public Map<String, FacilityPropagationState> getResults() {
        return results;
    }

    public void setResults(Map<String, FacilityPropagationState> results) {
        this.results = results;
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((facility == null) ? 0 : facility.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FacilityState other = (FacilityState) obj;
		if (facility == null) {
			if (other.facility != null)
				return false;
		} else if (!facility.equals(other.facility))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "FacilityState [facility=" + facility + ", state=" + state + "]";
	}
	
}
