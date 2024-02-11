/**
 * Copyright © 2014-2016 Paolo Simonetto
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
package ocotillo.geometry;

/**
 * Provider of default geometries.
 */
public class Geom {

    public static final Geom1D e1D = new Geom1D();
    public static final Geom2D e2D = new Geom2D();
    public static final Geom3D e3D = new Geom3D();
    public static final GeomXD eXD = new GeomXD();

}
