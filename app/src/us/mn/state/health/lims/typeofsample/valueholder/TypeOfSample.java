/**
* The contents of this file are subject to the Mozilla Public License
* Version 1.1 (the "License"); you may not use this file except in
* compliance with the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/ 
* 
* Software distributed under the License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
* License for the specific language governing rights and limitations under
* the License.
* 
* The Original Code is OpenELIS code.
* 
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
* 
* Contributor(s): CIRG, University of Washington, Seattle WA.
*/
package us.mn.state.health.lims.typeofsample.valueholder;

import org.codehaus.jackson.annotate.JsonIgnore;

import us.mn.state.health.lims.common.valueholder.BaseObject;

public class TypeOfSample extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String id;

	private String description;

	private String domain;
	
	private String localAbbreviation;

	private boolean isActive;
	
	private int sortOrder;
	
	public String getLocalAbbreviation() {
		return localAbbreviation;
	}

	public void setLocalAbbreviation(String localAbbreviation) {
		this.localAbbreviation = localAbbreviation;
	}

	public TypeOfSample() {
		super();
	}
	public TypeOfSample(TypeOfSample typeOfSample) {
		this.id=typeOfSample.getId();
		this.description=typeOfSample.getDescription();
		this.domain=typeOfSample.getDomain();
		this.localAbbreviation=typeOfSample.getLocalAbbreviation();
		this.isActive=typeOfSample.getIsActive();
		this.sortOrder=typeOfSample.getSortOrder();
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getDomain() {
		return domain;
	}

/*	@Override
	protected String getDefaultLocalizedName() {
		return description;
	}*/

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

    @JsonIgnore
	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

    @JsonIgnore
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	
}