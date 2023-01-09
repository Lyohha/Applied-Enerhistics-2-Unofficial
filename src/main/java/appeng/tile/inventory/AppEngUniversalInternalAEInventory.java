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
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.iterators.AEInvIterator;
import appeng.util.iterators.InvIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import java.util.Iterator;


public class AppEngUniversalInternalAEInventory extends AppEngInternalAEInventory
{

	private ConfigManager cm;

	public AppEngUniversalInternalAEInventory(final IAEAppEngInventory te, final int s )
	{
		super(te, s);
		this.cm = null;
	}

	public AppEngUniversalInternalAEInventory(final IAEAppEngInventory te, final int s, ConfigManager cm )
	{
		super(te, s);
		this.cm = cm;
	}

	private ItemStack getFluidStack(FluidStack fluid) {

		return fluid == null ? null : new ItemStack(AEApi.instance().definitions().items().getFluid().maybeItem().get(), 1, fluid.getFluid().getID());
	}


	@Override
	public void setInventorySlotContents( final int slot, ItemStack newItemStack )
	{
		if(newItemStack != null && this.cm != null && this.cm.getSetting( Settings.INTERFACE_FLUID_MODE ) == YesNo.YES && Platform.isServer()) {
			if(newItemStack.getItem() instanceof IFluidContainerItem) {
				if(((IFluidContainerItem) newItemStack.getItem()).getCapacity(newItemStack) > 0) {
					FluidStack fluid = ((IFluidContainerItem) newItemStack.getItem()).drain(newItemStack, 1, false);
					ItemStack fluoidStack = this.getFluidStack(fluid);
					if(fluoidStack != null) {
						super.setInventorySlotContents(slot, fluoidStack);
						return;
					}
				}
			}
		}

		super.setInventorySlotContents(slot, newItemStack);
	}
}
