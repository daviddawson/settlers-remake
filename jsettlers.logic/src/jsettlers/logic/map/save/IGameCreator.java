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
package jsettlers.logic.map.save;

import jsettlers.common.map.MapLoadException;
import jsettlers.input.PlayerState;
import jsettlers.logic.map.grid.MainGrid;

/**
 * Classes of this interface are capable of creating a game.
 * 
 * @author michael
 * @author Andreas Eberle
 */
public interface IGameCreator {

	public MainGridWithUiSettings loadMainGrid(boolean[] availablePlayers) throws MapLoadException;

	public String getMapName();

	public String getMapId();

	public class MainGridWithUiSettings {
		private final MainGrid mainGrid;
		private final PlayerState[] playerStates;

		public MainGridWithUiSettings(MainGrid mainGrid, PlayerState[] playerStates) {
			this.mainGrid = mainGrid;
			this.playerStates = playerStates;
		}

		public MainGrid getMainGrid() {
			return mainGrid;
		}

		public PlayerState[] getPlayerStates() {
			return playerStates;
		}

		public PlayerState getPlayerState(byte playerId) {
			return playerStates[playerId];
		}
	}
}
