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
package us.mn.state.health.lims.project.valueholder;

import us.mn.state.health.lims.common.util.DateUtil;
import us.mn.state.health.lims.common.util.SystemConfiguration;
import us.mn.state.health.lims.common.valueholder.BaseObject;
import us.mn.state.health.lims.common.valueholder.ValueHolder;
import us.mn.state.health.lims.common.valueholder.ValueHolderInterface;
import us.mn.state.health.lims.organization.valueholder.Organization;
import us.mn.state.health.lims.scriptlet.valueholder.Scriptlet;
import us.mn.state.health.lims.systemuser.valueholder.SystemUser;
import us.mn.state.health.lims.program.dao.ProgramDAO;
import us.mn.state.health.lims.program.daoimpl.ProgramDAOImpl;
import us.mn.state.health.lims.program.valueholder.Program;

import java.sql.Date;
import java.util.Set;

public class Project extends BaseObject {

	
	private static final long serialVersionUID = 1L;

	private String id;

	private String projectName;

	private String description;

	private String stickerReqFlag;

	private String rptResultsFlag;

	private String printOnMailerFlag;

	private Date startedDate = null;

	private String startedDateForDisplay = null;

	private Date completedDate = null;

	private String completedDateForDisplay = null;

	private String isActive;

	private String referenceTo;

    private String programCode;

    private String programName;

	private String sysUserId;

	private String opOpId;

	private ValueHolderInterface systemUser;

	private ValueHolderInterface scriptlet;

	private String scriptletName;
	//AIS - bugzilla 1851
	private String concatProjNameDesc;
	
    //bugzilla 2438
	private String localAbbreviation;
	
	/**
	 * All organization defined as associated with this project.
	 */
	private Set<Organization> organizations;
	
	public String getConcatProjNameDesc() {	
			if (null == this.description){
				return this.projectName;				
			}else{
				return this.projectName + "+" +this.description;			
			}			
		}
		
	public void setConcatProjNameDesc(String concatProjNameDesc) {
			this.concatProjNameDesc = concatProjNameDesc;
		}	

	public Project() {
		super();
		this.systemUser = new ValueHolder();
		this.scriptlet = new ValueHolder();
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setStickerReqFlag(String stickerReqFlag) {
		this.stickerReqFlag = stickerReqFlag;
	}

	public String getStickerReqFlag() {
		return stickerReqFlag;
	}

	public void setRptResultsFlag(String rptResultsFlag) {
		this.rptResultsFlag = rptResultsFlag;
	}

	public String getRptResultsFlag() {
		return rptResultsFlag;
	}

	public void setPrintOnMailerFlag(String printOnMailerFlag) {
		this.printOnMailerFlag = printOnMailerFlag;
	}

	public String getPrintOnMailerFlag() {
		return printOnMailerFlag;
	}

	public void setStartedDate(Date startedDate) {
		this.startedDate = startedDate;
		this.startedDateForDisplay = DateUtil.convertSqlDateToStringDate(startedDate);
	}

	public Date getStartedDate() {
		return startedDate;
	}

	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
		this.completedDateForDisplay = DateUtil.convertSqlDateToStringDate(	completedDate);
	}

	public Date getCompletedDate() {
		return completedDate;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setReferenceTo(String referenceTo) {
		this.referenceTo = referenceTo;
	}

	public String getReferenceTo() {
		return referenceTo;
	}

	public void setProgramCode(String programCode) {
		this.programCode = programCode;
	}

	public String getProgramCode() {
		return programCode;
	}

	/**
     * Get programName 
     *
     * @return programName
     */
    public String getProgramName() {
        String proName = "";
        ProgramDAO programDAO = new ProgramDAOImpl();
        Program program = programDAO.getProgramByProgramCode(getProgramCode()); 
        if (program != null) {
            proName = program.getProgramName();
        }
        return proName;
    }

    /**
     * Set programName 
     *
     * @param programName the programName to set
     */
    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setSysUserId(String sysUserId) {
		this.sysUserId = sysUserId;
	}

	public String getSysUserId() {
		return sysUserId;
	}

	public SystemUser getSystemUser() {
		return (SystemUser) this.systemUser.getValue();
	}

	protected ValueHolderInterface getSystemUserHolder() {
		return this.systemUser;
	}

	public void setSystemUser(SystemUser systemUser) {
		this.systemUser.setValue(systemUser);
	}

	protected void setSystemUserHolder(ValueHolderInterface systemUser) {
		this.systemUser = systemUser;
	}

	public void setOpOpId(String opOpId) {
		this.opOpId = opOpId;
	}

	public String getOpOpId() {
		return opOpId;
	}

	public void setStartedDateForDisplay(String startedDateForDisplay) {
		this.startedDateForDisplay = startedDateForDisplay;
		// also update the java.sql.Date
		String locale = SystemConfiguration.getInstance().getDefaultLocale()
				.toString();
		this.startedDate = DateUtil.convertStringDateToSqlDate(
				this.startedDateForDisplay, locale);
	}

	public String getStartedDateForDisplay() {
		return startedDateForDisplay;
	}

	public void setCompletedDateForDisplay(String completedDateForDisplay) {
		this.completedDateForDisplay = completedDateForDisplay;
		// also update the java.sql.Date
		String locale = SystemConfiguration.getInstance().getDefaultLocale()
				.toString();
		this.completedDate = DateUtil.convertStringDateToSqlDate(
				completedDateForDisplay, locale);
	}

	public String getCompletedDateForDisplay() {
		return completedDateForDisplay;
	}

	public Scriptlet getScriptlet() {
		return (Scriptlet) this.scriptlet.getValue();
	}

	protected ValueHolderInterface getScriptletHolder() {
		return this.scriptlet;
	}

	public void setScriptlet(Scriptlet scriptlet) {
		this.scriptlet.setValue(scriptlet);
	}

	protected void setScriptletHolder(ValueHolderInterface scriptlet) {
		this.scriptlet = scriptlet;
	}

	public String getScriptletName() {
		return scriptletName;
	}

	public void setScriptletName(String scriptletName) {
		this.scriptletName = scriptletName;
	}

	public String getLocalAbbreviation() {
		return localAbbreviation;
	}

	public void setLocalAbbreviation(String localAbbreviation) {
		this.localAbbreviation = localAbbreviation;
	}

	@Override
	protected String getDefaultLocalizedName() {
		return projectName;
	}

    public void setOrganizations(Set<Organization> organizations) {
        this.organizations = organizations;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }
}