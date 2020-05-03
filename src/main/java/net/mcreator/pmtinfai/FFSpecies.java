package net.mcreator.pmtinfai;

import net.minecraft.util.IStringSerializable;
import net.minecraft.world.biome.WarmOceanBiome;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;

public enum FFSpecies implements IStringSerializable {
	// Enum
	RS("RS-FF"), P_RS("pegel RS-FF"), HF_RS("high_flank RS-FF"), LF_RS("low_flank RS-FF"), MS_RS("master_slave RS-FF"),
	JK("JK-FF"), P_JK("pegel JK-FF"), HF_JK("high_flank JK-FF"), LF_JK("low_flank JK-FF"), MS_JK("master_slave JK-FF"),
	D("D-FF"), P_D("pegel D-FF"), HF_D("high_flank D-FF"), LF_D("low_flank D-FF"), MS_D("master_slave D-FF"),
	T("T-FF"), P_T("pegel T-FF"), HF_T("high_flank T-FF"), LF_T("low_flank T-FF"), MS_T("master_slave T-FF"),
	NONE("none");
	// S | R
	// 0 | 0
	// 0 | 1
	// 1 | 0
	// 1 | 1
	//Q=save, F=false, D=toggle, T=true
	private final char[] TableRS=new String[]{"Q","T","F","F"}
	private final char[] TableJK=new String[]{"Q","T","F","D"}
	private final char[] TableD=new String[]{"F","F","T","T"}
	private final char[] TableT=new String[]{"Q","Q","D","D"}
	// Variablen
	private final String name;
	/**
	 * Konstruktor
	 */
	private FFSpecies(String name) {
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
	 * Gibt den Namen zurück
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
	 * @param d
	 *            Direction zum Umwandeln
	 * @return zugehöriges Enum
	 */
	public static FFSpecies GetEnum(String d) {
		if (d.equals(FFSpecies.RS))
			return FFSpecies.RS;
		if (d.equals(FFSpecies.P_RS))
			return FFSpecies.P_RS;
		return LogicSpecies.NONE;
	}

	public char[] GetTable(){
		if(this==RS||this==P_RS||this==HF_RS||this==LF_RS||this==MS_RS)
			return TableRS;
		if(this==JK||this==P_JK||this==HF_JK||this==LF_JK||this==MS_JK)
			return TableJK;
		if(this==D||this==P_D||this==HF_D||this==LF_D||this==MS_D)
			return TableD;
		if(this==T||this==P_T||this==HF_T||this==LF_T||this==MS_T)
			return TableT;
		return null;
	}

	public int GetClockMode(){// 0-no Clock, 1-pegel, 2-High, 3- LOW, 4-MasterSlave
		if(this==RS||this==JK||this==D||this==T)
			return 0;
		if(this==P_RS||this==P_JK||this==P_D||this==P_T)
			return 1;
		if(this==HF_RS||this==HF_JK||this==HF_D||this==HF_T)
			return 2;
		if(this==LF_RS||this==LF_JK||this==LF_D||this==LF_T)
			return 3;
		if(this==MS_RS||this==MS_JK||this==MS_D||this==MS_T)
			return 4;
		return 0;
	}

	
}
