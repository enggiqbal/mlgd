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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Graph element attributes.
 *
 * @param <K> the type of element accepted.
 * @param <V> the type of value accepted.
 */
public abstract class ElementAttribute<K extends Element, V> implements Attribute<V>, Iterable<Entry<K, V>> {

    private final Map<K, V> values = new HashMap<>();
    private V defaultValue;
    private final Class<?> valueClass;

    private final Set<Observer.ElementAttributeChanges<K>> observers = new HashSet<>();
    private final Set<K> changedElements = new HashSet<>();
    private boolean defaultChanged = false;
    private boolean bulkNotify = false;

    private String description = "";
    private String stateDescription = "";
    private boolean isSleeping = false;

    /**
     * Constructs a graph attribute.
     *
     * @param defaultValue the value of an element when not directly set.
     */
    public ElementAttribute(V defaultValue) {
        this.defaultValue = defaultValue;
        this.valueClass = defaultValue.getClass();
    }

    /**
     * Gets the attribute value for an element.
     *
     * @param element the element.
     * @return the element value.
     */
    public V get(K element) {
        if (values.containsKey(element)) {
            return values.get(element);
        } else {
            return defaultValue;
        }
    }

    /**
     * Sets the attribute value for an element.
     *
     * @param element the element.
     * @param value the value to be assigned.
     */
    public void set(K element, V value) {
        Rules.checkAttributeValue(value);
        checkType(value);
        values.put(element, value);

        isSleeping = false;
        changedElements.add(element);
        notifyObservers();
    }

    /**
     * Gets the default value.
     *
     * @return the default value.
     */
    @Override
    public V getDefault() {
        return defaultValue;
    }

    /**
     * Sets the default value.
     *
     * @param value the default value.
     */
    @Override
    public void setDefault(V value) {
        Rules.checkAttributeValue(value);
        checkType(value);
        defaultValue = value;

        isSleeping = false;
        defaultChanged = true;
        notifyObservers();
    }

    /**
     * Checks if the element does not have an assigned value.
     *
     * @param element the element.
     * @return true if the element does not have an assigned value, true
     * otherwise.
     */
    public boolean isDefault(K element) {
        return !values.containsKey(element);
    }

    /**
     * Removes the assigned value to an element.
     *
     * @param element the element.
     */
    public void clear(K element) {
        values.remove(element);

        changedElements.add(element);
        notifyObservers();
    }

    /**
     * Removes all assigned values.
     */
    public void reset() {
        values.clear();

        defaultChanged = true;
        notifyObservers();
    }

    /**
     * Removes all assigned values and assigned a new default value.
     *
     * @param newDefault the new default value.
     */
    public void reset(V newDefault) {
        values.clear();
        isSleeping = false;
        setDefault(newDefault);
    }

    /**
     * Allows to iterate through the entries.
     *
     * @return the entry iterator.
     */
    @Override
    public Iterator<Entry<K, V>> iterator() {
        return values.entrySet().iterator();
    }

    /**
     * Returns the elements with non-default value.
     *
     * @return the elements with non-default value.
     */
    public Set<K> nonDefaultElements() {
        return values.keySet();
    }

    /**
     * Copies the content of another attribute into this.
     *
     * @param otherAttribute the other attribute.
     */
    public void copy(ElementAttribute<K, V> otherAttribute) {
        reset(otherAttribute.defaultValue);
        for (Entry<K, V> entry : otherAttribute) {
            set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Checks the type of the value inserted to match the attribute definition.
     *
     * @param value
     */
    private void checkType(V value) {
        valueClass.cast(value);
    }

    /**
     * Register an element attribute observer.
     *
     * @param observer the observer.
     */
    protected void registerObserver(Observer.ElementAttributeChanges<K> observer) {
        observers.add(observer);
    }

    /**
     * Unregister an element attribute observer.
     *
     * @param observer the observer.
     */
    protected void unregisterObserver(Observer.ElementAttributeChanges<K> observer) {
        observers.remove(observer);
    }

    /**
     * Starts a bulk notification. All notifications are suspended until the
     * bulk notification end command, at which point all notifications are
     * transmitted in block.
     */
    public void startBulkNotification() {
        bulkNotify = true;
    }

    /**
     * Ends a bulk notification. All notifications stacked up during the bulk
     * notification interval are transmitted in block.
     */
    public void stopBulkNotification() {
        bulkNotify = false;
        notifyObservers();
    }

    /**
     * Notifies the observers.
     */
    private void notifyObservers() {
        if (bulkNotify) {
            return;
        }

        if (defaultChanged) {
            for (Observer.ElementAttributeChanges<K> observer : observers) {
                observer.updateAll();
            }
        } else if (!changedElements.isEmpty()) {
            for (Observer.ElementAttributeChanges<K> observer : observers) {
                observer.update(Collections.unmodifiableCollection(changedElements));
            }
        }

        changedElements.clear();
        defaultChanged = false;
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
