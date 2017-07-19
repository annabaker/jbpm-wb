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

package org.jbpm.workbench.forms.client.display.views;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalSize;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jbpm.workbench.forms.client.display.GenericFormDisplayer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
// this is needed as a workaround (https://github.com/google/gwtmockito/issues/4)
@WithClassesToStub({Text.class, RootPanel.class})
public class PopupFormDisplayerViewTest {    

    @InjectMocks
    protected PopupFormDisplayerView view;
    
    @Mock
    private GenericFormDisplayer displayerMock;
    
    @Mock(name = "body")
    private FlowPanel body;
    @Mock(name = "footer")
    private ModalFooter footer;
    
    @Test
    public void initTest() {            
        
        PopupFormDisplayerView popupFormSpy = Mockito.spy(view);

        popupFormSpy.init();
        
        verify(popupFormSpy).setSize(ModalSize.LARGE);
        verify(popupFormSpy).setBody(body);
        verify(popupFormSpy).add(footer);
    }

    @Test
    public void displayTest() {
        view.display(displayerMock);
        
        InOrder inOrder = Mockito.inOrder(body, footer);
        
        inOrder.verify(body).clear();
        inOrder.verify(footer).clear();
        inOrder.verify(body).add(displayerMock.getContainer());
        inOrder.verify(footer).add(displayerMock.getFooter());
    }
    
    @Test
    public void closePopupTest() {       
        PopupFormDisplayerView popupFormSpy = Mockito.spy(view);
        
        popupFormSpy.closePopup();
        
        verify(popupFormSpy).hide();
        verify(popupFormSpy).setWidth("");        
    }
    
    
}
