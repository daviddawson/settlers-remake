/*******************************************************************************
 * Copyright (c) 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.map.newGrid.partition.manager.materials.interfaces;

import java.io.Serializable;

import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.map.newGrid.partition.manager.materials.MaterialsManager;

/**
 * This interface defines methods needed by the {@link MaterialsManager} to get jobless movables to give them jobs.
 * 
 * @author Andreas Eberle
 * 
 */
public interface IJoblessSupplier extends Serializable {

	/**
	 * States if this {@link IJoblessSupplier} has no more jobless left.
	 * 
	 * @return Returns true if there are no jobless left <br>
	 *         false otherwise.
	 */
	boolean isEmpty();

	/**
	 * This method returns the jobless closest to the given position.
	 * 
	 * @param position
	 *            The position is used as the center of the search for a jobless.
	 * 
	 * @return Returns the jobless closest to the given position<br>
	 *         or null if none has been found.
	 */
	IManagerBearer removeJoblessCloseTo(ShortPoint2D position);

}
