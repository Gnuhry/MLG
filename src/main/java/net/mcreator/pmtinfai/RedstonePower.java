package net.mcreator.pmtinfai;

import net.minecraft.util.IStringSerializable;

public enum RedstonePower implements IStringSerializable {
  ZERO("0"),
   ONE("1"),
   TWO("2"),
   THREE("3"),
   FOUR("4"),
   FIVE("5"),
   SIX("6"),
   SEVEN("7"),
   EIGHT("8"),
   NINE("9"),
   TEN("10"),
   ELEVEN("11"),
   TWELVE("12"),
   THIRTEEN("13"),
   FOURTEEN("14"),
   FIFTEEN("15");


   private final String name;

   private RedstonePower(String name) {
      this.name = name;
   }

   public String toString() {
      return this.getName();
   }

   public String getName() {
      return this.name;
   }
   public boolean isActive() {
      return this != ZERO;
   }
}
