package jsettlers.input;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jsettlers.common.map.shapes.HexBorderArea;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.map.UIState;
import jsettlers.input.tasks.ConstructBuildingTask;
import jsettlers.input.tasks.ConvertGuiTask;
import jsettlers.input.tasks.DestroyBuildingGuiTask;
import jsettlers.input.tasks.EGuiAction;
import jsettlers.input.tasks.MovableGuiTask;
import jsettlers.input.tasks.MoveToGuiTask;
import jsettlers.input.tasks.SetBuildingPriorityGuiTask;
import jsettlers.input.tasks.SetMaterialDistributionSettingsGuiTask;
import jsettlers.input.tasks.SetMaterialPrioritiesGuiTask;
import jsettlers.input.tasks.SimpleGuiTask;
import jsettlers.input.tasks.WorkAreaGuiTask;
import jsettlers.logic.buildings.Building;
import jsettlers.logic.buildings.military.OccupyingBuilding;
import jsettlers.logic.movable.Movable;
import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.synchronic.timer.ITaskExecutor;

/**
 * 
 * @author Andreas Eberle
 * 
 */
public class GuiTaskExecutor implements ITaskExecutor {
	private static GuiTaskExecutor instance = null;
	private final IGuiInputGrid grid;
	private final ITaskExecutorGuiInterface guiInterface;
	private final byte playerId;

	public GuiTaskExecutor(IGuiInputGrid grid, ITaskExecutorGuiInterface guiInterface, byte playerId) {
		this.grid = grid;
		this.guiInterface = guiInterface;
		this.playerId = playerId;
	}

	public static GuiTaskExecutor get() {
		return instance;
	}

	@Override
	public void executeTask(TaskPacket iTask) {
		if (!(iTask instanceof SimpleGuiTask)) {
			return;
		}

		SimpleGuiTask guiTask = (SimpleGuiTask) iTask;

		System.out.println("executeTask(GuiTask): " + guiTask.getGuiAction());
		switch (guiTask.getGuiAction()) {
		case SET_WORK_AREA: {
			WorkAreaGuiTask task = (WorkAreaGuiTask) guiTask;
			setWorkArea(task.getPosition(), task.getBuildingPos().x, task.getBuildingPos().y);
		}
			break;

		case BUILD: {
			ConstructBuildingTask task = (ConstructBuildingTask) guiTask;
			grid.constructBuildingAt(task.getPosition(), task.getType(), task.getPlayerId());
		}
			break;

		case MOVE_TO: {
			MoveToGuiTask task = (MoveToGuiTask) guiTask;
			moveSelectedTo(task.getPosition(), task.getSelection());
		}
			break;

		case QUICK_SAVE:
			save();
			break;

		case DESTROY_BUILDING: {
			ShortPoint2D buildingPos = ((DestroyBuildingGuiTask) guiTask).getPosition();
			Building building = ((Building) grid.getBuildingAt(buildingPos.x, buildingPos.y));
			if (building != null) {
				building.kill();
			}
		}
			break;

		case DESTROY_MOVABLES:
			killSelectedMovables(((MovableGuiTask) guiTask).getSelection());
			break;

		case START_WORKING:
		case STOP_WORKING:
			stopOrStartWorking(((MovableGuiTask) guiTask).getSelection(), guiTask.getGuiAction() == EGuiAction.STOP_WORKING);
			break;

		case CONVERT:
			convertMovables((ConvertGuiTask) guiTask);
			break;

		case SET_BUILDING_PRIORITY:
			setBuildingPriority((SetBuildingPriorityGuiTask) guiTask);
			break;

		case SET_MATERIAL_DISTRIBUTION_SETTINGS: {
			SetMaterialDistributionSettingsGuiTask task = (SetMaterialDistributionSettingsGuiTask) guiTask;
			grid.setMaterialDistributionSettings(task.getManagerPosition(), task.getMaterialType(), task.getProbabilities());
		}
			break;

		case SET_MATERIAL_PRIORITIES: {
			SetMaterialPrioritiesGuiTask task = (SetMaterialPrioritiesGuiTask) guiTask;
			grid.setMaterialPrioritiesSetting(task.getManagerPosition(), task.getMaterialTypeForPriority());
		}
			break;

		default:
			break;

		}
	}

