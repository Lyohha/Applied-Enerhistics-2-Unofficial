/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.tile.inventory;

import appeng.api.AEApi;
import appeng.api.definitions.IItems;
import appeng.core.AELog;
import appeng.items.misc.ItemFluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;
import org.apache.logging.log4j.Level;

public class AppEngInternalUniversalInventory extends AppEngInternalInventory implements IAEUniversalFluidHandler
{
	public static final int FLUID_TANK_AMOUNT = 8000;

	private final int size;
	private FluidTank[] tanks;
	private IAEAppEngFluidHandler te;

	public AppEngInternalUniversalInventory(final IAEAppEngFluidHandler inventory, final int size )
	{
		super((IAEAppEngInventory) inventory, size);
		this.te = inventory;
		this.size = size;
		this.tanks = new FluidTank[size];
		for(int i = 0; i < size; i++) {
			this.tanks[i] = new FluidTank(FLUID_TANK_AMOUNT);
		}
	}
	@Override
	public void writeToNBT(final NBTTagCompound data, final String name ) {
		final NBTTagCompound target = new NBTTagCompound();
		this.writeFluidToNBT(target);
		super.writeToNBT(target, name);
		data.setTag( name, target );
	}

	@Override
	public void readFromNBT( final NBTTagCompound data, final String name ) {
		final NBTTagCompound target = data.getCompoundTag( name );

		if( target != null ) {
			this.readFluidFromNBT(target);
			super.readFromNBT(target, name);
		}
	}


	@Override
	public void writeFluidToNBT(NBTTagCompound target) {

		for(int i = 0; i < size; i ++) {
			try {
				final NBTTagCompound c = new NBTTagCompound();

				if(this.tanks[i].getFluidAmount() > 0)
					this.tanks[i].writeToNBT(c);

				target.setTag( "#" + i, c );
			}
			catch( final Exception ignored ) { }
		}
	}

	@Override
	public void readFluidFromNBT(NBTTagCompound target) {
		for(int i = 0; i < this.size; i++) {
			try
			{
				final NBTTagCompound c = target.getCompoundTag( "#" + i );

				if( c != null )
				{
					this.tanks[i].readFromNBT(c);
				}
			}
			catch( final Exception e )
			{
				AELog.debug( e );
			}
		}
	}

	/**
	 * Check item for valid for slot.
	 *
	 * Function override for block access to slots what contain fluids
	 *
	 * @param slot slot number
	 * @param itemstack items
	 * @return boolean - is slot valid
	 */
	@Override
	public boolean isItemValidForSlot( final int slot, final ItemStack itemstack )
	{
//		return this.tanks[slot].getFluidAmount() == 0 && super.isItemValidForSlot(slot, itemstack);

		return this.tanks[slot].getFluidAmount() == 0 && !(this.getStackInSlot(slot) != null && this.getStackInSlot(slot).getItem() instanceof ItemFluid) && super.isItemValidForSlot(slot, itemstack);
	}

	@Override
	public ItemStack decrStackSize( final int slot, final int qty )
	{
		// block return item if it is fluid item
		return this.getStackInSlot(slot) != null && this.getStackInSlot(slot).getItem() instanceof ItemFluid ? null : super.decrStackSize(slot, qty);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if (resource == null)
			return 0;

		for(int i = 0; i < this.size; i++) {
			// предметов не должно быть в ячейке кроме фиктивной жидкости
			if(this.getStackInSlot(i) != null && !(this.getStackInSlot(i).getItem() instanceof ItemFluid))
				continue;

			// если есть жидкость, то она должна совпадать
			if(this.tanks[i].getFluid() != null && this.tanks[i].getFluid().getFluid() != resource.getFluid())
				continue;

			// если полный бак, то перейти к следующему
			if(this.tanks[i].getFluidAmount() == FLUID_TANK_AMOUNT)
				continue;

			int filled = this.tanks[i].fill(resource, doFill);

			if(filled > 0) {

				this.setInventorySlotContents(i, this.getFluidStack(this.tanks[i].getFluid(), this.tanks[i].getFluidAmount()));
				this.markDirty();
				return filled;
			}
		}

		return 0;
	}

