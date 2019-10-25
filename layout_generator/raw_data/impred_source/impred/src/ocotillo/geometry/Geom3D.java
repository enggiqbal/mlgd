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
 * Collects the geometry methods that operates on a 3D vectorial spaces. Unless
 * otherwise specified, it should be assumed that any dimension exceeding the
 * first two is ignored by the methods of this class and dropped from the
 * returned results.
 */
public class Geom3D extends GeomXD {

    @Override
    public int geomDim() {
        return 3;
    }

    /**
     * Returns the cross product of two vectors.
     *
     * @param vectorA the coordinates of the first vector.
     * @param vectorB the coordinates of the second vector.
     * @return the cross product.
     */
    public Coordinates crossProduct(Coordinates vectorA, Coordinates vectorB) {
        double x = vectorA.y() * vectorB.z() - vectorA.z() * vectorB.y();
        double y = vectorA.z() * vectorB.x() - vectorA.x() * vectorB.z();
        double z = vectorA.x() * vectorB.y() - vectorA.y() * vectorB.x();
        return new Coordinates(x, y, z);
    }
}
