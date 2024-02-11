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
package ocotillo.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Graph attribute.
 *
 * @param <V> the type of value accepted.
 */
public class GraphAttribute<V> implements Attribute<V> {

    private V value;
    private final Class<?> valueClass;

    private final Set<Observer.GraphAttributeChanges> observers = new HashSet<>();

    private String description = "";
    private String stateDescription = "";
    private boolean isSleeping = false;

    /**
     * Constructs a graph attribute.
     *
     * @param value its current value.
     */
    public GraphAttribute(V value) {
        Rules.checkAttributeValue(value);
        this.value = value;
        this.valueClass = value.getClass();
    }

    /**
     * Gets the current value.
     *
     * @return the value.
     */
    public V get() {
        return value;
    }

    /**
     * Sets the current value.
     *
     * @param value the value.
     */
    public void set(V value) {
        Rules.checkAttributeValue(value);
        checkType(value);
        this.value = value;
        isSleeping = false;
        notifyObservers();
    }

    /**
     * Checks of the type to be set is compatible with the previous type.
     *
     * @param value the value to be set.
     */
    private void checkType(V value) {
        valueClass.cast(value);
    }

    @Override
    public V getDefault() {
        return value;
    }

    @Override
    public void setDefault(V value) {
        set(value);
    }

    /**
     * Register a graph attribute observer.
     *
     * @param observer the observer.
     */
    protected void registerObserver(Observer.GraphAttributeChanges observer) {
        observers.add(observer);
    }

    /**
     * Unregister a graph attribute observer.
     *
     * @param observer the observer.
     */
    protected void unregisterObserver(Observer.GraphAttributeChanges observer) {
        observers.remove(observer);
    }

    /**
     * Notifies the observers.
     */
    private void notifyObservers() {
        for (Observer.GraphAttributeChanges observer : observers) {
            observer.update();
        }
    }

    @Override
    public Attribute.Type getAttributeType() {
        return Attribute.Type.graph;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    @Override
    public String getStateDescription() {
        return stateDescription;
    }

    @Override
    public boolean isSleeping() {
        return isSleeping;
    }

    /**
     * Sets the attribute as sleeping. Should only be called by the standard
     * attribute after its creation.
     */
    protected void setSleeping() {
        isSleeping = true;
    }

}