	private void save() {
		try {
			byte numberOfPlayers = grid.getNumberOfPlayers();
			PlayerState[] playerStates = new PlayerState[numberOfPlayers];
			for (byte playerId = 0; playerId < numberOfPlayers; playerId++) {
				// find a tower of the player
				UIState uiState = null;
				for (Building building : Building.getAllBuildings()) {
					if (building.getPlayer().playerId == playerId && building instanceof OccupyingBuilding) {
						uiState = new UIState(building.getPos());
						break;
					}
				}

				playerStates[playerId] = new PlayerState(playerId, uiState);
			}
			playerStates[playerId] = new PlayerState(this.playerId, guiInterface.getUIState(), grid.getFogOfWar());
			grid.save(playerStates);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setBuildingPriority(SetBuildingPriorityGuiTask guiTask) {
		ShortPoint2D pos = guiTask.getBuildingPosition();
		Building building = ((Building) grid.getBuildingAt(pos.x, pos.y));
		if (building != null) {
			building.setPriority(guiTask.getNewPriority());
		}
	}

	private void convertMovables(ConvertGuiTask guiTask) {
		for (Integer currID : guiTask.getSelection()) {
			Movable movable = Movable.getMovableByID(currID);
			if (movable != null) {
				movable.convertTo(guiTask.getTargetType());
			}
		}
		guiInterface.refreshSelection();
	}

	private void stopOrStartWorking(List<Integer> selectedMovables, boolean stop) {
		for (Integer currID : selectedMovables) {
			Movable.getMovableByID(currID).stopOrStartWorking(stop);
		}
	}

	private void killSelectedMovables(List<Integer> selectedMovables) {
		for (Integer currID : selectedMovables) {
			Movable curr = Movable.getMovableByID(currID);
			if (curr != null)
				curr.kill();
		}
	}

	/**
	 * Move the selected {@link Movable} to the given position.
	 * 
	 * @param targetPosition
	 *            position to move to
	 * @param movableIds
	 *            A list of the id's of the movables.
	 */
	private void moveSelectedTo(ShortPoint2D targetPosition, List<Integer> movableIds) {
		if (movableIds.size() == 1) {
			Movable currMovable = Movable.getMovableByID(movableIds.get(0));
			if (currMovable != null)
				currMovable.moveTo(targetPosition);
		} else if (!movableIds.isEmpty()) {
			short radius = 1;
			short ringsWithoutSuccessCtr = 0; // used to stop the loop
			Iterator<ShortPoint2D> posIterator = new HexBorderArea(targetPosition, radius).iterator();

			for (Integer currMovableId : movableIds) {
				Movable currMovable = Movable.getMovableByID(currMovableId);

				if (currMovable == null) {
					continue;
				}

				ShortPoint2D currTargetPos;

				do {
					if (!posIterator.hasNext()) {
						ringsWithoutSuccessCtr++;
						if (ringsWithoutSuccessCtr > 5) {
							return; // the rest of the movables can't be sent to the target.
						}

						radius++;
						posIterator = new HexBorderArea(targetPosition, radius).iterator();
					}

					currTargetPos = posIterator.next();
				} while (!canMoveTo(currMovable, currTargetPos));

				ringsWithoutSuccessCtr = 0;
				currMovable.moveTo(currTargetPos);
			}
		}
	}

	private boolean canMoveTo(Movable movable, ShortPoint2D potentialTargetPos) {
		return grid.isInBounds(potentialTargetPos) && !grid.isBlocked(potentialTargetPos)
				&& grid.getBlockedPartition(movable.getPos()) == grid.getBlockedPartition(potentialTargetPos);
	}

	private void setWorkArea(ShortPoint2D pos, short buildingX, short buildingY) {
		Building building = (Building) grid.getBuildingAt(buildingX, buildingY);

		if (building != null) {
			building.setWorkAreaCenter(pos);
		}
	}

}
