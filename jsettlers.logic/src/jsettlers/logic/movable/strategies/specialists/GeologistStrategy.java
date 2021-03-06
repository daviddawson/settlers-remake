package jsettlers.logic.movable.strategies.specialists;

import jsettlers.common.map.shapes.HexBorderArea;
import jsettlers.common.material.ESearchType;
import jsettlers.common.movable.EAction;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.MovableStrategy;

/**
 * 
 * @author Andreas Eberle
 * 
 */
public final class GeologistStrategy extends MovableStrategy {
	private static final long serialVersionUID = 1L;

	private static final float ACTION1_DURATION = 1.4f;
	private static final float ACTION2_DURATION = 1.5f;

	private EGeologistState state = EGeologistState.JOBLESS;
	private ShortPoint2D centerPos;

	public GeologistStrategy(Movable movable) {
		super(movable);
	}

	@Override
	protected void action() {
		switch (state) {
		case JOBLESS:
			return;

		case GOING_TO_POS: {
			ShortPoint2D pos = super.getPos();

			if (centerPos == null) {
				this.centerPos = pos;
			}

			super.getStrategyGrid().setMarked(pos, false); // unmark the pos for the following check
			if (canWorkOnPos(pos)) {
				super.getStrategyGrid().setMarked(pos, true);
				super.playAction(EAction.ACTION1, ACTION1_DURATION);
				state = EGeologistState.PLAYING_ACTION_1;
			} else {
				findWorkablePosition();
			}
		}
			break;

		case PLAYING_ACTION_1:
			super.playAction(EAction.ACTION2, ACTION2_DURATION);
			state = EGeologistState.PLAYING_ACTION_2;
			break;

		case PLAYING_ACTION_2: {
			ShortPoint2D pos = super.getPos();
			super.getStrategyGrid().setMarked(pos, false);
			if (canWorkOnPos(pos)) {
				executeAction(pos);
			}

			findWorkablePosition();
		}
			break;
		}
	}

	private void findWorkablePosition() {
		ShortPoint2D closeWorkablePos = getCloseWorkablePos();

		if (closeWorkablePos != null && super.goToPos(closeWorkablePos)) {
			super.getStrategyGrid().setMarked(closeWorkablePos, true);
			this.state = EGeologistState.GOING_TO_POS;
			return;
		}
		centerPos = null;

		ShortPoint2D pos = super.getPos();
		if (super.preSearchPath(true, pos.x, pos.y, (short) 30, ESearchType.RESOURCE_SIGNABLE)) {
			super.followPresearchedPath();
			this.state = EGeologistState.GOING_TO_POS;
			return;
		}

		this.state = EGeologistState.JOBLESS;
	}

	private final ShortPoint2D getCloseWorkablePos() {
		ShortPoint2D bestNeighbourPos = null;
		double bestNeighbourDistance = Double.MAX_VALUE; // distance from start point

		for (ShortPoint2D satelitePos : new HexBorderArea(super.getPos(), (short) 2)) {
			if (super.isValidPosition(satelitePos) && canWorkOnPos(satelitePos)) {
				double distance = Math.hypot(satelitePos.x - centerPos.x, satelitePos.y - centerPos.y);
				if (distance < bestNeighbourDistance) {
					bestNeighbourDistance = distance;
					bestNeighbourPos = satelitePos;
				}
			}
		}
		return bestNeighbourPos;
	}

	private void executeAction(ShortPoint2D pos) {
		super.getStrategyGrid().executeSearchType(pos, ESearchType.RESOURCE_SIGNABLE);
	}

	private boolean canWorkOnPos(ShortPoint2D pos) {
		return super.fitsSearchType(pos, ESearchType.RESOURCE_SIGNABLE);
	}

	@Override
	protected boolean isMoveToAble() {
		return true;
	}

	@Override
	protected void moveToPathSet(ShortPoint2D oldPosition, ShortPoint2D oldTargetPos, ShortPoint2D targetPos) {
		this.state = EGeologistState.GOING_TO_POS;
		centerPos = null;

		super.getStrategyGrid().setMarked(oldPosition, false);

		if (oldTargetPos != null) {
			super.getStrategyGrid().setMarked(oldTargetPos, false);
		}
	}

	@Override
	protected void stopOrStartWorking(boolean stop) {
		if (stop) {
			state = EGeologistState.JOBLESS;
		} else {
			state = EGeologistState.GOING_TO_POS;
		}
	}

	@Override
	protected void strategyKilledEvent(ShortPoint2D pathTarget) {
		if (pathTarget != null) {
			super.getStrategyGrid().setMarked(pathTarget, false);
		} else {
			super.getStrategyGrid().setMarked(super.getPos(), false);
		}
	}

	private static enum EGeologistState {
		JOBLESS,
		GOING_TO_POS,
		PLAYING_ACTION_1,
		PLAYING_ACTION_2
	}
}
