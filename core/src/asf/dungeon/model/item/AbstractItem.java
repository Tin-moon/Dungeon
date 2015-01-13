package asf.dungeon.model.item;

import asf.dungeon.model.token.Token;

/**
 * Created by Danny on 11/17/2014.
 */
public abstract class AbstractItem implements Item{

        public String name;
        public String description;

        public String vagueName, vagueDescription;

        @Override
        public String getName() {
                return name;
        }

        @Override
        public String getDescription() {
                return description;
        }

        @Override
        public String getVagueName() { return vagueName; }

        @Override
        public String getVagueDescription() { return vagueDescription; }

        @Override
        public String getNameFromJournal(Token token) {
                if (isIdentified(token)) return getName();
                return getVagueName();
        }

        @Override
        public String getDescriptionFromJournal(Token token) {
                if (isIdentified(token)) return getDescription();
                return getVagueDescription();
        }

}
