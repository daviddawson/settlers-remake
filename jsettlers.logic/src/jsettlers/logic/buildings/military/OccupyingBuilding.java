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
package jsettlers.logic.buildings.military;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import jsettlers.algorithms.path.IPathCalculatable;
import jsettlers.algorithms.path.Path;
import jsettlers.algorithms.path.dijkstra.DijkstraAlgorithm.DijkstraContinuableRequest;
import jsettlers.common.CommonConstants;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.buildings.IBuildingOccupyer;
import jsettlers.common.buildings.OccupyerPlace;
import jsettlers.common.buildings.OccupyerPlace.ESoldierType;
import jsettlers.common.map.shapes.FreeMapArea;
import jsettlers.common.map.shapes.MapCircle;
import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.mapobject.IAttackableTowerMapObject;
import jsettlers.common.material.ESearchType;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.movable.IMovable;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.graphics.messages.SimpleMessage;
import jsettlers.logic.buildings.Building;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.interfaces.IAttackable;
import jsettlers.logic.movable.interfaces.IAttackableMovable;
import jsettlers.logic.objects.StandardMapObject;
import jsettlers.logic.player.Player;
import jsettlers.network.synchronic.random.RandomSingleton;

/**
 * This is a tower building that can request soldiers and let them defend the tower.
 * 
 * @author Andreas Eberle
 * 
 */
public class OccupyingBuilding extends Building implements IBuilding.IOccupyed, IPathCalculatable, IOccupyableBuilding, Serializable {
	private static final long serialVersionUID = 5267249978497095473L;

	private static final int TIMER_PERIOD = 500;

	private final LinkedList<TowerOccupier> occupiers;
	private final LinkedList<ESearchType> searchedSoldiers = new LinkedList<ESearchType>();
	private final LinkedList<OccupyerPlace> emptyPlaces = new LinkedList<OccupyerPlace>();

	private DijkstraContinuableRequest request;

	private boolean occupiedArea;
	private float doorHealth = 1.0f;
	private boolean inFight = false;
	private AttackableTowerMapObject attackableTowerObject = null;

	private final int[] maximumRequestedSoldiers = new int[ESoldierType.values().length];
	private final int[] currentlyCommingSoldiers = new int[ESoldierType.values().length];

	public OccupyingBuilding(EBuildingType type, Player player) {
		super(type, player);

		this.occupiers = new LinkedList<TowerOccupier>(); // for testing purposes

		initSoldierRequests();
	}

	private void initSoldierRequests() {
		final OccupyerPlace[] occupyerPlaces = super.getBuildingType().getOccupyerPlaces();
		if (occupyerPlaces.length > 0) {
			for (OccupyerPlace currPlace : occupyerPlaces) {
				requestSoldierForPlace(currPlace);
			}
		}
	}

	private void requestSoldierForPlace(OccupyerPlace currPlace) {
		emptyPlaces.add(currPlace);
		searchedSoldiers.add(currPlace.getType() == ESoldierType.INFANTRY ? ESearchType.SOLDIER_SWORDSMAN : ESearchType.SOLDIER_BOWMAN);
	}

	@Override
	protected final int constructionFinishedEvent() {
		setAttackableTowerObject(true);
		return TIMER_PERIOD + RandomSingleton.getInt(0, 200); // adding random prevents simultaneous scan after map creation
	}

	private void setAttackableTowerObject(boolean set) {
		if (set) {
			attackableTowerObject = new AttackableTowerMapObject();
			super.getGrid().getMapObjectsManager().addAttackableTowerObject(getDoor(), attackableTowerObject);
		} else {
			super.getGrid().getMapObjectsManager().removeMapObjectType(getDoor().x, getDoor().y, EMapObjectType.ATTACKABLE_TOWER);
			attackableTowerObject = null;
		}
	}

	void changePlayerTo(ShortPoint2D attackerPos) {
		assert occupiers.isEmpty() : "there cannot be any occupiers in the tower when changing the player.";

		Movable enemy = super.getGrid().getMovable(attackerPos);
		Player newPlayer = enemy.getPlayer();

		setAttackableTowerObject(false);
		super.placeFlag(false);

		resetSoldierSearch();

		super.setPlayer(newPlayer);

		if (occupiedArea) { // free the area if it had been occupied.
			ShortPoint2D pos = super.getPos();
			FreeMapArea protectedArea = new FreeMapArea(pos, getBuildingType().getProtectedTiles());
			super.getGrid().changePlayerOfTower(pos, newPlayer, protectedArea);
		} else {
			occupyAreaIfNeeded();
		}

		initSoldierRequests();
		searchedSoldiers.remove(ESearchType.SOLDIER_SWORDSMAN);
		enemy.setOccupyableBuilding(this);
		currentlyCommingSoldiers[ESoldierType.INFANTRY.ordinal()]++;

		doorHealth = 0.1f;
		inFight = false;

		super.placeFlag(true);
		setAttackableTowerObject(true);
	}

