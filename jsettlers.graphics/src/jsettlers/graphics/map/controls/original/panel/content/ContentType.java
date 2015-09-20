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

import jsettlers.graphics.ui.UIPanel;

/**
 * This are the main content types
 * 
 * @author michael
 */
public final class ContentType {
	public static final AbstractContentProvider EMPTY = new AbstractContentProvider() {
		@Override
		public UIPanel getPanel() {
			return new UIPanel();
		}

		@Override
		public boolean isForSelection() {
			// This is the empty selection content.
			return true;
		};
	};

	public static final AbstractContentProvider BUILD_NORMAL = BuildingBuildContent.getNormal();
	public static final AbstractContentProvider BUILD_SOCIAL = BuildingBuildContent.getSocial();
	public static final AbstractContentProvider BUILD_MILITARY = BuildingBuildContent.getMilitary();
	public static final AbstractContentProvider BUILD_FOOD = BuildingBuildContent.getFood();

	public static final AbstractContentProvider STOCK = new InventoryPanel();
	public static final AbstractContentProvider TOOLS = new ToolsPanel();
	public static final AbstractContentProvider GOODS_SPREAD = new DistributionPanel();
	public static final AbstractContentProvider GOODS_TRANSPORT = new MaterialPriorityContent();

	public static final AbstractContentProvider SETTLERSTATISTIC = EMPTY;
	public static final AbstractContentProvider PROFESSION = EMPTY;
	public static final AbstractContentProvider WARRIORS = EMPTY;
	public static final AbstractContentProvider PRODUCTION = EMPTY;

	private ContentType() {
	}
}
