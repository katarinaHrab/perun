package cz.metacentrum.perun.cabinet.model;

import java.util.Date;

/**
 * Extension for single Thanks to provide GUI more info
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class ThanksForGUI extends Thanks {

	private static final long serialVersionUID = 1L;

	private String ownerName;

	public ThanksForGUI() {
		super();
	}

	public ThanksForGUI(Integer id, Integer publicationId, Integer ownerId,
			String createdBy, Date createdDate) {
		super(id, publicationId, ownerId, createdBy, createdDate);
	}

	public ThanksForGUI(Thanks thanks) {
		setId(thanks.getId());
		setPublicationId(thanks.getPublicationId());
		setCreatedBy(thanks.getCreatedBy());
		setCreatedDate(thanks.getCreatedDate());
		setOwnerId(thanks.getOwnerId());
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String owner) {
		this.ownerName = owner;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName()+":[id="+ this.getId() + ", pubId="+ this.getPublicationId() +", ownerId="+ this.getOwnerId() +", ownerName="+ ownerName +", createdBy="+ this.getCreatedBy() +", createdDate="+ this.getCreatedDate() +"]";
	}

}