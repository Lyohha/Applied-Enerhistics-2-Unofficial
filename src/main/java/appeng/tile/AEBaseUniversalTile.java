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

package appeng.tile;

import appeng.core.AELog;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.IAEAppEngFluidHandler;
import appeng.tile.inventory.IAEUniversalFluidHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public abstract class AEBaseUniversalTile extends AEBaseInvTile implements IAEAppEngFluidHandler, IFluidHandler
{

	@TileEvent( TileEventType.UNIVERSAL_NBT_READ )
	public boolean readFromNBT_AEBaseUniversalTile( final NBTTagCompound data )
	{
		NBTTagCompound target = data.getCompoundTag("storage");
		if(target != null)
			getInternalFluidHandler().readFluidFromNBT(target);
		return true;
	}

	@TileEvent( TileEventType.UNIVERSAL_NBT_WRITE )
	public void writeToNBT_AEBaseUniversalTile( final NBTTagCompound data )
	{
		NBTTagCompound target = new NBTTagCompound();
		getInternalFluidHandler().writeFluidToNBT(target);
		data.setTag("storage", target);
	}

	@Override
	public void markFluidUpdate() {
		if(worldObj != null && !getWorldObj().isRemote)
			getWorldObj().markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	// IFluidHandler implementations
	public abstract IAEUniversalFluidHandler getInternalFluidHandler();

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return getInternalFluidHandler().fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return getInternalFluidHandler().drain(from, resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return getInternalFluidHandler().drain(from, maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return getInternalFluidHandler().canFill(from, fluid);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return getInternalFluidHandler().canDrain(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return getInternalFluidHandler().getTankInfo(from);
	}
}
