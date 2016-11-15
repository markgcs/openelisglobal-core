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
package us.mn.state.health.lims.resultvalidation.action;

import static org.apache.commons.validator.GenericValidator.isBlankOrNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.GenericValidator;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;

import us.mn.state.health.lims.analysis.dao.AnalysisDAO;
import us.mn.state.health.lims.analysis.daoimpl.AnalysisDAOImpl;
import us.mn.state.health.lims.analysis.valueholder.Analysis;
import us.mn.state.health.lims.analysisexchange.dao.AnalysisExchangeDAO;
import us.mn.state.health.lims.analysisexchange.daoimpl.AnalysisExchangeDAOImpl;
import us.mn.state.health.lims.analysisexchange.valueholder.AnalysisExchange;
import us.mn.state.health.lims.common.action.BaseActionForm;
import us.mn.state.health.lims.common.exception.LIMSRuntimeException;
import us.mn.state.health.lims.common.log.LogEvent;
import us.mn.state.health.lims.common.services.AnalysisService;
import us.mn.state.health.lims.common.services.IResultSaveService;
import us.mn.state.health.lims.common.services.NoteService;
import us.mn.state.health.lims.common.services.NoteService.NoteType;
import us.mn.state.health.lims.common.services.ResultSaveService;
import us.mn.state.health.lims.common.services.StatusService;
import us.mn.state.health.lims.common.services.StatusService.AnalysisStatus;
import us.mn.state.health.lims.common.services.StatusService.OrderStatus;
import us.mn.state.health.lims.common.services.beanAdapters.ResultSaveBeanAdapter;
import us.mn.state.health.lims.common.services.registration.ValidationUpdateRegister;
import us.mn.state.health.lims.common.services.registration.interfaces.IResultUpdate;
import us.mn.state.health.lims.common.services.serviceBeans.ResultSaveBean;
import us.mn.state.health.lims.common.util.ConfigurationProperties;
import us.mn.state.health.lims.common.util.ConfigurationProperties.Property;
import us.mn.state.health.lims.common.util.SqlConnectUtil;
import us.mn.state.health.lims.common.util.StringUtil;
import us.mn.state.health.lims.common.util.validator.ActionError;
import us.mn.state.health.lims.hibernate.HibernateUtil;
import us.mn.state.health.lims.note.dao.NoteDAO;
import us.mn.state.health.lims.note.daoimpl.NoteDAOImpl;
import us.mn.state.health.lims.note.valueholder.Note;
import us.mn.state.health.lims.patient.valueholder.Patient;
import us.mn.state.health.lims.referencetables.daoimpl.ReferenceTablesDAOImpl;
import us.mn.state.health.lims.reports.dao.DocumentTrackDAO;
import us.mn.state.health.lims.reports.daoimpl.DocumentTrackDAOImpl;
import us.mn.state.health.lims.reports.daoimpl.DocumentTypeDAOImpl;
import us.mn.state.health.lims.reports.valueholder.DocumentTrack;
import us.mn.state.health.lims.result.action.util.ResultSet;
import us.mn.state.health.lims.result.dao.ResultDAO;
import us.mn.state.health.lims.result.dao.ResultSignatureDAO;
import us.mn.state.health.lims.result.daoimpl.ResultDAOImpl;
import us.mn.state.health.lims.result.daoimpl.ResultSignatureDAOImpl;
import us.mn.state.health.lims.result.valueholder.Result;
import us.mn.state.health.lims.result.valueholder.ResultSignature;
import us.mn.state.health.lims.resultvalidation.action.util.ResultValidationPaging;
import us.mn.state.health.lims.resultvalidation.bean.AnalysisItem;
import us.mn.state.health.lims.sample.dao.SampleDAO;
import us.mn.state.health.lims.sample.daoimpl.SampleDAOImpl;
import us.mn.state.health.lims.sample.valueholder.Sample;
import us.mn.state.health.lims.samplehuman.dao.SampleHumanDAO;
import us.mn.state.health.lims.samplehuman.daoimpl.SampleHumanDAOImpl;
import us.mn.state.health.lims.systemuser.dao.SystemUserDAO;
import us.mn.state.health.lims.systemuser.daoimpl.SystemUserDAOImpl;
import us.mn.state.health.lims.systemuser.valueholder.SystemUser;
import us.mn.state.health.lims.testresult.dao.TestResultDAO;
import us.mn.state.health.lims.testresult.daoimpl.TestResultDAOImpl;
import us.mn.state.health.lims.testresult.valueholder.TestResult;
import us.mn.state.health.lims.typeoftestresult.valueholder.TypeOfTestResult.ResultType;

