/*******************************************************************************
 * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
 * as represented by the Administrator of the National Aeronautics and Space 
 * Administration. All rights reserved.
 *
 * The MCT platform is licensed under the Apache License, Version 2.0 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations under 
 * the License.
 *
 * MCT includes source code licensed under additional open source licenses. See 
 * the MCT Open Source Licenses file included with this distribution or the About 
 * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
 * information. 
 *******************************************************************************/
package gov.nasa.arc.mct.scenario.component;

import gov.nasa.arc.mct.components.AbstractComponent;
import gov.nasa.arc.mct.components.PropertyEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagPropertyEditor implements PropertyEditor<List<AbstractComponent>> {
	private AbstractComponent component;
	
	public TagPropertyEditor(AbstractComponent component) {
		super();
		this.component = component;
	}

	@Override
	public String getAsText() {
		// Unused
		return "";
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		// Unused
	}

	@Override
	public List<AbstractComponent> getValue() {
		List<AbstractComponent> current = new ArrayList<AbstractComponent>();
		for (TagCapability tc : component.getCapabilities(TagCapability.class)) {
			current.add(tc.getComponentRepresentation());
		}
		return current;
	}

	@Override
	public void setValue(Object value) throws IllegalArgumentException {
		if (!(value instanceof List)) {
			throw new IllegalArgumentException("Must provide a list");
		}
		
		List<AbstractComponent> newChildren = new ArrayList<AbstractComponent>();
		for (Object element : (List<?>) value) {
			if (!(element instanceof AbstractComponent)) {
				throw new IllegalArgumentException("Must provide list of AbstractComponents");
			} else {
				newChildren.add((AbstractComponent) element);
			}
		}
		
		List<AbstractComponent> toRemove = getValue();
		toRemove.removeAll(newChildren);
		
		newChildren.removeAll(getValue());
		
		component.removeDelegateComponents(toRemove);
		component.addDelegateComponents(newChildren);
	}

	@Override
	public List<List<AbstractComponent>> getTags() {
		// Unused
		return Collections.emptyList();
	}
	
}
