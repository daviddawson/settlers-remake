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
package jsettlers.graphics.map.controls.original.panel.content;

import go.graphics.GLDrawContext;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.images.EImageLinkType;
import jsettlers.common.images.ImageLink;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.position.FloatRectangle;
import jsettlers.graphics.action.BuildAction;
import jsettlers.graphics.image.Image;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.graphics.utils.Button;

/**
 * This is a button to construct a building.
 * 
 * @author michael
 */
public class BuildingButton extends Button {
	private static final OriginalImageLink activeMark = new OriginalImageLink(EImageLinkType.GUI, 3, 123, 0);
	private static final float ICON_BUTTON_RATIO = 0.85f;

	private final ImageLink buildingImage;
	private final EBuildingType buildingType;

	private float lastButtonHeight;
	private float lastButtonWidth;
	private float lastImageHeight;
	private float lastImageWidth;
	private float iconLeft;
	private float iconRight;
	private float iconTop;
	private float iconBottom;

	public BuildingButton(EBuildingType buildingType) {
		super(new BuildAction(buildingType), null, null, Labels.getName(buildingType));
		this.buildingType = buildingType;
		buildingImage = buildingType.getGuiImage();
	}

	@Override
	public void drawAt(GLDrawContext gl) {
		drawBackground(gl);
		if (isActive()) {
			gl.color(1, 1, 1, 1);
			FloatRectangle position = getPosition();
			ImageProvider.getInstance().getImage(activeMark, lastButtonWidth, lastButtonHeight)
					.drawImageAtRect(gl, position.getMinX(), position.getMinY(), position.getMaxX(), position.getMaxY());
		}
	}

	@Override
	protected void drawBackground(GLDrawContext gl) {
		FloatRectangle position = getPosition();
		Image image; // TODO keep reference to image and only refetch & re-calcCoords if the button has resized?
		if (buildingImage instanceof OriginalImageLink) {
			image = ImageProvider.getInstance().getImage(buildingImage, position.getWidth(), position.getHeight());
		} else {
			image = ImageProvider.getInstance().getImage(buildingImage);
		}

		float buttonHeight = position.getHeight();
		float buttonWidth = position.getWidth();
		float imageHeight = image.getHeight();
		float imageWidth = image.getWidth();
		if (buttonHeight != lastButtonHeight || buttonWidth != lastButtonWidth ||
				imageHeight != lastImageHeight || imageWidth != lastImageWidth) {
			calculateIconCoords(buttonHeight, buttonWidth, position.getCenterX(), position.getCenterY(), imageHeight, imageWidth);
		}

		gl.color(1, 1, 1, 1);
		image.drawImageAtRect(gl, iconLeft, iconTop, iconRight, iconBottom);
	}

	private void calculateIconCoords(float buttonHeight, float buttonWidth, float btnXMid, float btnYMid,
			float imageHeight, float imageWidth) {
		float scaling = Math.min(buttonHeight * ICON_BUTTON_RATIO / imageHeight, buttonWidth * ICON_BUTTON_RATIO / imageWidth);
		float imgScaledWidth = imageWidth * scaling;
		float imgScaledHeight = imageHeight * scaling;
		iconLeft = btnXMid - imgScaledWidth / 2;
		iconRight = btnXMid + imgScaledWidth / 2;
		iconTop = btnYMid - imgScaledHeight / 2;
		iconBottom = btnYMid + imgScaledHeight / 2;
		lastButtonHeight = buttonHeight;
		lastButtonWidth = buttonWidth;
		lastImageHeight = imageHeight;
		lastImageWidth = imageWidth;
	}

	public EBuildingType getBuildingType() {
		return buildingType;
	}
}
