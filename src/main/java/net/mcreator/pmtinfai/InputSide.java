package net.mcreator.pmtinfai;

import net.minecraft.util.IStringSerializable;

public enum InputSide implements IStringSerializable {
   WEST("west"),
   EAST("east"),
   NORTH("north"),
   SOUTH("south"),
   NONE("none");
   

   private final String name;

   private InputSide(String name) {
      this.name = name;
   }

   public String toString() {
      return this.getName();
   }

   public String getName() {
      return this.name;
   }
   public boolean isActive() {
      return this != NONE;
   }
}