	private void resetSoldierSearch() {
		request = null;
		searchedSoldiers.clear();
		emptyPlaces.clear();
		for (int i = 0; i < currentlyCommingSoldiers.length; i++) {
			currentlyCommingSoldiers[i] = 0;
		}
	}

	@Override
	protected final void appearedEvent() {
		occupyAreaIfNeeded();
		searchedSoldiers.remove(ESearchType.SOLDIER_SWORDSMAN);
		currentlyCommingSoldiers[ESoldierType.INFANTRY.ordinal()]++;
	}

	@Override
	protected final EMapObjectType getFlagType() {
		return EMapObjectType.FLAG_DOOR;
	}

	@Override
	protected final int subTimerEvent() {
		if (doorHealth < 1 && !inFight) {
			doorHealth = Math.min(1, doorHealth + Constants.TOWER_DOOR_REGENERATION);
		}

		if (!searchedSoldiers.isEmpty()) {
			if (request == null) {
				request = new DijkstraContinuableRequest(this, super.getPos().x, super.getPos().y, (short) 1, Constants.TOWER_SEARCH_RADIUS);
			}
			request.setSearchType(searchedSoldiers.peek());

			Path path = super.getGrid().getDijkstra().find(request);
			if (path != null) {
				Movable soldier = super.getGrid().getMovable(path.getTargetPos());
				if (soldier != null && soldier.setOccupyableBuilding(this)) {
					ESearchType searchedSoldier = searchedSoldiers.pop();
					currentlyCommingSoldiers[getSoldierType(searchedSoldier).ordinal()]++;
				}// soldier wasn't at the position or wasn't able to take the job to go to this building
			}
		}
		return TIMER_PERIOD;
	}

	private ESoldierType getSoldierType(ESearchType searchedSoldier) {
		switch (searchedSoldier) {
		case SOLDIER_BOWMAN:
			return ESoldierType.BOWMAN;
		case SOLDIER_PIKEMAN:
		case SOLDIER_SWORDSMAN:
			return ESoldierType.INFANTRY;
		default:
			throw new IllegalArgumentException(searchedSoldier + " is no soldier search type!");
		}
	}

	@Override
	protected void positionedEvent(ShortPoint2D pos) {
	}

	@Override
	protected final void killedEvent() {
		setSelected(false);

		if (occupiedArea) {
			freeArea();

			int idx = 0;
			FreeMapArea buildingArea = super.getBuildingArea();
			for (TowerOccupier curr : occupiers) {
				addInformableMapObject(curr, false);// if curr is a bowman, this removes the informable map object.

				curr.getSoldier().leaveOccupyableBuilding(buildingArea.get(idx));
				idx++;
			}

			occupiers.clear();
		}

		setAttackableTowerObject(false);
	}

	private void freeArea() {
		super.getGrid().freeAreaOccupiedByTower(super.getPos());
	}

	@Override
	public final List<? extends IBuildingOccupyer> getOccupyers() {
		return occupiers;
	}

	@Override
	public final boolean needsPlayersGround() { // soldiers don't need players ground.
		return false;
	}

	@Override
	public final OccupyerPlace addSoldier(IBuildingOccupyableMovable soldier) {
		OccupyerPlace freePosition = findFreePositionFor(soldier.getSoldierType());

		currentlyCommingSoldiers[freePosition.getType().ordinal()]--;
		emptyPlaces.remove(freePosition);

		TowerOccupier towerOccupier = new TowerOccupier(freePosition, soldier);
		occupiers.add(towerOccupier);

		occupyAreaIfNeeded();

		soldier.setSelected(super.isSelected());

		addInformableMapObject(towerOccupier, true);

		return freePosition;
	}

	@Override
	public void removeSoldier(IBuildingOccupyableMovable soldier) {
		TowerOccupier occupier = null;
		for (TowerOccupier currOccupier : occupiers) {
			if (currOccupier.soldier == soldier) {
				occupier = currOccupier;
				break;
			}
		}

		// if the soldier is not in the tower, just return
		if (occupier == null) {
			return;
		}

		// remove the soldier and request a new one
		occupiers.remove(occupier);
		requestSoldierForPlace(occupier.place);
	}

