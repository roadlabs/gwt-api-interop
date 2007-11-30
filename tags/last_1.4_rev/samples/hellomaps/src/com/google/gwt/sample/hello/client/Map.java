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

import com.google.gwt.core.client.GWT;
import com.google.gwt.jsio.client.JSFunction;
import com.google.gwt.jsio.client.JSWrapper;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Encapsulates a Google Map UI.
 */
public class Map extends Composite {

  /**
   * Polymorphism works just fine. Again, the name of this interface has no
   * direct bearing on the linkage, it's simply convenient to use the same name
   * as the Maps API documentation.
   */
  interface GControl extends JSWrapper {
  }

  /**
   * Provides access to the Google Maps geocoding service
   * 
   * @gwt.constructor $wnd.GClientGeocoder
   */
  interface Geocoder extends JSWrapper {
    /**
     * Sends a request to the geocoding service. The Geocoder will issue a
     * callback, so it's important that we can "pass" a Java code closure into a
     * JavaScript API. That's where JSFunction comes into play.
     * 
     * @param address The location to geocode
     * @param callback A callback that will be invoked when a result is
     *          returned.
     * @gwt.fieldName getLatLng
     */
    void lookup(String address, GeocoderCallback callback);
  }

  /**
   * A callback for the geocoding service. We explicity name the desired export
   * function so that the interface could provide additional functions. If the
   * class has only a single function, the annotation isn't strictly necessary.
   * 
   * @gwt.exported onGeocode
   */
  abstract class GeocoderCallback extends JSFunction {
    public abstract void onGeocode(GLatLng position);
  }

  /**
   * The naming of the class is arbitrary, but chosen for consistency with the
   * underlying JS API
   */
  interface GLatLng extends JSWrapper {
    /**
     * The naming of the method is arbitrary, the only thing that's important is
     * the presence of the gwt.constructor annotation.
     * 
     * @gwt.constructor $wnd.GLatLng
     */
    public GLatLng construct(double lat, double lng);

    public double lat();

    public double lng();
  }

  /**
   * This is the map type controller (map, satellite, hybrid)
   * 
   * @gwt.constructor $wnd.GMapTypeControl
   */
  interface GMapTypeControl extends GControl {
  }

  /**
   * This interface is named differently from the underlying name of the JS
   * class.
   */
  interface GoogleMap extends JSWrapper {
    /**
     * We need to be able to add some controls to the map, so we'll expose the
     * method to do so. See below for the two "implementations" of GControl.
     */
    public void addControl(GControl control);

    /**
     * Tells the map code to update its visual state when the size of the
     * containing element changes.
     */
    public void checkResize();

    /**
     * @gwt.constructor $wnd.GMap2
     */
    public GoogleMap construct(Element elt);

    /**
     * Pan the map to a new position.
     */
    public void panTo(GLatLng position);

    /**
     * No annotations are required since the arguments make this not a bean-
     * style setter.
     */
    public void setCenter(GLatLng position, int zoomLevel);
  }

  /**
   * This is the small map controller (directional control and zoom).
   * 
   * @gwt.constructor $wnd.GSmallMapControl
   */
  interface GSmallMapControl extends GControl {
  }

  // GWT.create the Map and geocoding service
  final GoogleMap map = (GoogleMap)GWT.create(GoogleMap.class);
  final Geocoder geocoder = (Geocoder)GWT.create(Geocoder.class);

  public Map() {
    final SimplePanel panel = new SimplePanel();
    panel.setPixelSize(400, 400);

    map.construct(panel.getElement());

    // Add some standard controllers
    map.addControl((GControl)GWT.create(GSmallMapControl.class));
    map.addControl((GControl)GWT.create(GMapTypeControl.class));

    // Create a position wrapper
    final GLatLng center = (GLatLng)GWT.create(GLatLng.class);
    // Set the position of the wrapper (Palto Alto)
    center.construct(37.4419, -122.1419);

    // Set the center and zoom level
    map.setCenter(center, 12);

    initWidget(panel);
  }

  public void onLoad() {
    // The map control needs to be kicked when its enclosing element changes
    // size.
    map.checkResize();
  }

  public void setLocation(String address) {
    // Notice that the callbacks work correctly with anonymous classes
    geocoder.lookup(address, new GeocoderCallback() {
      // This will move the map to the location retured by the service
      public void onGeocode(GLatLng position) {
        if (position == null) {
          Window.alert("The name you entered couldn't be found.");
        } else {
          map.panTo(position);
        }
      }
    });
  }
}
