package net.mcreator.pmtinfai.enums;

import net.minecraft.util.IStringSerializable;

public enum FFSpecies implements IStringSerializable {
    // Enum
    RS("rs_ff"), P_RS("gated_rs_ff"), HF_RS("rising_rs_ff"), LF_RS("falling_rs_ff"), MS_RS("master_slave_rs_ff"), JK("jk_ff"), P_JK(
            "gated_jk_ff"), HF_JK("rising_jk_ff"), LF_JK("falling_jk_ff"), MS_JK("master_slave_jk_ff"), D("d_ff"), P_D("gated_d_ff"), HF_D(
            "rising_d_ff"), LF_D("falling_d_ff"), MS_D("master_slave_f_ff"), T(
            "t_ff"), P_T("gated_t_ff"), HF_T("rising_t_ff"), LF_T("falling_t_ff"), MS_T("master_slave_t_ff"), NONE("none");
    // S | R
    // 0 | 0
    // 0 | 1
    // 1 | 0
    // 1 | 1
    // Q=save, F=false, D=toggle, T=true
    private final char[] TableRS = new char[]{'Q', 'T', 'F', 'F'};
    private final char[] TableJK = new char[]{'Q', 'T', 'F', 'D'};
    private final char[] TableD = new char[]{'F', 'F', 'T', 'T'};
    private final char[] TableT = new char[]{'Q', 'Q', 'D', 'D'};
    // Variablen
    private final String name;

    /**
     * Konstruktor
     */
    FFSpecies(String name) {
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
    public boolean isActive() {
        return this != NONE;
    }

    /**
     * Wandelt die Direction in das Enum um
     *
     * @param d Direction zum Umwandeln
     * @return zugeh�riges Enum
     */
    public static FFSpecies GetEnum(String d) {
        if (d.equals("rs_ff"))
            return FFSpecies.RS;
        if (d.equals("gated_rs_ff"))
            return FFSpecies.P_RS;
        if (d.equals("rising_rs_ff"))
            return FFSpecies.HF_RS;
        if (d.equals("falling_rs_ff"))
            return FFSpecies.LF_RS;
        if (d.equals("master_slace_rs_ff"))
            return FFSpecies.MS_RS;
        if (d.equals("jk_ff"))
            return FFSpecies.JK;
        if (d.equals("gated_jk_ff"))
            return FFSpecies.P_JK;
        if (d.equals("rising_jk_ff"))
            return FFSpecies.HF_JK;
        if (d.equals("falling_jk_ff"))
            return FFSpecies.LF_JK;
        if (d.equals("master_slace_jk_ff"))
            return FFSpecies.MS_JK;
        if (d.equals("d_ff"))
            return FFSpecies.D;
        if (d.equals("gated_d_ff"))
            return FFSpecies.P_D;
        if (d.equals("rising_d_ff"))
            return FFSpecies.HF_D;
        if (d.equals("falling_d_ff"))
            return FFSpecies.LF_D;
        if (d.equals("master_slave_d_ff"))
            return FFSpecies.MS_D;
        if (d.equals("t_ff"))
            return FFSpecies.T;
        if (d.equals("gated_t_ff"))
            return FFSpecies.P_D;
        if (d.equals("rising_t_ff"))
            return FFSpecies.HF_T;
        if (d.equals("falling_t_ff"))
            return FFSpecies.LF_T;
        if (d.equals("master_slave_t_ff"))
            return FFSpecies.MS_T;
        return FFSpecies.NONE;
    }

    public char[] GetTable() {
        if (this == RS || this == P_RS || this == HF_RS || this == LF_RS || this == MS_RS)
            return TableRS;
        if (this == JK || this == P_JK || this == HF_JK || this == LF_JK || this == MS_JK)
            return TableJK;
        if (this == D || this == P_D || this == HF_D || this == LF_D || this == MS_D)
            return TableD;
        if (this == T || this == P_T || this == HF_T || this == LF_T || this == MS_T)
            return TableT;
        return null;
    }

    public int GetClockMode() {// 0-no Clock, 1-pegel, 2-High, 3- LOW, 4-MasterSlave
        if (this == RS || this == JK || this == D || this == T)
            return 0;
        if (this == P_RS || this == P_JK || this == P_D || this == P_T)
            return 1;
        if (this == HF_RS || this == HF_JK || this == HF_D || this == HF_T)
            return 2;
        if (this == LF_RS || this == LF_JK || this == LF_D || this == LF_T)
            return 3;
        if (this == MS_RS || this == MS_JK || this == MS_D || this == MS_T)
            return 4;
        return 0;
    }
}