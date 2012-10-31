package jsettlers.logic.movable;

import jsettlers.TestWindow;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.action.Action;
import jsettlers.graphics.action.MoveToAction;
import jsettlers.graphics.map.IMapInterfaceListener;
import jsettlers.graphics.map.MapInterfaceConnector;
import jsettlers.input.SelectionSet;
import jsettlers.logic.movable.testmap.MovableTestsMap;
import jsettlers.logic.newmovable.NewMovable;
import random.RandomSingleton;
import synchronic.timer.NetworkTimer;

public class MovableTestWindow {
	public static void main(String args[]) throws InterruptedException {
		new MovableTestWindow();
	}

	private NewMovable movable;

	private MovableTestWindow() throws InterruptedException {
		NetworkTimer.get().schedule();
		RandomSingleton.load(1000);

		MovableTestsMap grid = new MovableTestsMap(100, 100);
		MapInterfaceConnector connector = TestWindow.openTestWindow(grid);

		movable = new NewMovable(grid.getMovableGrid(), EMovableType.PIONEER, new ShortPoint2D(49, 50), (byte) 0);
		movable.setSelected(true);

		connector.setSelection(new SelectionSet(movable));

		connector.addListener(new IMapInterfaceListener() {
			@Override
			public void action(Action action) {
				switch (action.getActionType()) {
				case MOVE_TO:
					movable.moveTo(((MoveToAction) action).getPosition());
					break;
				case SPEED_FASTER:
					NetworkTimer.multiplyGameSpeed(1.2f);
					break;
				case SPEED_SLOWER:
					NetworkTimer.multiplyGameSpeed(1 / 1.2f);
					break;
				case FAST_FORWARD:
					NetworkTimer.get().fastForward();
					break;
				default:
					break;
				}
			}
		});

		grid.getMovableGrid().dropMaterial(new ShortPoint2D(40, 40), EMaterialType.PLANK, true);
		grid.getMovableGrid().dropMaterial(new ShortPoint2D(60, 60), EMaterialType.STONE, true);

		new NewMovable(grid.getMovableGrid(), EMovableType.BEARER, new ShortPoint2D(30, 30), (byte) 0);
		new NewMovable(grid.getMovableGrid(), EMovableType.BEARER, new ShortPoint2D(31, 31), (byte) 0);
		new NewMovable(grid.getMovableGrid(), EMovableType.BEARER, new ShortPoint2D(32, 32), (byte) 0);
		new NewMovable(grid.getMovableGrid(), EMovableType.BEARER, new ShortPoint2D(33, 33), (byte) 0);

		new NewMovable(grid.getMovableGrid(), EMovableType.BEARER, new ShortPoint2D(50, 50), (byte) 0);

		{// test automatic distribution of many movables next to each other
			for (int x = 30; x < 40; x++) {
				for (int y = 80; y < 90; y++) {
					new NewMovable(grid.getMovableGrid(), EMovableType.BEARER, new ShortPoint2D(x, y), (byte) 0);
				}
			}
		}

		{
			Thread.sleep(3000);
			// circle of three movables blocking each others path
			NewMovable m1 = new NewMovable(grid.getMovableGrid(), EMovableType.PIONEER, new ShortPoint2D(50, 65), (byte) 0);
			NewMovable m2 = new NewMovable(grid.getMovableGrid(), EMovableType.PIONEER, new ShortPoint2D(51, 65), (byte) 0);
			NewMovable m3 = new NewMovable(grid.getMovableGrid(), EMovableType.PIONEER, new ShortPoint2D(50, 64), (byte) 0);

			m1.moveTo(new ShortPoint2D(52, 65));
			m2.moveTo(new ShortPoint2D(49, 63));
			m3.moveTo(new ShortPoint2D(50, 66));
		}
	}
}
