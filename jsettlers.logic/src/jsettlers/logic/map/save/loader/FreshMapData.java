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
package jsettlers.logic.map.save.loader;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.landscape.EResourceType;
import jsettlers.common.map.IMapData;
import jsettlers.common.map.object.MapObject;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.map.save.FreshMapSerializer.IMapDataReceiver;

/**
 * This is a map data receiver that stores the given data and provides access to it via the {@link IMapData} interface.
 * 
 * @author michael
 */
public class FreshMapData implements IMapDataReceiver, IMapData {

	private int width;
	private int height;

	private int playerCount;
	private ShortPoint2D[] playerStarts;

	private byte[][] heights;
	private ELandscapeType[][] landscapes;
	private MapObject[][] mapObjects;
	private EResourceType[][] resourceTypes;
	private byte[][] resourceAmount;
	private short[][] blockedPartitions;

	@Override
	public void setDimension(int width, int height, int playerCount) {
		this.width = width;
		this.height = height;
		this.playerCount = playerCount;
		this.playerStarts = new ShortPoint2D[playerCount];
		this.heights = new byte[width][height];
		this.landscapes = new ELandscapeType[width][height];
		this.mapObjects = new MapObject[width][height];
		this.resourceTypes = new EResourceType[width][height];
		this.resourceAmount = new byte[width][height];
		this.blockedPartitions = new short[width][height];
	}

	@Override
	public void setPlayerStart(byte player, int x, int y) {
		playerStarts[player] = new ShortPoint2D(x, y);
	}

	@Override
	public void setHeight(int x, int y, byte height) {
		heights[x][y] = height;
	}

	@Override
	public void setLandscape(int x, int y, ELandscapeType type) {
		landscapes[x][y] = type;
	}

	@Override
	public void setMapObject(int x, int y, MapObject object) {
		mapObjects[x][y] = object;
	}

	/* - - - - - - IMapData interface - - - - - - - */

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public ELandscapeType getLandscape(int x, int y) {
		return landscapes[x][y];
	}

	@Override
	public MapObject getMapObject(int x, int y) {
		return mapObjects[x][y];
	}

	@Override
	public byte getLandscapeHeight(int x, int y) {
		return heights[x][y];
	}

	@Override
	public ShortPoint2D getStartPoint(int player) {
		return playerStarts[player];
	}

	@Override
	public int getPlayerCount() {
		return playerCount;
	}

	@Override
	public EResourceType getResourceType(short x, short y) {
		return resourceTypes[x][y];
	}

	@Override
	public byte getResourceAmount(short x, short y) {
		return resourceAmount[x][y];
	}

	@Override
	public void setResources(int x, int y, EResourceType type, byte amount) {
		resourceAmount[x][y] = amount;
		resourceTypes[x][y] = type;
	}

	@Override
	public short getBlockedPartition(short x, short y) {
		return blockedPartitions[x][y];
	}

	@Override
	public void setBlockedPartition(int x, int y, short blockedPartition) {
		blockedPartitions[x][y] = blockedPartition;
	}
}
