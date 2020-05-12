package net.mcreator.pmtinfai;

import net.mcreator.pmtinfai.enums.LogicKinds;

public class Recipe_MKLG {

    public static int[] CheckRecipe(LogicKinds kind) {
        if (kind==LogicKinds.AndGate)
            return new int[]{12, 16, 1, 8};
        else if (kind==LogicKinds.OrGate)
            return new int[]{12, 16, 1, 6};
        else if (kind==LogicKinds.XorGate)
            return new int[]{12, 16, 2, 8};
        else if (kind==LogicKinds.NotGate)
            return new int[]{12, 16, 1, 6};
        else if (kind==LogicKinds.NandGate)
            return new int[]{12, 16, 1, 9};
        else if (kind==LogicKinds.NorGate)
            return new int[]{12, 16, 1, 7};
        else if (kind==LogicKinds.XnorGate)
            return new int[]{12, 16, 2, 10};
        else if (kind==LogicKinds.RS_FF)
            return new int[]{20, 20, 4, 7};
        else if (kind==LogicKinds.RS_FF_H)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.RS_FF_L)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.RS_FF_P)
            return new int[]{24, 24, 4, 7};
        else if (kind==LogicKinds.RS_FF_MS)
            return new int[]{28, 28, 5, 9};
        else if (kind==LogicKinds.D_FF)
            return new int[]{20, 20, 4, 7};
        else if (kind==LogicKinds.D_FF_H)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.D_FF_L)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.D_FF_P)
            return new int[]{24, 24, 4, 7};
        else if (kind==LogicKinds.D_FF_MS)
            return new int[]{28, 28, 5, 9};
        else if (kind==LogicKinds.T_FF)
            return new int[]{20, 20, 4, 7};
        else if (kind==LogicKinds.T_FF_H)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.T_FF_L)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.T_FF_P)
            return new int[]{24, 24, 4, 7};
        else if (kind==LogicKinds.T_FF_MS)
            return new int[]{28, 28, 5, 9};
        else if (kind==LogicKinds.JK_FF)
            return new int[]{20, 20, 4, 7};
        else if (kind==LogicKinds.JK_FF_H)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.JK_FF_L)
            return new int[]{24, 24, 4, 8};
        else if (kind==LogicKinds.JK_FF_P)
            return new int[]{24, 24, 4, 7};
        else if (kind==LogicKinds.JK_FF_MS)
            return new int[]{28, 28, 5, 9};
        return new int[]{0, 0, 0, 0};
    }
}
