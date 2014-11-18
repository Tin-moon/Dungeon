package asf.dungeon.model.floorgen;

import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.FullAgroLogic;

/**
 * Created by Danny on 11/4/2014.
 */
public class BalanceTestFloorGen implements FloorMapGenerator, FloorMap.MonsterSpawner{

        public FloorMap generate(Dungeon dungeon, int floorIndex){
                String[] tileData = new String[]{
                        "-------------------",
                        "|.................|",
                        "|.^...............|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|.................|",
                        "|-----------------|"

                };

                FloorMap floorMap = new FloorMap(floorIndex, PreBuiltFloorGen.convertTileData(floorIndex, tileData), this);
                //UtFloorGen.spawnCharacters(dungeon, floorMap);
                //UtFloorGen.spawnRandomCrates(dungeon, floorMap);
                return floorMap;
        }


        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if(countTeam1 == 0){
                        int x, y;
                        do{
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        }while(floorMap.getTile(x,y) == null || !floorMap.getTile(x,y).isFloor() || floorMap.hasTokensAt(x,y));

                        Token token = dungeon.newCharacterToken(floorMap, "Monster",
                                ModelId.Berzerker,
                                new FullAgroLogic(1),
                                new Experience(1, 4, 9, 6,1),
                                x,y);

                        //EquipmentItem sword = EquipmentItem.makeWeapon("Sword", 1);
                        //token.getInventory().add(sword);
                        //token.getInventory().equip(sword);

                }
        }
}