/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workbench.cm.client.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jbpm.workbench.cm.client.events.CaseCreatedEvent;
import org.jbpm.workbench.cm.client.newcase.NewCaseInstancePresenter;
import org.jbpm.workbench.cm.client.overview.CaseOverviewPresenter;
import org.jbpm.workbench.cm.client.perspectives.CaseOverviewPerspective;
import org.jbpm.workbench.cm.client.util.AbstractPresenter;
import org.jbpm.workbench.cm.model.CaseInstanceSummary;
import org.jbpm.workbench.cm.service.CaseManagementService;
import org.jbpm.workbench.cm.util.CaseInstanceSearchRequest;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.mvp.impl.DefaultPlaceRequest;

import static org.jbpm.workbench.cm.client.resources.i18n.Constants.CASES_LIST;
import static java.util.stream.Collectors.toList;

@Dependent
@WorkbenchScreen(identifier = CaseInstanceListPresenter.SCREEN_ID)
public class CaseInstanceListPresenter extends AbstractPresenter<CaseInstanceListPresenter.CaseInstanceListView> {

    public int currentPage = 0;
    public static final int PAGE_SIZE = 2;

    public static final String SCREEN_ID = "Case List";

    private Caller<CaseManagementService> caseService;

    List<CaseInstanceSummary> visibleCaseInstances = new ArrayList<CaseInstanceSummary>();
    HashSet<CaseInstanceSummary> hashSet = new HashSet<CaseInstanceSummary>();
    
    Logger logger = Logger.getLogger("CaseInstanceListPresenter");

    @Inject
    private PlaceManager placeManager;

    @Inject
    private TranslationService translationService;

    @Inject
    private NewCaseInstancePresenter newCaseInstancePresenter;

    public int getPageSize() {
        return PAGE_SIZE;
    }

    public void setCurrentPage(int i) {
        this.currentPage = i;
    }

    public int getCurrentPage() {
        return this.currentPage;
    }

    @WorkbenchPartTitle
    public String getTitle() {
        return translationService.format(CASES_LIST);
    }

    @PostConstruct
    @Override
    public void init() {
        super.init();
        refreshData();
    }

    public void createCaseInstance() {
        newCaseInstancePresenter.show();
    }
    
    private void caseInstancesServiceCall(int currentPage) {
        caseService.call((List<CaseInstanceSummary> cases) -> {          
            visibleCaseInstances.addAll(cases);   
            hashSet.addAll(cases);
            logger.log(Level.SEVERE, "hash set size" + hashSet.size());
            Set<CaseInstanceSummary> uniqueCaseInstances = new LinkedHashSet<>(visibleCaseInstances);
            logger.log(Level.SEVERE, "uniqueCaseInstances" + uniqueCaseInstances.size());
            visibleCaseInstances.clear();
            visibleCaseInstances.addAll(uniqueCaseInstances);           
            view.setCaseInstanceList(visibleCaseInstances.stream().collect(toList())); 
            logger.log(Level.SEVERE, "visible size inside service call" + visibleCaseInstances.size());
        }).getCaseInstances(view.getCaseInstanceSearchRequest(),
                            currentPage,
                            PAGE_SIZE);
        
        
        caseService.call((List<CaseInstanceSummary> cases) -> {
            
            logger.log(Level.SEVERE, "cases for next page size" + cases.size());
            
            if (cases.isEmpty()) {
                view.hideLoadButton();
            }
            else {
                view.showLoadButton();
            }
           
        }).getCaseInstances(view.getCaseInstanceSearchRequest(),
                            getCurrentPage() + 1,
                            PAGE_SIZE);
        
        
    }

    protected void refreshData() {
        caseInstancesServiceCall(getCurrentPage());
    }

    protected void selectCaseInstance(final CaseInstanceSummary cis) {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(CaseOverviewPresenter.PARAMETER_SERVER_TEMPLATE_ID,
                       "");
        parameters.put(CaseOverviewPresenter.PARAMETER_CONTAINER_ID,
                       cis.getContainerId());
        parameters.put(CaseOverviewPresenter.PARAMETER_CASE_ID,
                       cis.getCaseId());
        final DefaultPlaceRequest overview = new DefaultPlaceRequest(CaseOverviewPerspective.PERSPECTIVE_ID,
                                                                     parameters);
        placeManager.goTo(overview);
    }

    protected void cancelCaseInstance(final CaseInstanceSummary caseInstanceSummary) {
        caseService.call(
                e -> refreshData()
        ).cancelCaseInstance(null,
                             caseInstanceSummary.getContainerId(),
                             caseInstanceSummary.getCaseId());
    }

    protected void destroyCaseInstance(final CaseInstanceSummary caseInstanceSummary) {
        caseService.call(
                e -> refreshData()
        ).destroyCaseInstance(null,
                              caseInstanceSummary.getContainerId(),
                              caseInstanceSummary.getCaseId());
    }

    public void onCaseCreatedEvent(@Observes CaseCreatedEvent event) {
        refreshData();
    }

    protected void searchCaseInstances() {
        refreshData();
    }

    @Inject
    public void setCaseService(final Caller<CaseManagementService> caseService) {
        this.caseService = caseService;
    }

    public interface CaseInstanceListView extends UberElement<CaseInstanceListPresenter> {

        void setCaseInstanceList(List<CaseInstanceSummary> caseInstanceList);

        CaseInstanceSearchRequest getCaseInstanceSearchRequest();

        void hideLoadButton();

        void showLoadButton();
    }

    public void loadMoreCaseInstances() {
        this.currentPage = currentPage + 1;
        caseInstancesServiceCall(currentPage);
        logger.log(Level.SEVERE, "visibleCasesSize" + visibleCaseInstances.size());
        int maxPageIndex = (visibleCaseInstances.size() + getPageSize() - 1) / getPageSize();
//        if (maxPageIndex == getCurrentPage()) {
//            view.hideLoadButton();
//        }
        logger.log(Level.SEVERE, "maxPageIndex" + maxPageIndex);
        logger.log(Level.SEVERE, "currentPage" + getCurrentPage());
        //displayLoadMoreToggle();
    }
    
//    private void displayLoadMoreToggle() {
//        int maxPageIndex = (visibleCaseInstances.size() + getPageSize() - 1) / getPageSize() - 1;
//        logger.log(Level.SEVERE, "maxPageIndex" + maxPageIndex);
//        logger.log(Level.SEVERE, "getCurrentPage" + getCurrentPage());
//        logger.log(Level.SEVERE, "visibleCasesSize" + visibleCaseInstances.size());
//        logger.log(Level.SEVERE, "visibleCasesSize" + visibleCaseInstances.stream().collect(toList()).size());
//
//    }
}