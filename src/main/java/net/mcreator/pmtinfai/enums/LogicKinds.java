package net.mcreator.pmtinfai.enums;

import net.minecraft.nbt.CompoundNBT;

public enum LogicKinds {
    AndGate, OrGate, NotGate, NandGate, NorGate, XorGate, XnorGate, RS_FF, RS_FF_P, RS_FF_H, RS_FF_L, RS_FF_MS, JK_FF, JK_FF_P, JK_FF_H, JK_FF_L, JK_FF_MS, D_FF, D_FF_P, D_FF_H, D_FF_L, D_FF_MS, T_FF, T_FF_P, T_FF_H, T_FF_L, T_FF_MS;

    public static LogicKinds Get(int i) {
        switch (i) {
            case 0:
                return AndGate;
            case 1:
                return OrGate;
            case 2:
                return NotGate;
            case 3:
                return NandGate;
            case 4:
                return NorGate;
            case 5:
                return XorGate;
            case 6:
                return XnorGate;
            case 7:
                return RS_FF;
            case 8:
                return RS_FF_P;
            case 9:
                return D_FF;
            case 10:
                return RS_FF_L;
            case 11:
                return D_FF_L;
            case 12:
                return D_FF_P;
            case 13:
                return T_FF;
            case 14:
                return RS_FF_H;
            case 15:
                return D_FF_H;
            case 16:
                return T_FF_H;
            case 17:
                return T_FF_L;
            case 18:
                return T_FF_P;
            case 19:
                return RS_FF_MS;
            case 20:
                return D_FF_MS;
            case 21:
                return T_FF_MS;
            case 22:
                return JK_FF;
            case 23:
                return JK_FF_H;
            case 24:
                return JK_FF_L;
            case 25:
                return JK_FF_P;
            case 26:
                return JK_FF_MS;
        }
        return null;
    }
    public int Get() {
        if (this == AndGate) return 0;
        if (this == OrGate) return 1;
        if (this == NotGate) return 2;
        if (this == NandGate) return 3;
        if (this == NorGate) return 4;
        if (this == XorGate) return 5;
        if (this == XnorGate) return 6;
        if (this == RS_FF) return 7;
        if (this == RS_FF_P) return 8;
        if (this == D_FF) return 9;
        if (this == RS_FF_L) return 10;
        if (this == D_FF_L) return 11;
        if (this == D_FF_P) return 12;
        if (this == T_FF) return 13;
        if (this == RS_FF_H) return 14;
        if (this == D_FF_H) return 15;
        if (this == T_FF_H) return 16;
        if (this == T_FF_L) return 17;
        if (this == T_FF_P) return 18;
        if (this == RS_FF_MS) return 19;
        if (this == D_FF_MS) return 20;
        if (this == T_FF_MS) return 21;
        if (this == JK_FF) return 22;
        if (this == JK_FF_H) return 23;
        if (this == JK_FF_L) return 24;
        if (this == JK_FF_P) return 25;
        if (this == JK_FF_MS) return 26;
        return 27;
    }

    public CompoundNBT GetNBT() {
        CompoundNBT erg = new CompoundNBT();
        switch (this) {
            case AndGate:
                erg.putString("logic", "(A&(B&C)),(A&B),F");
                erg.putBoolean("logic_", true);
                return erg;
            case OrGate:
                erg.putString("logic", "(A|(B|C)),(A|B),F");
                erg.putBoolean("logic_", true);
                return erg;
            case NotGate:
                erg.putString("logic", "F,F,(!A)");
                erg.putBoolean("logic_", true);
                return erg;
            case NandGate:
                erg.putString("logic", "(!(A&(B&C))),(!(A&B)),F");
                erg.putBoolean("logic_", true);
                return erg;
            case NorGate:
                erg.putString("logic", "(!(A|(B|C))),(!(A|B)),F");
                erg.putBoolean("logic_", true);
                return erg;
            case XorGate:
                erg.putString("logic", "((A&(B&C))|((A&((!B)&(!C)))|(((!A)&(B&(!C)))|((!A)&((!B)&C))))),(((!A)&B)|((!B)&A)),F");
                erg.putBoolean("logic_", true);
                return erg;
            case XnorGate:
                erg.putString("logic", "(!((A&(B&C))|((A&((!B)&(!C)))|(((!A)&(B&(!C)))|((!A)&((!B)&C)))))),(!(((!A)&B)|((!B)&A))),F");
                erg.putBoolean("logic_", true);
                return erg;
            case RS_FF:
                erg.putString("logic", FFSpecies.RS.getName());
                return erg;
            case RS_FF_P:
                erg.putString("logic", FFSpecies.P_RS.getName());
                return erg;
            case RS_FF_H:
                erg.putString("logic", FFSpecies.HF_RS.getName());
                return erg;
            case RS_FF_L:
                erg.putString("logic", FFSpecies.LF_RS.getName());
                return erg;
            case RS_FF_MS:
                erg.putString("logic", FFSpecies.MS_RS.getName());
                return erg;
            case JK_FF:
                erg.putString("logic", FFSpecies.JK.getName());
                return erg;
            case JK_FF_P:
                erg.putString("logic", FFSpecies.P_JK.getName());
                return erg;
            case JK_FF_H:
                erg.putString("logic", FFSpecies.HF_JK.getName());
                return erg;
            case JK_FF_L:
                erg.putString("logic", FFSpecies.LF_JK.getName());
                return erg;
            case JK_FF_MS:
                erg.putString("logic", FFSpecies.MS_JK.getName());
                return erg;
            case D_FF:
                erg.putString("logic", FFSpecies.D.getName());
                return erg;
            case D_FF_P:
                erg.putString("logic", FFSpecies.P_D.getName());
                return erg;
            case D_FF_H:
                erg.putString("logic", FFSpecies.HF_D.getName());
                return erg;
            case D_FF_L:
                erg.putString("logic", FFSpecies.LF_D.getName());
                return erg;
            case D_FF_MS:
                erg.putString("logic", FFSpecies.MS_D.getName());
                return erg;
            case T_FF:
                erg.putString("logic", FFSpecies.T.getName());
                return erg;
            case T_FF_P:
                erg.putString("logic", FFSpecies.P_T.getName());
                return erg;
            case T_FF_H:
                erg.putString("logic", FFSpecies.HF_T.getName());
                return erg;
            case T_FF_L:
                erg.putString("logic", FFSpecies.LF_T.getName());
                return erg;
            case T_FF_MS:
                erg.putString("logic", FFSpecies.MS_T.getName());
                return erg;
        }
        return null;
    }
}
