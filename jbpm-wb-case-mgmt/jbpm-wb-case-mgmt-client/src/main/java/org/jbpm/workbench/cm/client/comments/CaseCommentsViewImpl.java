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

package org.jbpm.workbench.cm.client.comments;


import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.Button;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Event;

import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.Input;
import org.jboss.errai.common.client.dom.MouseEvent;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import org.jbpm.workbench.cm.client.util.AbstractView;
import org.jbpm.workbench.cm.client.util.FormGroup;
import org.jbpm.workbench.cm.client.util.ValidationState;
import org.jbpm.workbench.cm.model.CaseCommentSummary;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.jboss.errai.common.client.dom.DOMUtil.*;
import static org.jbpm.workbench.cm.client.resources.i18n.Constants.*;


@Dependent
@Templated
public class CaseCommentsViewImpl extends AbstractView<CaseCommentsPresenter>
        implements CaseCommentsPresenter.CaseCommentsView {
    
    private int PAGE_SIZE = 4;
    
    @Inject
    @DataField("load-div")
    Div loadDiv;

    @Inject
    @DataField("comments")
    Div commentsContainer;

    @Inject
    @DataField("sort-alpha-asc")
    private Button sortAlphaAsc;

    @Inject
    @DataField("sort-alpha-desc")
    private Button sortAlphaDesc;
    
    @Inject
    @DataField("load-more-comments")
    @SuppressWarnings("PMD.UnusedPrivateField")
    private Button loadMoreComments;

    @Inject
    @Bound
    @DataField("comments-list")
    private ListComponent<CaseCommentSummary, CaseCommentItemView> comments;

    @Inject
    @AutoBound
    private DataBinder<List<CaseCommentSummary>> caseCommentList;

    @Inject
    @DataField("empty-list-item")
    private Div emptyContainer;

    @Inject
    @DataField("comment-creation-input")
    Input newCommentTextArea;

    @Inject
    @DataField("comment-creation-help")
    Span newCommentTextAreaHelp;

    @Inject
    @DataField("comment-creation-group")
    FormGroup newCommentTextAreaGroup;

    @Inject
    @DataField
    Anchor addCommentButton;

    @Inject
    private TranslationService translationService;

    List<CaseCommentSummary> allCommentsList;
    List<CaseCommentSummary> visibleComments;
    boolean canLoadMorePages = false;


    @PostConstruct
    public void init() {
        tooltip(sortAlphaAsc);
        sortAlphaAsc.setAttribute("data-original-title", translationService.format(SORT_BY_DATE_DESC));
        tooltip(sortAlphaDesc);
        sortAlphaDesc.setAttribute("data-original-title", translationService.format(SORT_BY_DATE_ASC));
    }

    @Override
    public HTMLElement getElement() {
        return commentsContainer;
    }

    @Override
    public void init(final CaseCommentsPresenter presenter) {
        this.presenter = presenter;
        comments.addComponentCreationHandler(v -> v.init(presenter));
    }

    @Override
    public void clearCommentInputForm() {
        newCommentTextArea.setValue("");
        clearErrorMessages();
    }

    public void clearErrorMessages() {
        newCommentTextAreaHelp.setTextContent("");
        newCommentTextAreaGroup.clearValidationState();
    }

    @Override
    public void resetPagination() {
        presenter.setCurrentPage(1);
        onSortChange(sortAlphaAsc, sortAlphaDesc, false);
    }

    @Override
    public void setCaseCommentList(final List<CaseCommentSummary> caseCommentList) {
        allCommentsList = caseCommentList;
        setVisibleComments(false);
        
        if (caseCommentList.isEmpty()) {
            removeCSSClass(emptyContainer, "hidden");
        } else {
            addCSSClass(emptyContainer, "hidden");
        }
    }
    
    
    protected void setVisibleComments(boolean doLoadMore) {
        
        int numberOfComments = allCommentsList.size();
        int currentPage = presenter.getCurrentPage();
        
        int availablePageSpace = (numberOfComments + PAGE_SIZE - 1) / PAGE_SIZE;

        if (this.allCommentsList.size() <= PAGE_SIZE) {
           visibleComments = allCommentsList.subList(0, allCommentsList.size());
        }
        
        else {
            canLoadMorePages = true;           
            if (doLoadMore) {
                
                if (availablePageSpace > 1 && currentPage == availablePageSpace) {
                    visibleComments = allCommentsList.subList(0, numberOfComments);
                }
                else {
                    visibleComments = allCommentsList.subList(numberOfComments - (PAGE_SIZE * currentPage), numberOfComments);
                }                
            }
        }
        
        loadDiv.setHidden(!canLoadMorePages);

        this.caseCommentList.setModel(visibleComments);
    }

    @EventHandler("addCommentButton")
    @SuppressWarnings("unsued")
    public void addCommentButton(@ForEvent("click") final Event e) {
        submitCommentAddition();
    }

    private void submitCommentAddition(){
        if (validateForm()) {
            presenter.addCaseComment(newCommentTextArea.getValue());
        }
    }

    private boolean validateForm() {
        clearErrorMessages();

        final boolean newCommentEmpty = isNullOrEmpty(newCommentTextArea.getValue());
        if (newCommentEmpty) {
            newCommentTextArea.focus();
            newCommentTextAreaHelp.setTextContent(translationService.format(CASE_COMMENT_CANT_BE_EMPTY));
            newCommentTextAreaGroup.setValidationState(ValidationState.ERROR);
            return false;
        }
        return true;
    }

    @EventHandler("sort-alpha-asc")
    public void onSortAlphaAsc(final @ForEvent("click") MouseEvent event) {
        onSortChange(sortAlphaAsc, sortAlphaDesc, false);
    }

    @EventHandler("sort-alpha-desc")
    public void onSortAlphaDesc(final @ForEvent("click") MouseEvent event) {
        onSortChange(sortAlphaDesc, sortAlphaAsc, true);
    }

    private void onSortChange(final HTMLElement toHide, final HTMLElement toShow, final Boolean sortByAsc){
        addCSSClass(toHide, "hidden");
        removeCSSClass(toShow, "hidden");
        presenter.sortComments(sortByAsc);
    }
    
    @EventHandler("load-more-comments")
    public void loadMoreComments(final @ForEvent("click") MouseEvent event) {
        
        presenter.loadMoreComments();
        setVisibleComments(true);                

    }

}
    
 