/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.sample.hello.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * HelloMaps application.
 */
public class Hello implements EntryPoint {
  

  public void onModuleLoad() {

    Button create = new Button("Create another map", new ClickListener() {
      public void onClick(Widget w) {
        createMap();
      }
    });
    RootPanel.get().add(create);
    
    createMap();
  }
  
  private void createMap() {
    final Map theMap = new Map();
    
    TextBox tb = new TextBox();
    tb.addKeyboardListener(new KeyboardListenerAdapter() {
      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        if (keyCode == KEY_ENTER) {
          theMap.setLocation(((TextBox)sender).getText());
        }
      }
    });
    tb.setWidth("100%");
    
    VerticalPanel vp = new VerticalPanel();
    
    vp.add(tb);
    vp.add(theMap);
    
    // Set the map up in a Dialog box, just for fun.
    DialogBox dialog = new DialogBox(false, false);
    dialog.setText("Drag me!");
    dialog.setWidget(vp);
    dialog.show();
  }
}
