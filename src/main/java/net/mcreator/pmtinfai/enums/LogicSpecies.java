package net.mcreator.pmtinfai.enums;

import net.minecraft.util.IStringSerializable;
import net.minecraft.client.resources.I18n;

public enum LogicSpecies implements IStringSerializable {
    // Enum
    AND("and"), OR("or"), NOT("not"), NOR("nor"), NAND("nand"), XOR("xor"), XNOR("xnor"), NONE("none"), CUSTOM("custom");
    private static final String[] help = new String[]{"(A&(B&C)),(A&B),F", "(A|(B|C)),(A|B),F", "F,F,(!A)", "(!(A|(B|C))),(!(A|B)),F",
            "(!(A&(B&C))),(!(A&B)),F", "((A&(B&C))|((A&((!B)&(!C)))|(((!A)&(B&(!C)))|((!A)&((!B)&C))))),(((!A)&B)|((!B)&A)),F",
            "(!((A&(B&C))|((A&((!B)&(!C)))|(((!A)&(B&(!C)))|((!A)&((!B)&C)))))),(!(((!A)&B)|((!B)&A))),F"};
    // Variablen
    private final String name;

    /**
     * Konstruktor
     */
    LogicSpecies(String name) {
        this.name = name;
    }

    /**
     * Wandelt das Enum in einen String
     *
     * @return Name des Enums
     */
    public String toString() {
        return this.getName();
    }

    /**
     * Gibt den Namen zur�ck
     *
     * @return Name des Enums
     */
    public String getName() {
        return this.name;
    }

    /**
     * Abfrage ob Enum Aktiv ist
     *
     * @return Aktiv wenn die InputSeite existiert
     */

    public String getTranslationName() {
        if(this==AND){
            return I18n.format("enum.pmtinfai.and");
        }
        if(this==OR){
            return I18n.format("enum.pmtinfai.or");
        }
        if(this==NOT){
            return I18n.format("enum.pmtinfai.not");
        }
        if(this==NOR){
            return I18n.format("enum.pmtinfai.nor");
        }
        if(this==NAND){
            return I18n.format("enum.pmtinfai.nand");
        }
        if(this==XOR){
            return I18n.format("enum.pmtinfai.xor");
        }
        if(this==XNOR){
            return I18n.format("enum.pmtinfai.xnor");
        }
        if(this==NONE){
            return I18n.format("enum.pmtinfai.none");
        }
        if(this==CUSTOM){
            return I18n.format("enum.pmtinfai.custom");
        }
        return this.getName();
    }

    public boolean isActive() {
        return this != NONE;
    }

    /**
     * Wandelt die Direction in das Enum um
     *
     * @param d Direction zum Umwandeln
     * @return zugeh�riges Enum
     */
    public static LogicSpecies GetEnum(String d) {
        if (d.equals(help[0]))
            return LogicSpecies.AND;
        if (d.equals(help[1]))
            return LogicSpecies.OR;
        if (d.equals(help[2]))
            return LogicSpecies.NOT;
        if (d.equals(help[3]))
            return LogicSpecies.NOR;
        if (d.equals(help[4]))
            return LogicSpecies.NAND;
        if (d.equals(help[5]))
            return LogicSpecies.XOR;
        if (d.equals(help[6]))
            return LogicSpecies.XNOR;
        if (!d.equals(null) && !d.equals("none"))
            return LogicSpecies.CUSTOM;
        return LogicSpecies.NONE;
    }
}
