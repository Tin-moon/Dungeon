package asf.dungeon.model.floorgen.room;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Dungeon;
import asf.dungeon.model.FloorMap;
import asf.dungeon.model.ModelId;
import asf.dungeon.model.Pair;
import asf.dungeon.model.Tile;
import asf.dungeon.model.floorgen.FloorMapGenerator;
import asf.dungeon.model.floorgen.UtFloorGen;
import asf.dungeon.model.item.WeaponItem;
import asf.dungeon.model.token.Experience;
import asf.dungeon.model.token.Token;
import asf.dungeon.model.token.logic.fsm.FsmLogic;
import asf.dungeon.model.token.logic.fsm.Monster;
import com.badlogic.gdx.utils.Array;

/**
 * Creates a floor which is a bunch of square rooms connected at their edges. similiar
 * to how classic zelda dungeons were made.
 * Created by Danny on 11/4/2014.
 */
public class ZeldaGen implements FloorMapGenerator, FloorMap.MonsterSpawner {

        private int minRoomSize = 5;
        private int maxRoomSize = 7;
        private int maxRooms = 8;

        @Override
        public FloorMap generate(Dungeon dungeon, int floorIndex) {
                minRoomSize = 6;
                maxRoomSize = 8;
                maxRooms = 12;
                int roomSize = dungeon.rand.range(minRoomSize, maxRoomSize);
                int numRooms = maxRooms - dungeon.rand.random.nextInt(Math.round(maxRooms * .25f));
                int halfRooms = Math.round(numRooms / 2f);
                Room[][] roomGrid = new Room[halfRooms][halfRooms];
                Array<Room> rooms = new Array<Room>(true, numRooms, Room.class);

                Pair currentGridLoc = new Pair(Math.round(roomGrid.length / 2f), 0);
                Pair nextGridLoc = new Pair();
                // make rooms
                while (rooms.size < numRooms) {
                        if (rooms.size > 0) {
                                int tries = 0;
                                do {
                                        // pick a random valid direction
                                        do {
                                                Direction dir = dungeon.rand.direction8Axis();
                                                nextGridLoc.set(currentGridLoc).addFree(dir);
                                        } while (nextGridLoc.x < 0 || nextGridLoc.x >= roomGrid.length || nextGridLoc.y < 0 || nextGridLoc.y >= roomGrid[0].length);

                                        if(numNeighbors(roomGrid, nextGridLoc.x, nextGridLoc.y) >=2
                                                && dungeon.rand.bool(.75f) && ++tries < 5 ){
                                                // if it will have 2 or more neighbors here then theres a
                                                // 75 percent chance to not place the room here
                                                        continue;
                                        }

                                        currentGridLoc.set(nextGridLoc);
                                } while (roomGrid[nextGridLoc.x][nextGridLoc.y] != null);
                        }
                        int x1 = currentGridLoc.x * roomSize;
                        int y1 = currentGridLoc.y * roomSize;
                        Room newRoom = new Room(x1, y1, x1 + roomSize, y1 + roomSize);
                        rooms.add(newRoom);
                        roomGrid[currentGridLoc.x][currentGridLoc.y] = newRoom;
                }
                Tile[][] tiles = new Tile[(roomSize * halfRooms) + 1][(roomSize * halfRooms) + 1];
                UtRoomCarve.fillAndCarve(dungeon, floorIndex, tiles, rooms);

                FloorMap floorMap = new FloorMap(floorIndex, tiles, this);
                UtRoomSpawn.spawnStairs(dungeon, floorMap, rooms);
                UtRoomSpawn.carveLockedDoorsAndSpawnKeys(dungeon, floorMap, rooms);
                UtFloorGen.spawnCharacters(dungeon, floorMap);
                UtFloorGen.spawnRandomCrates(dungeon, floorMap);

                return floorMap;
        }

        private int numNeighbors(Room[][] roomGrid, int x, int y) {
                int count = 0;
                if (x > 0 && roomGrid[x - 1][y] != null) count++;
                if (x < roomGrid.length - 1 && roomGrid[x + 1][y] != null) count++;
                if (y > 0 && roomGrid[x][y - 1] != null) count++;
                if (y < roomGrid[0].length - 1 && roomGrid[x][y + 1] != null) count++;
                return count;
        }

        @Override
        public void spawnMonsters(Dungeon dungeon, FloorMap floorMap) {
                int countTeam1 = floorMap.getTokensOnTeam(1).size;
                if (countTeam1 < 2) {
                        int x, y;
                        ModelId modelId = dungeon.rand.random.nextBoolean() ? ModelId.Skeleton : ModelId.Skeleton;
                        do {
                                x = dungeon.rand.random.nextInt(floorMap.getWidth());
                                y = dungeon.rand.random.nextInt(floorMap.getHeight());
                        } while (floorMap.getTile(x, y) == null || !floorMap.getTile(x, y).isFloor() || floorMap.hasTokensAt(x, y));

                        Token token = dungeon.newCharacterToken(floorMap, modelId.name(),
                                modelId,
                                new FsmLogic(1, null, Monster.Sleep),
                                new Experience(1, 8, 4, 6, 1, 1),
                                x, y);

                        if (modelId == ModelId.Archer) {
                                WeaponItem weapon = new WeaponItem(dungeon,  2,2,1, true,3,1);
                                token.inventory.add(weapon);
                                token.inventory.equip(weapon);
                        }

                }
        }
}
