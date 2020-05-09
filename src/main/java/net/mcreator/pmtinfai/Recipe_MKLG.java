package net.mcreator.pmtinfai;

import net.mcreator.pmtinfai.enums.LogicKinds;

public class Recipe_MKLG {

    public static int[] CheckRecipe(LogicKinds kind) {
        if (kind==LogicKinds.AndGate)
            return new int[]{10, 10, 10, 10};
        else if (kind==LogicKinds.OrGate)
            return new int[]{10, 10, 10, 10};
        return new int[]{0, 0, 0, 0};
    }
}
