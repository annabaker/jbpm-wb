/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workbench.pr.service;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jbpm.workbench.common.service.GenericServiceEntryPoint;
import org.jbpm.workbench.pr.model.DocumentKey;
import org.jbpm.workbench.pr.model.DocumentSummary;

@Remote
public interface ProcessDocumentsService extends GenericServiceEntryPoint<DocumentKey, DocumentSummary> {

    String getDocumentLink(String serverTemplateId,
                           String documentIdentifier);
}