public class ResultValidationTransferAction extends BaseResultValidationAction implements IResultSaveService {

    // DAOs
    private static final AnalysisDAO analysisDAO = new AnalysisDAOImpl();

    private static final SampleDAO sampleDAO = new SampleDAOImpl();

    private static final TestResultDAO testResultDAO = new TestResultDAOImpl();

    private static final ResultDAO resultDAO = new ResultDAOImpl();

    private static final NoteDAO noteDAO = new NoteDAOImpl();

    private static final SampleHumanDAO sampleHumanDAO = new SampleHumanDAOImpl();

    private static final DocumentTrackDAO documentTrackDAO = new DocumentTrackDAOImpl();
    
    private static final ResultSignatureDAO resultSignature = new ResultSignatureDAOImpl();
    
    private static final AnalysisExchangeDAO analysisExchangeDAO = new AnalysisExchangeDAOImpl();

    // Update Lists
    private List<Analysis> analysisUpdateList;

    private ArrayList<Sample> sampleUpdateList;

    private ArrayList<Note> noteUpdateList;

    private ArrayList<Result> resultUpdateList;
    //Added by Dung 2016.07.13
    //List remove if choose blank combobox
    private Map<String,Result> resultDeleteList;
    private List<Result> deletableList;

    private SystemUser systemUser;

    private ArrayList<Integer> sampleFinishedStatus = new ArrayList<Integer>();

    private List<ResultSet> modifiedResultSet;

    private List<ResultSet> newResultSet;

    private static final String RESULT_SUBJECT = "Result Note";

    private static final String RESULT_TABLE_ID;

    private static final String RESULT_REPORT_ID;

