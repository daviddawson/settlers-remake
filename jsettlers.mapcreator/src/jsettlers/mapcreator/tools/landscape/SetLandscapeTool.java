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
package jsettlers.mapcreator.tools.landscape;

import jsettlers.common.landscape.ELandscapeType;
import jsettlers.common.map.shapes.IMapArea;
import jsettlers.common.map.shapes.MapCircle;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.mapcreator.data.MapData;
import jsettlers.mapcreator.localization.EditorLabels;
import jsettlers.mapcreator.tools.Tool;
import jsettlers.mapcreator.tools.buffers.ByteMapArea;
import jsettlers.mapcreator.tools.buffers.GlobalShapeBuffer;
import jsettlers.mapcreator.tools.shapes.LineCircleShape;
import jsettlers.mapcreator.tools.shapes.LineShape;
import jsettlers.mapcreator.tools.shapes.NoisyLineCircleShape;
import jsettlers.mapcreator.tools.shapes.PointShape;
import jsettlers.mapcreator.tools.shapes.ShapeType;

public class SetLandscapeTool implements Tool {

	private static final ShapeType[] SHAPES = new ShapeType[] {
			new PointShape(),
			new LineShape(),
			new LineCircleShape(),
			new NoisyLineCircleShape(),
	};

	private static final ShapeType[] RIVERSHAPES = new ShapeType[] {
			new PointShape(), new LineShape(),
	};

	private final ELandscapeType type;

	private final boolean isRiver;

	private GlobalShapeBuffer buffer;

	public SetLandscapeTool(ELandscapeType type, boolean isRiver) {
		this.type = type;
		this.isRiver = isRiver;
	}

	@Override
	public String getName() {
		return String.format(EditorLabels.getLabel("landscapedescr"), EditorLabels.getLabel("landscape_" + type));
	}

	@Override
	public ShapeType[] getShapes() {
		return isRiver ? RIVERSHAPES : SHAPES;
	}

	@Override
	public void apply(MapData map, ShapeType shape, ShortPoint2D start,
			ShortPoint2D end, double uidx) {
		if (buffer == null) {
			buffer = new GlobalShapeBuffer(map.getWidth(), map.getHeight());
		}

		short startx = start.x;
		short endx = end.x;
		short starty = start.y;
		short endy = end.y;
		int size = shape.getSize();
		int usedminx = Math.min(startx, endx) - size - 3;
		int usedminy = Math.min(starty, endy) - (int) (size / MapCircle.Y_SCALE) - 3;
		int usedmaxx = Math.max(startx, endx) + size + 3;
		int usedmaxy = Math.max(starty, endy) + (int) (size / MapCircle.Y_SCALE) + 3;
		byte[][] array =
				buffer.getArray(usedminx, usedminy, usedmaxx, usedmaxy);

		shape.setAffectedStatus(array, start, end);

		IMapArea area = new ByteMapArea(array);

		map.fill(type, area);
	}

	@Override
	public void start(MapData data, ShapeType shape, ShortPoint2D pos) {
	}

}
