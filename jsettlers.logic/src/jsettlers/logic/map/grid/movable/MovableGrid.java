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
package jsettlers.logic.map.grid.movable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import jsettlers.common.map.shapes.HexBorderArea;
import jsettlers.common.map.shapes.HexGridArea;
import jsettlers.common.map.shapes.IMapArea;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.movable.IMovable;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.SerializationUtils;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.map.grid.landscape.IWalkableGround;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.interfaces.IAttackable;

/**
 * This grid stores the position of the {@link IMovable}s.
 * 
 * @author Andreas Eberle
 */
public final class MovableGrid implements Serializable {
	private static final long serialVersionUID = 7003522358013103962L;

	private transient Movable[] movableGrid;
	private final IWalkableGround ground;
	private final short width;

	private final short height;

	public MovableGrid(short width, short height, IWalkableGround ground) {
		this.width = width;
		this.height = height;
		this.ground = ground;
		this.movableGrid = new Movable[width * height];
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		SerializationUtils.writeSparseArray(oos, movableGrid);
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		movableGrid = SerializationUtils.readSparseArray(ois, Movable.class);
	}

	public final Movable getMovableAt(int x, int y) {
		return this.movableGrid[x + y * width];
	}

	public final void setMovable(short x, short y, Movable movable) {
		this.movableGrid[x + y * width] = movable;
	}

	public final void movableLeft(ShortPoint2D position, Movable movable) {
		int idx = position.x + position.y * width;
		if (this.movableGrid[idx] == movable) {
			this.movableGrid[idx] = null;
		}
	}

	/**
	 * Lets the given movable enter the given position.
	 * 
	 * @param position
	 *            Position to be entered.
	 * @param movable
	 *            Movable that enters the position.
	 */
	public final void movableEntered(ShortPoint2D position, Movable movable) {
		short x = position.x;
		short y = position.y;

		int idx = x + y * width;
		if (idx < 0) {
			System.out.println("index < 0");
		}

		this.movableGrid[idx] = movable;
		if (movable != null && movable.getMovableType() == EMovableType.BEARER) {
			ground.walkOn(x, y);
		}

	}

	/**
	 * 
	 * @param movable
	 *            The movable that needs to inform the others.
	 * @param x
	 *            x coordinate of the movables position.
	 * @param y
	 *            y coordinate of the movables position.
	 * @param informFullArea
	 *            If true, the full soldier update area is informed if the given movable is attackable.<br>
	 *            If false, only a circle is informed if the given movable is attackable.
	 */
	public void informMovables(Movable movable, short x, short y, boolean informFullArea) {
		// inform all movables of the given movable
		IMapArea area;
		if (informFullArea) {
			area = new HexGridArea(x, y, (short) 1, Constants.SOLDIER_SEARCH_RADIUS);
		} else {
			area = new HexBorderArea(x, y, (short) (Constants.SOLDIER_SEARCH_RADIUS - 1));
		}

		boolean foundOne = false;
		byte movablePlayer = movable.getPlayerId();

		for (ShortPoint2D curr : area) {
			short currX = curr.x;
			short currY = curr.y;
			if (0 <= currX && currX < width && 0 <= currY && currY < height) {
				Movable currMovable = getMovableAt(currX, currY);
				if (currMovable != null && isEnemy(movablePlayer, currMovable)) {
					currMovable.informAboutAttackable(movable);

					if (!foundOne) { // the first found movable is the one closest to the given movable.
						movable.informAboutAttackable(currMovable);
						foundOne = true;
					}
				}
			}
		}
	}

	// FIXME @Andreas Eberle replace player everywhere by an object with team and player and move this method to the new class
	/**
	 * 
	 * @param player
	 *            The player id of the first player.
	 * @param otherAttackable
	 *            The other attackable. (Must not be null!)
	 * 
	 * @return
	 */
	public static boolean isEnemy(byte player, IAttackable otherAttackable) {
		return otherAttackable.getPlayerId() != player && otherAttackable.isAttackable();
	}

	public boolean hasNoMovableAt(int x, int y) {
		return getMovableAt(x, y) == null;
	}
}