	protected TowerOccupier removeSoldier() {
		TowerOccupier removedSoldier = occupiers.removeFirst();

		addInformableMapObject(removedSoldier, false);

		return removedSoldier;
	}

	/**
	 * Adds or removes the informable map object for the given soldier.
	 * 
	 * @param soldier
	 * @param add
	 *            if true, the object is added<br>
	 *            if false, the object is removed.
	 */
	private void addInformableMapObject(TowerOccupier soldier, boolean add) {
		if (soldier.place.getType() == ESoldierType.BOWMAN) {
			ShortPoint2D position = getTowerBowmanSearchPosition(soldier.place);

			if (add) {
				super.getGrid().getMapObjectsManager().addInformableMapObjectAt(position, soldier.getSoldier().getMovable());
			} else {
				super.getGrid().getMapObjectsManager().removeMapObjectType(position.x, position.y, EMapObjectType.INFORMABLE_MAP_OBJECT);
			}
		}
	}

	@Override
	public ShortPoint2D getTowerBowmanSearchPosition(OccupyerPlace place) {
		ShortPoint2D pos = place.getPosition().calculatePoint(super.getPos());
		// FIXME @Andreas Eberle introduce new field in the buildings xml file
		ShortPoint2D position = new ShortPoint2D(pos.x + 3, pos.y + 6);
		return position;
	}

	private OccupyerPlace findFreePositionFor(ESoldierType soldierType) {
		OccupyerPlace freePosition = null;
		for (OccupyerPlace curr : emptyPlaces) {
			if (curr.getType() == soldierType) {
				freePosition = curr;

				break;
			}
		}
		return freePosition;
	}

	private final void occupyAreaIfNeeded() {
		if (!occupiedArea) {
			MapCircle occupying = new MapCircle(super.getPos(), CommonConstants.TOWER_RADIUS);
			super.getGrid().occupyAreaByTower(super.getPlayer(), occupying);
			occupiedArea = true;
		}
	}

	@Override
	public final void requestFailed(EMovableType soldierType) {
		ESearchType searchType = getSearchType(soldierType);

		currentlyCommingSoldiers[getSoldierType(searchType).ordinal()]--;

		if (searchType != null)
			searchedSoldiers.add(searchType);
	}

	@Override
	public final ShortPoint2D getPosition(IBuildingOccupyableMovable soldier) {
		for (TowerOccupier curr : occupiers) {
			if (curr.getSoldier() == soldier) {
				return curr.place.getPosition().calculatePoint(super.getPos());
			}
		}
		return null;
	}

	@Override
	public final void setSelected(boolean selected) {
		super.setSelected(selected);
		for (TowerOccupier curr : occupiers) {
			curr.getSoldier().setSelected(selected);
		}

		if (attackableTowerObject != null && attackableTowerObject.currDefender != null) {
			attackableTowerObject.currDefender.getSoldier().setSelected(selected);
		}
	}

	private final ESearchType getSearchType(EMovableType movableType) {
		ESearchType searchType;

		switch (movableType) {
		case BOWMAN_L1:
		case BOWMAN_L2:
		case BOWMAN_L3:
			searchType = ESearchType.SOLDIER_BOWMAN;
			break;
		case SWORDSMAN_L1:
		case SWORDSMAN_L2:
		case SWORDSMAN_L3:
			searchType = ESearchType.SOLDIER_SWORDSMAN;
			break;
		case PIKEMAN_L1:
		case PIKEMAN_L2:
		case PIKEMAN_L3:
			searchType = ESearchType.SOLDIER_PIKEMAN;
			break;
		default:
			return null;
		}
		return searchType;
	}

	@Override
	public final boolean isOccupied() {
		return !occupiers.isEmpty();
	}

	@Override
	public void towerDefended(IBuildingOccupyableMovable soldier) {
		inFight = false;
		if (attackableTowerObject.currDefender == null) {
			System.err.println("ERROR: WHAT? No defender in a defended tower!");
		} else {
			occupiers.add(new TowerOccupier(attackableTowerObject.currDefender.place, soldier));
			attackableTowerObject.currDefender = null;
		}
		doorHealth = 0.1f;
	}

	@Override
	public int getMaximumRequestedSoldiers(ESoldierType type) {
		return maximumRequestedSoldiers[type.ordinal()];
	}

