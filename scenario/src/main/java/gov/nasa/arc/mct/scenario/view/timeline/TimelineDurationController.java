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
package gov.nasa.arc.mct.scenario.view.timeline;

import gov.nasa.arc.mct.scenario.component.ActivityComponent;
import gov.nasa.arc.mct.scenario.component.DurationCapability;
import gov.nasa.arc.mct.scenario.view.AbstractTimelineView;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Mouse controls for moving & resizing activities/decisions within a timeline
 * @author vwoeltje
 *
 */
public class TimelineDurationController extends MouseAdapter {
	private ActivityComponent parentComponent = null;
	private DurationCapability durationCapability; 
	private AbstractTimelineView parentView;
	
	private Map<Integer, DurationHandle> handles = new HashMap<Integer, DurationHandle>();
	private DurationHandle activeHandle = null;
	private int            initialX     = 0;
	private long           initialStart = 0;
	private long           initialEnd   = 0;
	private int            priorX       = 0;
	
	public TimelineDurationController(ActivityComponent immediateParent, DurationCapability dc,
			AbstractTimelineView parent) {
		this(dc, parent);
		this.parentComponent = immediateParent;
	}
	
	public TimelineDurationController(DurationCapability dc,
			AbstractTimelineView parent) {
		super();
		this.durationCapability = dc;
		this.parentView = parent;
		
		handles.put(Cursor.E_RESIZE_CURSOR, new DurationHandle() {
			@Override
			public void mouseDragged(long timeDifference) {
				timeDifference = clamp(timeDifference, initialEnd, initialStart, parentView.getEnd());		
				durationCapability.setEnd(initialEnd + timeDifference);
			}			
		});
		handles.put(Cursor.W_RESIZE_CURSOR, new DurationHandle() {
			@Override
			public void mouseDragged(long timeDifference) {
				timeDifference = clamp(timeDifference, initialStart, parentView.getStart(), initialEnd);
				durationCapability.setStart(initialStart + timeDifference);
			}			
		});
		handles.put(Cursor.MOVE_CURSOR, new DurationHandle() {
			@Override
			public void mouseDragged(long timeDifference) {
				timeDifference = clamp(timeDifference, initialStart, parentView.getStart(), parentView.getEnd() - (initialEnd-initialStart));
				durationCapability.setStart(initialStart + timeDifference);
				durationCapability.setEnd(initialEnd + timeDifference);
			}			
		});
		
	}

	/**
	 * Utility method to clamp the difference in time related to a mouse drag to 
	 * some appropriate time span (i.e. not off the edge of the timeline...)
	 * @param timeDifference
	 * @param initial
	 * @param min
	 * @param max
	 * @return
	 */
	private long clamp(long timeDifference, long initial, long min, long max) {
		if (timeDifference < 0 && initial + timeDifference < min) {
			timeDifference = min - initial;
		}
		if (timeDifference > 0 && initial + timeDifference > max) {
			timeDifference = max - initial;
		}
		return timeDifference;
	}
	
	@Override
	public void mousePressed(MouseEvent e) {		
		Object src = e.getSource();
		if (src instanceof Component) {
			Component comp = (Component) src;
			initialX = e.getXOnScreen();
			priorX = initialX;
			initialStart = durationCapability.getStart();
			initialEnd = durationCapability.getEnd();
			activeHandle = handles.get(comp.getCursor().getType());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		super.mouseReleased(e);
	}


	@Override
	public void mouseExited(MouseEvent e) {
		Object src = e.getSource();
		if (src instanceof Component) {
			Component comp = (Component) src;
			comp.setCursor(Cursor.getDefaultCursor());
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (activeHandle != null) {
			boolean isTowardStart = e.getXOnScreen() < priorX;
			int xDiff = e.getXOnScreen() - initialX;
			long tDiff = (long) (xDiff / parentView.getPixelScale());			
			
			activeHandle.mouseDragged(tDiff);

			if (parentComponent != null) {
				parentComponent.constrainChildren(durationCapability, isTowardStart);
			}
			
			parentView.revalidate();
			parentView.repaint();
			// parentView.stateChanged(null); Consider this in lieu of above
			Object src = e.getSource();
			if (src instanceof Component) {
				((Component) src).invalidate();
				((Component) src).validate();
				((Component) src).repaint();
			}			
			 
			parentView.getManifestedComponent().save();
			priorX = e.getXOnScreen();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Object src = e.getSource();
		if (src instanceof Component) {
			Component comp = (Component) src;
			if (e.getX() < 2) {
				comp.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));;
			} else if (e.getX() > comp.getWidth() - 3) {
				comp.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			} else {
				comp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
		}

	}

	
	private abstract class DurationHandle {
		public abstract void mouseDragged(long timeDifference);
	}
}
