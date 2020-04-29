package net.mcreator.pmtinfai.procedures;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.Entity;

import net.mcreator.pmtinfai.PMTINFAIElements;

import java.util.function.Supplier;
import java.util.Map;
import net.minecraft.item.Item;
import com.sun.corba.se.spi.extension.ZeroPortPolicy;
import com.sun.corba.se.spi.orbutil.fsm.InputImpl;
import net.minecraft.block.BlockState;
import java.util.ArrayList;
import java.util.List;

@PMTINFAIElements.ModElement.Tag
public class ChangeItemProcedure extends PMTINFAIElements.ModElement {
	public ChangeItemProcedure(PMTINFAIElements instance) {
		super(instance, 7);
	}

	public static void executeProcedure(java.util.HashMap<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			System.err.println("Failed to load dependency entity for procedure ChangeItem!");
			return;
		}
		if (dependencies.get("x") == null) {
			System.err.println("Failed to load dependency x for procedure ChangeItem!");
			return;
		}
		if (dependencies.get("y") == null) {
			System.err.println("Failed to load dependency y for procedure ChangeItem!");
			return;
		}
		if (dependencies.get("z") == null) {
			System.err.println("Failed to load dependency z for procedure ChangeItem!");
			return;
		}
		if (dependencies.get("world") == null) {
			System.err.println("Failed to load dependency world for procedure ChangeItem!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		int x = (int) dependencies.get("x");
		int y = (int) dependencies.get("y");
		int z = (int) dependencies.get("z");
		World world = (World) dependencies.get("world");
		
		class GetItemStack{
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}
		
		GetItemStack help=new GetItemStack();
		Item zero=help.getItemStack(0).getItem();
		Item one=help.getItemStack(1).getItem();
		Item two=help.getItemStack(2).getItem();
		Item three=help.getItemStack(3).getItem();
		Item InputItem=Items.REDSTONE;
		Item OutputItem=Items.REDSTONE_TORCH;
		
		BlockPos pos=new BlockPos((int) x, (int) y, (int) z);
		List<Direction> directions_input=new ArrayList<>();
		List<Direction> directions_output=new ArrayList<>();
		if (zero == InputItem) {
				directions_input.add(Direction.WEST);
				
		}
		else if (zero == OutputItem) {
			directions_output.add(Direction.WEST);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		if (one == InputItem) {
				directions_input.add(Direction.NORTH);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		else if (one == OutputItem) {
			directions_output.add(Direction.NORTH);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		if (two == InputItem) {
				directions_input.add(Direction.EAST);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		else if (two == OutputItem) {
			directions_output.add(Direction.EAST);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		if (three == InputItem) {
				directions_input.add(Direction.SOUTH);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		else if (three == OutputItem) {
			directions_output.add(Direction.SOUTH);
				//((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).addInput(Direction.WEST, pos, world);
		}
		if(directions_output.size()==1&&directions_input.size()>0&&directions_input.size()<4){
			Direction[]d_help=new Direction[directions_input.size()];
			for(int f=0;f<d_help.length;f++)
				d_help[f]=directions_input.get(f);
			((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).setPort(d_help,directions_output.get(0),world,pos);
		}
		else{
			((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(pos).getBlock()).clearInput(pos, world);
		}
		
		/*else if(new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(0).getItem() != new ItemStack(Items.REDSTONE,1).getItem()) {
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).removeInput(Direction.WEST, new BlockPos((int) x, (int) y, (int) z), world);	
	}*/
	/*if (new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(1).getItem() == new ItemStack(Items.REDSTONE,1).getItem()) {
			//world.notifyNeighborsOfStateChange(new BlockPos((int) x, (int) y, (int) z),
			//		world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock());
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).addInput(Direction.NORTH, new BlockPos((int) x, (int) y, (int) z), world);
		}
		else if(new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(1).getItem() != new ItemStack(Items.REDSTONE,1).getItem()) {
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).removeInput(Direction.NORTH, new BlockPos((int) x, (int) y, (int) z), world);	
	}
	if (new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(2).getItem() == new ItemStack(Items.REDSTONE,1).getItem()) {
			//world.notifyNeighborsOfStateChange(new BlockPos((int) x, (int) y, (int) z),
			//		world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock());
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).addInput(Direction.EAST, new BlockPos((int) x, (int) y, (int) z), world);
		}
		else if(new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(2).getItem() != new ItemStack(Items.REDSTONE,1).getItem()) {
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).removeInput(Direction.EAST, new BlockPos((int) x, (int) y, (int) z), world);	
	}if (new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(3).getItem() == new ItemStack(Items.REDSTONE,1).getItem()) {
			//world.notifyNeighborsOfStateChange(new BlockPos((int) x, (int) y, (int) z),
			//		world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock());
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).addInput(Direction.SOUTH, new BlockPos((int) x, (int) y, (int) z), world);
		}
		else if(new Object() {
			public ItemStack getItemStack(int sltid) {
				if (entity instanceof ServerPlayerEntity) {
					Container _current = ((ServerPlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							return ((Slot) ((Map) invobj).get(sltid)).getStack();
						}
					}
				}
				return ItemStack.EMPTY;
			}
		}.getItemStack(3).getItem() != new ItemStack(Items.REDSTONE,1).getItem()) {
					((net.mcreator.pmtinfai.LogicBlock)world.getBlockState(new BlockPos((int) x, (int) y, (int) z)).getBlock()).removeInput(Direction.SOUTH, new BlockPos((int) x, (int) y, (int) z), world);	
	}*/
}
}