    static {
        RESULT_TABLE_ID = new ReferenceTablesDAOImpl().getReferenceTableByName("RESULT").getId();
        RESULT_REPORT_ID = new DocumentTypeDAOImpl().getDocumentTypeByName("resultExport").getId();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String forward = FWD_SUCCESS;

        List<IResultUpdate> updaters = ValidationUpdateRegister.getRegisteredUpdaters();
        boolean areListeners = updaters != null && !updaters.isEmpty();

        request.getSession().setAttribute(SAVE_DISABLED, "true");

        BaseActionForm dynaForm = (BaseActionForm) form;

        ResultValidationPaging paging = new ResultValidationPaging();
        paging.updatePagedResults(request, dynaForm);
        List<AnalysisItem> resultItemList = paging.getResults(request);
        String testSectionName = (String) dynaForm.get("testSection");
        String testName = (String) dynaForm.get("testName");
        // Dung add
        String accessNumber = (String) dynaForm.get("accessionNumber");
        setRequestType(testSectionName);

        //ActionMessages errors = validateModifiedItems(resultItemList);

        //Trung add
        //Fix bug 335, if have error message when save, it will return search by accessionNumber type
        
        // Comment this function because validateQuantifiableItems is unnecessary 
        /*if (errors.size() > 0) {
            saveErrors(request, errors);
            request.setAttribute(Globals.ERROR_KEY, errors);
            if(accessNumber!=null){
            	return mapping.findForward(FWD_VALIDATION_ERROR_ACCESSION_NUMBER);
            }
            else
            return mapping.findForward(FWD_VALIDATION_ERROR);
        }*/

        createSystemUser();
        setSampleFinishedStatuses();

        noteUpdateList = new ArrayList<Note>();
        resultUpdateList = new ArrayList<Result>();
        analysisUpdateList = new ArrayList<Analysis>();
        modifiedResultSet = new ArrayList<ResultSet>();
        newResultSet = new ArrayList<ResultSet>();
        deletableList = new ArrayList<Result>();
        resultDeleteList=new HashMap();

        if (testSectionName.equals("serology")) {
            createUpdateElisaList(resultItemList);
        } else {
            createUpdateList(resultItemList, areListeners);
        }
        //Trung add
        //In validation screen, if click search button without input the accessionNumber
        //fix bug 335 update August 1 2016
        /*if(accessNumber==""){
        	return mapping.findForward(FWD_VALIDATION_ERROR_ACCESSION_NUMBER);
        }*/
        Transaction tx = HibernateUtil.getSession().beginTransaction();

        try {
            ResultSaveService.removeDeletedResultsInTransaction(deletableList, currentUserId);

            
            for (Analysis analysis : analysisUpdateList) {
                
                //get analysis exchange by internal_analysis_id
                AnalysisExchange analysisExchange = analysisExchangeDAO.getAnalysisExchangeByInternalAnalysisId(analysis.getId());
                if (analysisExchange != null) {
                	analysisExchange.setExchangedBy(currentUserId);
                    //transfer data to other SQL server (MS SQL)
                    SqlConnectUtil.transferTestResult(analysis, analysisExchange, accessNumber);
                  //update analysis exchange
                    analysisExchangeDAO.updateData(analysisExchange, analysis, currentUserId);
                 // update analysis
                   analysisDAO.updateAnalysisStatus(analysis);
                    
                    request.setAttribute(FWD_SUCCESS_NUMBER, true);
                }
            }

            for (Result result : resultUpdateList) {
                if (result.getId() != null) {
                    resultDAO.updateData(result);
                } else {
                    resultDAO.insertData(result);
                }
                request.setAttribute(FWD_SUCCESS_NUMBER, true);
            }
            for (Entry<String,Result> map : resultDeleteList.entrySet()) {
                if (map.getValue().getId() != null) {
                    
                    List<ResultSignature> liResultSignature=resultSignature.getResultSignaturesByResult(map.getValue());
                    
                    resultSignature.deleteData(liResultSignature);
                    resultDAO.deleteAllData(map.getValue());
                } else {
                    resultDAO.insertData(map.getValue());
                }
                request.setAttribute(FWD_SUCCESS_NUMBER, true);
            }

            checkIfSamplesFinished(resultItemList);

            // update finished samples
            for (Sample sample : sampleUpdateList) {
                sampleDAO.updateData(sample);
                request.setAttribute(FWD_SUCCESS_NUMBER, true);
            }

            // create or update notes
            for (Note note : noteUpdateList) {
                if (note != null) {
                    if (note.getId() == null) {
                        noteDAO.insertData(note);
                    } else {
                        noteDAO.updateData(note);
                    }
                    request.setAttribute(FWD_SUCCESS_NUMBER, true);
                }
            }

            for (IResultUpdate updater : updaters) {
                updater.transactionalUpdate(this);
                request.setAttribute(FWD_SUCCESS_NUMBER, true);
            }

            tx.commit();

        } catch (LIMSRuntimeException lre) {
            tx.rollback();
        }

        for (IResultUpdate updater : updaters) {
            updater.postTransactionalCommitUpdate(this);
        }

        // route save back to RetroC specific ResultValidationRetroCAction
        if (ConfigurationProperties.getInstance().isPropertyValueEqual(Property.configurationName, "CI RetroCI"))
            forward = "successRetroC";
        if (!GenericValidator.isBlankOrNull(accessNumber)) {
            forward = FWD_SUCCESS_NUMBER;
        }
        if (isBlankOrNull(testSectionName)) {
            return mapping.findForward(forward);
        } else {
            Map<String, String> params = new HashMap<String, String>();
            params.put("type", testSectionName);
            params.put("test", testName);
            params.put("forward", forward);

            return getForwardWithParameters(mapping.findForward(forward), params);
        }

    }

