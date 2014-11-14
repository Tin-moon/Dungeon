package asf.dungeon.model.token;

import asf.dungeon.model.Direction;
import asf.dungeon.model.Item;
import com.badlogic.gdx.utils.Array;

/**
 * Created by Danny on 11/11/2014.
 */
public class Inventory implements TokenComponent {
        private final Token token;
        private final Array<Item> inventory = new Array<Item>(true, 16, Item.class);
        private Item.Consumable consumeItem;                               // item to be  consumed on next update


        public Inventory(Token token, Item... items) {
                this.token = token;
                this.inventory.addAll(items);
        }

        public Item getItem() {
                if (inventory.size > 0)
                        return inventory.get(0);
                return null;
        }

        public Item getItem(int index) {
                if (inventory.size > index)
                        return inventory.get(index);
                return null;
        }

        public int size() {
                return inventory.size;
        }

        public boolean useItem(Item item) {
                if (consumeItem != null || token.getDamage().isDead())
                        return false; // already consuming an item

                if (item instanceof Item.Consumable) {
                        consumeItem = (Item.Consumable) item;
                        return true;
                }

                return false;

        }

        public void addItem(Item item) {
                inventory.add(item);
                QuickSlot quickSlot = token.get(QuickSlot.class);
                if(quickSlot != null){
                        if(quickSlot.getItem() == null){
                                // if the quickslot has nothing in it, then put the newly picked up item there...
                                // TODO: may want to have further qualifiers so that only relevant items will be automatically quickslotted
                                quickSlot.setItem(item);
                        }
                }
                if (token.listener != null)
                        token.listener.onInventoryAdd(item);
        }

        public boolean discardItem(Item item) {
                if (token.getDamage().isDead()) {
                        return false;
                }
                boolean valid = inventory.removeValue(item, true);
                if (valid) {
                        QuickSlot quickSlot = token.get(QuickSlot.class);
                        if (quickSlot != null) {
                                if (quickSlot.getItem() == item ){
                                        // change the quick slot to another item that .equals it (eg to another health potion) otherwise set to null
                                        quickSlot.setItem(null);
                                        for (Item i : inventory) {
                                                if(i.equals(item)){
                                                        quickSlot.setItem(i);
                                                        break;
                                                }
                                        }
                                }

                        }

                        if (token.listener != null)
                                token.listener.onInventoryRemove(item);
                } else
                        throw new AssertionError(token.getName() + " is not carrying " + item + " and can not discard it");

                return valid;
        }

        /**
         * the "quantity" of the item is done be doing a .equals against all items in the inventory
         * items are considered equal if all their effects are the same (as determined by the item itself)
         *
         * items that .equals eachother should appear stacked in the inventory screen or similiar.
         * @param item
         * @return
         */
        public int getQuantity(Item item) {
                int count = 0;
                for (Item i : inventory) {
                        if (item.equals(i))
                                count++;
                }
                return count;
        }

        /**
         * do not modify! use addItem() and discardItem(), this is required to properly announce this change to the listener
         *
         * @return
         */
        public Array<Item> getInventory() {
                return inventory;
        }

        @Override
        public boolean teleportToLocation(int x, int y, Direction direction) {
                return true;
        }

        @Override
        public boolean update(float delta) {
                if (consumeItem != null) { // if an item was marked for consumption by useItem() then do so now
                        consumeItem.consume(token);
                        if (token.listener != null)
                                token.listener.onConsumeItem(consumeItem);
                        discardItem(consumeItem);
                        consumeItem = null;
                }
                return false;
        }
}