	@Override
	public void setMaximumRequestedSoldiers(ESoldierType type, int max) {
		final OccupyerPlace[] occupyerPlaces = super.getBuildingType().getOccupyerPlaces();
		int physicalMax = 0;
		for (OccupyerPlace place : occupyerPlaces) {
			if (place.getType() == type) {
				physicalMax++;
			}
		}
		if (max > physicalMax) {
			maximumRequestedSoldiers[type.ordinal()] = physicalMax;
		} else if (max <= 0) {
			maximumRequestedSoldiers[type.ordinal()] = 0;
			/* There must always be someone in a tower, or at least requested. */
			/*
			 * NOTE: We might skip this, and instead let the last soldier not leave the tower if the tower would become empty afterwards
			 */
			boolean isEmpty = true;
			for (int count : maximumRequestedSoldiers) {
				isEmpty &= count == 0;
			}
			if (isEmpty && physicalMax >= 1) {
				maximumRequestedSoldiers[type.ordinal()] = 1;
			}
		} else {
			maximumRequestedSoldiers[type.ordinal()] = max;
		}
	}

	@Override
	public int getCurrentlyCommingSoldiers(ESoldierType type) {
		return currentlyCommingSoldiers[type.ordinal()];
	}

	private final static class TowerOccupier implements IBuildingOccupyer, Serializable {
		private static final long serialVersionUID = -1491427078923346232L;

		final OccupyerPlace place;
		final IBuildingOccupyableMovable soldier;

		TowerOccupier(OccupyerPlace place, IBuildingOccupyableMovable soldier) {
			this.place = place;
			this.soldier = soldier;
		}

		@Override
		public OccupyerPlace getPlace() {
			return place;
		}

		@Override
		public IMovable getMovable() {
			return soldier.getMovable();
		}

		public IBuildingOccupyableMovable getSoldier() {
			return soldier;
		}

	}

	/**
	 * This map object lies at the door of a tower and is used to signal soldiers that there is something to attack.
	 * 
	 * @author Andreas Eberle
	 * 
	 */
	public class AttackableTowerMapObject extends StandardMapObject implements IAttackable, IAttackableTowerMapObject {
		private static final long serialVersionUID = -5137593316096740750L;
		private TowerOccupier currDefender;

		public AttackableTowerMapObject() {
			super(EMapObjectType.ATTACKABLE_TOWER, false, OccupyingBuilding.this.getPlayerId());
		}

		@Override
		public ShortPoint2D getPos() {
			return OccupyingBuilding.this.getDoor();
		}

		@Override
		public void receiveHit(float strength, ShortPoint2D attackerPos, byte attackingPlayer) {
			Movable attacker = getGrid().getMovable(attackerPos);
			if (attacker != null && attacker.getPlayer() == getPlayer()) {
				return; // this can happen directly after the tower changed its player
			}

			if (doorHealth > 0) {
				doorHealth -= strength / Constants.DOOR_HIT_RESISTENCY_FACTOR;

				if (doorHealth <= 0) {
					doorHealth = 0;
					inFight = true;

					OccupyingBuilding.this.getGrid().getMapObjectsManager()
							.addSelfDeletingMapObject(getPos(), EMapObjectType.GHOST, Constants.GHOST_PLAY_DURATION, getPlayer());

					pollNewDefender(attackerPos);
				}
			} else if (currDefender != null) {
				IAttackableMovable movable = currDefender.getSoldier().getMovable();
				movable.receiveHit(strength, attackerPos, attackingPlayer);

				if (movable.getHealth() <= 0) {
					emptyPlaces.add(currDefender.place); // request a new soldier.
					searchedSoldiers.add(getSearchType(currDefender.getSoldier().getMovableType()));

					pollNewDefender(attackerPos);
				}
			}

			OccupyingBuilding.this.getPlayer().showMessage(SimpleMessage.attacked(attackingPlayer, attackerPos));
		}

		private void pollNewDefender(ShortPoint2D attackerPos) {
			if (occupiers.isEmpty()) {
				currDefender = null;
				changePlayerTo(attackerPos);
			} else {
				currDefender = removeSoldier();
				currDefender.getSoldier().setDefendingAt(getPos());
			}
		}

		@Override
		public float getHealth() {
			if (doorHealth > 0) {
				return doorHealth;
			} else {
				return currDefender == null ? 0 : currDefender.getMovable().getHealth();
			}
		}

		@Override
		public boolean isAttackable() {
			return true;
		}

		@Override
		public IMovable getMovable() {
			return currDefender == null ? null : currDefender.getSoldier().getMovable();
		}

		@Override
		public EMovableType getMovableType() {
			assert false : "This should never have been called";
			return EMovableType.SWORDSMAN_L1;
		}

		@Override
		public void informAboutAttackable(IAttackable attackable) {
		}
	}

}