    private ActionMessages validateModifiedItems(List<AnalysisItem> resultItemList) {
        ActionErrors errors = new ActionErrors();
        try {
            for (AnalysisItem item : resultItemList) {
                List<ActionError> errorList = new ArrayList<ActionError>();
                validateQuantifiableItems(item, errorList);

                if (errorList.size() > 0) {
                    StringBuilder augmentedAccession = new StringBuilder(item.getAccessionNumber());
                    augmentedAccession.append(" : ");
                    augmentedAccession.append(item.getTestName());
                    ActionError accessionError = new ActionError("errors.followingAccession", augmentedAccession);
                    errors.add(ActionErrors.GLOBAL_MESSAGE, accessionError);

                    for (ActionError error : errorList) {
                        errors.add(ActionErrors.GLOBAL_MESSAGE, error);
                    }
                }
            }
        } catch (Exception ex) {
            LogEvent.logError("ResultValidationTransferAction", "validateModifiedItems()", ex.getMessage());
        }

        return errors;
    }

    public void validateQuantifiableItems(AnalysisItem analysisItem, List<ActionError> errors) {
        if (analysisItem.isHasQualifiedResult() && isBlankOrNull(analysisItem.getQualifiedResultValue())
                && analysisItemWillBeUpdated(analysisItem)) {
            errors.add(new ActionError("errors.missing.result.details", new StringBuilder("Result")));
        }
        // verify that qualifiedResultValue has been entered if required
        if (!isBlankOrNull(analysisItem.getQualifiedDictionaryId())) {
            String[] qualifiedDictionaryIds = analysisItem.getQualifiedDictionaryId().replace("[", "").replace("]", "")
                    .split(",");
            Set<String> qualifiedDictIdsSet = new HashSet<String>(Arrays.asList(qualifiedDictionaryIds));

            if (qualifiedDictIdsSet.contains(analysisItem.getResult())
                    && isBlankOrNull(analysisItem.getQualifiedResultValue())) {
                errors.add(new ActionError("errors.missing.result.details", new StringBuilder("Result")));

            }
        }
    }