	private ItemStack getFluidStack(FluidStack fluid, int amount) {

		if(fluid == null)
			return null;

		ItemStack fluidStack = new ItemStack(AEApi.instance().definitions().items().getFluid().maybeItem().get(), 1, fluid.getFluid().getID());

		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("mFluidDisplayAmount", amount);

		fluidStack.setTagCompound(tag);

		return fluidStack;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {

		for(int i = 0; i < this.size; i++) {
			// пропусти пустую жидкость
			if(this.tanks[i].getFluid() == null)
				continue;

			// пропустить жидкость, которая не совпала
			if(this.tanks[i].getFluid().getFluid() != resource.getFluid())
				continue;

			// выкачать жидкость
			FluidStack drain = this.tanks[i].drain(resource.amount, doDrain);

			// если вдруг не вышло выкачать, то продолжить выбор бака
			if(drain == null)
				continue;

			if(this.tanks[i].getFluidAmount() == 0) {
				this.setInventorySlotContents(i, null);
			}

			if(this.tanks[i].getFluidAmount() > 0) {
				this.setInventorySlotContents(i, this.getFluidStack(this.tanks[i].getFluid(), this.tanks[i].getFluidAmount()));
			}

			this.markDirty();

			return drain;
		}

		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {

		for(int i = 0; i < this.size; i++) {
			if(this.tanks[i].getFluid() == null)
				continue;

			FluidStack drain = this.tanks[i].drain(maxDrain, doDrain);

			if(drain == null)
				continue;

			if(this.tanks[i].getFluidAmount() == 0) {
				this.setInventorySlotContents(i, null);
			}

			if(this.tanks[i].getFluidAmount() > 0) {
				this.setInventorySlotContents(i, this.getFluidStack(this.tanks[i].getFluid(), this.tanks[i].getFluidAmount()));
			}

			this.markDirty();

			return drain;
		}

		return null;
	}

	@Override
	public FluidStack drain(int slot, int maxDrain, boolean doDrain) {

		FluidStack stack = this.tanks[slot].drain(maxDrain, doDrain);

		this.setInventorySlotContents(slot, this.getFluidStack(this.tanks[slot].getFluid(), this.tanks[slot].getFluidAmount()));
		this.markDirty();

		return stack;
	}

	@Override
	public int fill(int slot, FluidStack resource, boolean doFill) {
		if (resource == null)
			return 0;

		// предметов не должно быть в ячейке кроме фиктивной жидкости
		if(this.getStackInSlot(slot) != null && !(this.getStackInSlot(slot).getItem() instanceof ItemFluid))
			return 0;

		// если есть жидкость, то она должна совпадать
		if(this.tanks[slot].getFluid() != null && this.tanks[slot].getFluid().getFluid() != resource.getFluid())
			return 0;

		// если полный бак, то перейти к следующему
		if(this.tanks[slot].getFluidAmount() == FLUID_TANK_AMOUNT)
			return 0;

		int filled = this.tanks[slot].fill(resource, doFill);

		if(filled > 0) {

			this.setInventorySlotContents(slot, this.getFluidStack(this.tanks[slot].getFluid(), this.tanks[slot].getFluidAmount()));
			this.markDirty();
			return filled;
		}

		return 0;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {

		for(int i = 0; i < this.size; i++) {
			// если забит слот предметом
			if(this.getStackInSlot(i) != null)
				continue;

			// если есть жидкость, но она не совпадает
			if(this.tanks[i].getFluid() != null && this.tanks[i].getFluid().getFluid() != fluid)
				continue;

			// если полный бак
			if(this.tanks[i].getFluidAmount() == FLUID_TANK_AMOUNT)
				continue;

			// если условия не прошли, значит слот пустой или в нем есть еще место под эту жидкость
			return true;
		}

		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {

		for(int i = 0; i < this.size; i++) {
			// если бак пустой
			if(this.tanks[i].getFluid() == null)
				continue;

			// если жидкость совпала
			if(this.tanks[i].getFluid().getFluid() == fluid)
				return true;
		}

		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {

		FluidTankInfo[] fluidTankInfo = new FluidTankInfo[this.size];

		for(int i = 0; i < this.size; i++) {
			fluidTankInfo[i] = this.tanks[i].getInfo();
		}

		return fluidTankInfo;
	}

	@Override
	public FluidTankInfo getTankInfo(int slot) {

		return this.tanks[slot].getInfo();
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		this.te.markFluidUpdate();
	}

}
