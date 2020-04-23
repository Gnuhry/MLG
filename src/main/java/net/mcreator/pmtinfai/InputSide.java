package net.mcreator.pmtinfai;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Direction;

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
   public static InputSide GetEnum(Direction d){
   		if (d == Direction.EAST)
			return InputSide.EAST;
		if (d == Direction.WEST)
			return InputSide.WEST;
		if (d == Direction.NORTH)
			return InputSide.NORTH;
		if (d == Direction.SOUTH)
			return InputSide.SOUTH;
		return InputSide.NONE;

   }
}