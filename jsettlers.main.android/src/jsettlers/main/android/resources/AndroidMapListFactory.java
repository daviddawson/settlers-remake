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
package jsettlers.main.android.resources;

import java.io.File;

import android.content.res.AssetManager;
import jsettlers.logic.map.save.DirectoryMapLister;
import jsettlers.logic.map.save.IMapListFactory;
import jsettlers.logic.map.save.MapList;

public class AndroidMapListFactory implements IMapListFactory {

	private final AssetManager manager;
	private final File writeableDir;

	public AndroidMapListFactory(AssetManager manager, File writeableDir) {
		super();
		this.manager = manager;
		this.writeableDir = writeableDir;
	}

	@Override
	public MapList getMapList() {
		return new MapList(new AndroidAssetsMapLister(manager, "maps"),
				new DirectoryMapLister(new File(writeableDir, "save"), true));
	}

}