    private void createUpdateList(List<AnalysisItem> analysisItems, boolean areListeners) {

        List<String> analysisIdList = new ArrayList<String>();
        try {
            for (AnalysisItem analysisItem : analysisItems) {
                if (!analysisItem.isReadOnly() && analysisItemWillBeUpdated(analysisItem)) {

                    AnalysisService analysisService = new AnalysisService(analysisItem.getAnalysisId());
                    Analysis analysis = analysisService.getAnalysis();
                    NoteService noteService = new NoteService(analysis);

                    analysis.setSysUserId(currentUserId);

                    if (!analysisIdList.contains(analysis.getId())) {

                        if (analysisItem.getIsAccepted()) {
                            // Added by Dung 2016.07.13
                            if(!analysisItem.getResult().equals("0"))
                            analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized));
                            analysis.setReleasedDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
                            analysisIdList.add(analysis.getId());
                            analysisUpdateList.add(analysis);
                        }

                        if (analysisItem.getIsRejected()) {
                            analysis.setStatusId(StatusService.getInstance().getStatusID(
                                    AnalysisStatus.BiologistRejected));
                            analysisIdList.add(analysis.getId());
                            analysisUpdateList.add(analysis);
                        }
                    }

                    createNeededNotes(analysisItem, noteService);

//                    if (areResults(analysisItem)) {
                        //Check if choose combobox is blank then delete
                        if(!analysisItem.getResult().equals("0")){//maybe result is ""
                            List<Result> results = createResultFromAnalysisItem(analysisItem, analysisService, noteService);
                            for (Result result : results) {
                                resultUpdateList.add(result);
    
                                if (areListeners) {
                                    addResultSets(analysis, result);
                                }
                            }
                        }else{
                            List<Result> results = createResultFromAnalysisItem(analysisItem, analysisService, noteService);
                            for (Result result : results) {
                                resultDeleteList.put(result.getId(),result);
    
                                if (areListeners) {
                                    addResultSets(analysis, result);
                                }
                            }
                        }
//                    }
                }
            }
        } catch (Exception ex) {
            LogEvent.logError("ResultValidationTransferAction", "createUpdateList()", ex.getMessage());
        }
    }

    private void createNeededNotes(AnalysisItem analysisItem, NoteService noteService) {
        if (analysisItem.getIsRejected()) {
            Note note = noteService.createSavableNote(NoteType.INTERNAL,
                    StringUtil.getMessageForKey("validation.note.retest"), RESULT_SUBJECT, currentUserId);
            noteUpdateList.add(note);
        }

        if (!GenericValidator.isBlankOrNull(analysisItem.getNote())) {
            NoteType noteType = analysisItem.getIsAccepted() ? NoteType.EXTERNAL : NoteType.INTERNAL;
            Note note = noteService.createSavableNote(noteType, analysisItem.getNote(), RESULT_SUBJECT, currentUserId);
            noteUpdateList.add(note);
        }
    }

    private void addResultSets(Analysis analysis, Result result) {
        Sample sample = analysis.getSampleItem().getSample();
        Patient patient = sampleHumanDAO.getPatientForSample(sample);
        List<DocumentTrack> documents = documentTrackDAO.getByTypeRecordAndTable(RESULT_REPORT_ID, RESULT_TABLE_ID,
                result.getId());
        if (documents.isEmpty()) {
            newResultSet.add(new ResultSet(result, null, null, patient, sample, null, false));
        } else {
            modifiedResultSet.add(new ResultSet(result, null, null, patient, sample, null, false));
        }
    }

    private boolean analysisItemWillBeUpdated(AnalysisItem analysisItem) {
        return analysisItem.getIsAccepted() || analysisItem.getIsRejected();
    }

    private void createUpdateElisaList(List<AnalysisItem> resultItems) {

        for (AnalysisItem resultItem : resultItems) {

            if (resultItem.getIsAccepted()) {

                List<Analysis> acceptedAnalysisList = createAnalysisFromElisaAnalysisItem(resultItem);

                for (Analysis analysis : acceptedAnalysisList) {
                    analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized));
                    analysisUpdateList.add(analysis);
                }
            }

            if (resultItem.getIsRejected()) {
                List<Analysis> rejectedAnalysisList = createAnalysisFromElisaAnalysisItem(resultItem);

                for (Analysis analysis : rejectedAnalysisList) {
                    analysis.setStatusId(StatusService.getInstance().getStatusID(AnalysisStatus.BiologistRejected));
                    analysisUpdateList.add(analysis);
                }

            }
        }
    }

    private List<Analysis> createAnalysisFromElisaAnalysisItem(AnalysisItem analysisItem) {

        List<Analysis> analysisList = new ArrayList<Analysis>();

        Analysis analysis = new Analysis();

        if (!isBlankOrNull(analysisItem.getMurexResult())) {
            analysis = getAnalysisFromId(analysisItem.getMurexAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getBiolineResult())) {
            analysis = getAnalysisFromId(analysisItem.getBiolineAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getIntegralResult())) {
            analysis = getAnalysisFromId(analysisItem.getIntegralAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getVironostikaResult())) {
            analysis = getAnalysisFromId(analysisItem.getVironostikaAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getGenieIIResult())) {
            analysis = getAnalysisFromId(analysisItem.getGenieIIAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getGenieII10Result())) {
            analysis = getAnalysisFromId(analysisItem.getGenieII10AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getGenieII100Result())) {
            analysis = getAnalysisFromId(analysisItem.getGenieII100AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getWesternBlot1Result())) {
            analysis = getAnalysisFromId(analysisItem.getWesternBlot1AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getWesternBlot2Result())) {
            analysis = getAnalysisFromId(analysisItem.getWesternBlot2AnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getP24AgResult())) {
            analysis = getAnalysisFromId(analysisItem.getP24AgAnalysisId());
            analysisList.add(analysis);
        }
        if (!isBlankOrNull(analysisItem.getInnoliaResult())) {
            analysis = getAnalysisFromId(analysisItem.getInnoliaAnalysisId());
            analysisList.add(analysis);
        }

        analysisList.add(analysis);

        return analysisList;
    }

    private void checkIfSamplesFinished(List<AnalysisItem> resultItemList) {
        sampleUpdateList = new ArrayList<Sample>();

        String currentSampleId = "";
        boolean sampleFinished = true;
        try {
            for (AnalysisItem analysisItem : resultItemList) {

                String analysisSampleId = sampleDAO.getSampleByAccessionNumber(analysisItem.getAccessionNumber())
                        .getId();
                if (!analysisSampleId.equals(currentSampleId)) {

                    currentSampleId = analysisSampleId;

                    List<Analysis> analysisList = analysisDAO.getAnalysesBySampleId(currentSampleId);
                    // if status is 15("Test was requested but then canceled") the doesn't update
                    for (Analysis analysis : analysisList) {
                        if (!sampleFinishedStatus.contains(Integer.parseInt(analysis.getStatusId()))) {
                            sampleFinished = false;
                            break;
                        }
                    }

                    if (sampleFinished) {
                        Sample sample = new Sample();
                        sample.setId(currentSampleId);
                        sampleDAO.getData(sample);
                        sample.setStatusId(StatusService.getInstance().getStatusID(OrderStatus.Finished));
                        sampleUpdateList.add(sample);
                    }

                    sampleFinished = true;

                }

            }
        } catch (Exception ex) {
            LogEvent.logError("ResultValidationTransferAction", "checkIfSamplesFinished()", ex.getMessage());
        }
    }

    private Analysis getAnalysisFromId(String id) {
        Analysis analysis = new Analysis();
        analysis.setId(id);
        analysisDAO.getData(analysis);
        analysis.setSysUserId(currentUserId);

        return analysis;
    }

    private List<Result> createResultFromAnalysisItem(AnalysisItem analysisItem, AnalysisService analysisService,
            NoteService noteService) {

        ResultSaveBean bean = ResultSaveBeanAdapter.fromAnalysisItem(analysisItem);
        ResultSaveService resultSaveService = new ResultSaveService(analysisService.getAnalysis(), currentUserId);
        List<Result> results = resultSaveService.createResultsFromTestResultItem(bean, deletableList);
        if (analysisService.patientReportHasBeenDone() && resultSaveService.isUpdatedResult() && !analysisItem.getResult().equals("0")) {
            analysisService.getAnalysis().setCorrectedSincePatientReport(true);
            noteUpdateList.add(noteService.createSavableNote(NoteType.EXTERNAL,
                    StringUtil.getMessageForKey("note.corrected.result"), RESULT_SUBJECT, currentUserId));
        }
        return results;
    }

    protected TestResult getTestResult(AnalysisItem analysisItem) {
        TestResult testResult = null;
        if (ResultType.DICTIONARY.matches(analysisItem.getResultType())) {
            testResult = testResultDAO.getTestResultsByTestAndDictonaryResult(analysisItem.getTestId(),
                    analysisItem.getResult());
        } else {
            List<TestResult> testResultList = testResultDAO.getActiveTestResultsByTest(analysisItem.getTestId());
            // we are assuming there is only one testResult for a numeric type
            // result
            if (!testResultList.isEmpty()) {
                testResult = testResultList.get(0);
            }
        }
        return testResult;
    }

    private boolean areResults(AnalysisItem item) {
        return !(isBlankOrNull(item.getResult()) || (ResultType.DICTIONARY.matches(item.getResultType()) && "0"
                .equals(item.getResult())))
                || (ResultType.isMultiSelectVariant(item.getResultType()) && !isBlankOrNull(item
                        .getMultiSelectResultValues()));
    }

    private void createSystemUser() {
        systemUser = new SystemUser();
        systemUser.setId(currentUserId);
        SystemUserDAO systemUserDAO = new SystemUserDAOImpl();
        systemUserDAO.getData(systemUser);
    }

    private void setSampleFinishedStatuses() {
        sampleFinishedStatus = new ArrayList<Integer>();
        sampleFinishedStatus.add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Finalized)));
        sampleFinishedStatus.add(Integer.parseInt(StatusService.getInstance().getStatusID(AnalysisStatus.Canceled)));
        sampleFinishedStatus.add(Integer.parseInt(StatusService.getInstance().getStatusID(
                AnalysisStatus.NonConforming_depricated)));
    }

    @Override
    public String getCurrentUserId() {
        return currentUserId;
    }

    @Override
    public List<ResultSet> getNewResults() {
        return newResultSet;
    }

    @Override
    public List<ResultSet> getModifiedResults() {
        return modifiedResultSet;
    }

}
