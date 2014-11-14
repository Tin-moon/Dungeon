package asf.dungeon.model;

import asf.dungeon.model.factory.FloorMapGenerator;
import asf.dungeon.model.token.Attack;
import asf.dungeon.model.token.Damage;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Inventory;
import asf.dungeon.model.token.Loot;
import asf.dungeon.model.token.Move;
import asf.dungeon.model.token.StatusEffects;
import asf.dungeon.model.token.Target;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.LocalPlayerLogicProvider;
import asf.dungeon.model.token.logic.LogicProvider;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by danny on 10/22/14.
 */
public class Dungeon {
        private final MasterJournal masterJournal;
        private final FloorMapGenerator floorMapFactory;
        private final Map<Integer, FloorMap> floorMaps = new HashMap<Integer, FloorMap>(16);
        private FloorMap currentFloorMap;
        private int nextTokenId = 0;

        private transient Listener listener;

        public Dungeon(MasterJournal masterJournal, FloorMapGenerator floorMapFactory) {
                this.masterJournal = masterJournal;
                this.floorMapFactory = floorMapFactory;
        }


        public MasterJournal getMasterJournal() {
                return masterJournal;
        }

        public Token getLocalPlayerToken() {
                for (FloorMap floorMap : floorMaps.values()) {
                        for (Token token : floorMap.tokens) {
                                if (token.get(LocalPlayerLogicProvider.class) != null) {
                                        return token;
                                }
                        }
                }
                return null;
        }

        public void update(float delta) {

                currentFloorMap.update(delta);

        }

        public FloorMap generateFloor(int floorIndex) {

                FloorMap floorMap = floorMaps.get(floorIndex);
                if (floorMap == null) {
                        Gdx.app.log("Dungeon", "generateFloor: " + floorIndex);

                        floorMap = floorMapFactory.generate(this, floorIndex);

                        floorMaps.put(floorIndex, floorMap);
                }
                return floorMap;
        }


        public void setCurrentFloor(int floorIndex) {
                FloorMap newFloor = generateFloor(floorIndex);
                if (newFloor == currentFloorMap) {
                        return;
                }
                Gdx.app.log("Dungeon", "setCurrentFloor: " + newFloor.index);
                FloorMap oldFloorMap = currentFloorMap;
                currentFloorMap = newFloor;

                if (listener == null)
                        return;
                listener.onFloorMapChanged(currentFloorMap);

                if (oldFloorMap != null) {
                        for (int i = 0; i < oldFloorMap.tokens.size; i++) {
                                listener.onTokenRemoved(oldFloorMap.tokens.items[i]);
                        }
                }

                for (int i = 0; i < currentFloorMap.tokens.size; i++) {
                        listener.onTokenAdded(currentFloorMap.tokens.items[i]);
                }

        }

        public void setListener(Listener listener) {
                this.listener = listener;
                if (listener == null || currentFloorMap == null)
                        return;

                listener.onFloorMapChanged(currentFloorMap);

                for (int i = 0; i < currentFloorMap.tokens.size; i++) {
                        listener.onTokenAdded(currentFloorMap.tokens.items[i]);
                }
        }

        public FloorMap getCurrentFloopMap() {
                return currentFloorMap;
        }

        public FloorMap getFloorMap(int floorIndex) {
                return floorMaps.get(floorIndex);
        }

        public Token newCharacterToken(FloorMap fm, String name, ModelId modelId, LogicProvider logicProvider) {
                Token t = new Token(this, fm, nextTokenId++, name, modelId);
                t.add(logicProvider);
                t.add(new Experience(t));
                t.add(new Inventory(t));
                t.add(new StatusEffects(t));
                t.add(new Target(t));
                t.add(new Attack(t));
                t.add(new Damage(t, 10));
                t.add(new Move(t));

                t.getDamage().setDeathDuration(3f);
                t.getDamage().setDeathRemovalCountdown(10f);
                fm.tokens.add(t);
                if (currentFloorMap == fm && listener != null)
                        listener.onTokenAdded(t);
                return t;
        }

        public Token newCrateToken(FloorMap fm, String name, ModelId modelId, Item item) {
                Token t = new Token(this, fm, nextTokenId++, name, modelId);
                t.add(new Inventory(t, item));
                t.add(new Damage(t, 1));
                t.getDamage().setDeathDuration(2.5f);
                t.getDamage().setDeathRemovalCountdown(.25f);
                fm.tokens.add(t);
                if (currentFloorMap == fm && listener != null)
                        listener.onTokenAdded(t);
                return t;
        }

        public Token newLootToken(FloorMap fm, Item item, int x, int y) {
                Token t = new Token(this, fm, nextTokenId++, item.getName(), item.getModelId());
                t.add(new Loot(t, item));
                fm.tokens.add(t);
                t.teleportToLocation(x, y);
                if (currentFloorMap == fm && listener != null)
                        listener.onTokenAdded(t);
                return t;
        }

        public Token removeToken(Token token) {
                for (FloorMap floorMap : floorMaps.values()) {
                        boolean b = floorMap.tokens.removeValue(token, true);
                        if (b) {
                                if (token.getFloorMap() == currentFloorMap && listener != null)
                                        listener.onTokenRemoved(token);
                                return token;
                        }
                }

                return null;
        }


        /**
         * should only be called by Token
         *
         * @param token
         * @param newFloorMap
         */
        public void moveTokenToFloor(Token token, FloorMap newFloorMap) {
                for (FloorMap oldFloorMap : floorMaps.values()) {
                        boolean b = oldFloorMap.tokens.removeValue(token, true);
                        if (b) {
                                newFloorMap.tokens.add(token);
                                if (listener != null)
                                        if (oldFloorMap == currentFloorMap) {
                                                listener.onTokenRemoved(token);
                                        } else if (newFloorMap == currentFloorMap) {
                                                listener.onTokenAdded(token);
                                        }
                                return;
                        }
                }
                throw new IllegalStateException("This token is not on any floor");
        }


        public interface Listener {
                public void onFloorMapChanged(FloorMap newFloorMap);

                public void onTokenAdded(Token token);

                public void onTokenRemoved(Token token);

        }


}
