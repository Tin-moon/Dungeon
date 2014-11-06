package asf.dungeon.model;

import asf.dungeon.model.factory.FloorMapGenerator;
import com.badlogic.gdx.Gdx;
import asf.dungeon.model.logic.LocalPlayerLogicProvider;
import asf.dungeon.model.logic.LogicProvider;

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

        private final Listener listener;

        public Dungeon(Listener listener, MasterJournal masterJournal,FloorMapGenerator floorMapFactory) {
                this.listener = listener;
                this.masterJournal = masterJournal;
                this.floorMapFactory = floorMapFactory;

        }

        public MasterJournal getMasterJournal(){
                return masterJournal;
        }

        public void update(float delta) {

                currentFloorMap.update(delta);

        }

        public FloorMap generateFloor(int floorIndex){

                FloorMap floorMap = floorMaps.get(floorIndex);
                if(floorMap == null){
                        Gdx.app.log("Dungeon","generateFloor: "+floorIndex);
                        floorMap = floorMapFactory.generate(this, floorIndex);
                        floorMaps.put(floorIndex, floorMap);
                }
                return floorMap;
        }

        public void setCurrentFloor(FloorMap newFloor) {
                if(newFloor == currentFloorMap){
                        return;
                }
                Gdx.app.log("Dungeon","setCurrentFloor: "+newFloor.index);
                FloorMap oldFloorMap = currentFloorMap;
                currentFloorMap = newFloor;

                if(oldFloorMap != null){
                        for (int i = 0; i < oldFloorMap.tokens.size; i++) {
                                listener.onTokenRemoved(oldFloorMap.tokens.items[i]);
                        }
                }

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

        public CharacterToken newCharacterToken(FloorMap fm, String name, ModelId modelId, LogicProvider logicProvider) {
                CharacterToken t = new CharacterToken(this,fm, nextTokenId++, name, modelId);
                t.setLogicProvider(logicProvider);
                fm.tokens.add(t);
                if(currentFloorMap == fm)
                        listener.onTokenAdded(t);
                return t;
        }

        public CrateToken newCrateToken(FloorMap fm, String name, ModelId modelId, Item item) {
                CrateToken t = new CrateToken(this, fm, nextTokenId++, name, modelId, item );
                fm.tokens.add(t);
                if(currentFloorMap == fm)
                        listener.onTokenAdded(t);
                return t;
        }

        public LootToken newLootToken(FloorMap fm, Item item, int x, int y) {
                LootToken t = new LootToken(this, fm,nextTokenId++, item);
                fm.tokens.add(t);
                t.teleportToLocation( x, y);
                if(currentFloorMap == fm)
                        listener.onTokenAdded(t);
                return t;
        }

        public Token removeToken(Token token) {
                for (FloorMap floorMap : floorMaps.values()) {
                        boolean b = floorMap.tokens.removeValue(token, true);
                        if (b) {
                                if(token.getFloorMap() == currentFloorMap)
                                        listener.onTokenRemoved(token);
                                return token;
                        }
                }

                return null;
        }


        protected void moveTokenToFloor(Token token, FloorMap newFloorMap) {
                for (FloorMap oldFloorMap : floorMaps.values()) {
                        boolean b = oldFloorMap.tokens.removeValue(token, true);
                        if (b) {
                                newFloorMap.tokens.add(token);
                                if(oldFloorMap == currentFloorMap){
                                        listener.onTokenRemoved(token);
                                }else if(newFloorMap == currentFloorMap){
                                        listener.onTokenAdded(token);
                                }
                                return;
                        }
                }
                throw new IllegalStateException("This token is not on any floor");
        }

        public Token getToken(int playerId) {
                for (FloorMap floorMap : floorMaps.values()) {
                        for (Token token : floorMap.tokens) {
                                if (token instanceof CharacterToken) {
                                        CharacterToken character = (CharacterToken) token;
                                        if (character.getLogicProvider() instanceof LocalPlayerLogicProvider) {
                                                LocalPlayerLogicProvider local = (LocalPlayerLogicProvider) character.getLogicProvider();
                                                if (local.getId() == playerId) {
                                                        return token;
                                                }
                                        }
                                }
                        }
                }
                return null;
        }


        public interface Listener {
                public void onTokenAdded(Token token);

                public void onTokenRemoved(Token token);

        }


}