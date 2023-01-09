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

package appeng.integration.modules.waila.tile;


import appeng.api.networking.energy.IAEPowerStorage;
import appeng.core.localization.WailaText;
import appeng.helpers.IUniversalInterfaceHost;
import appeng.helpers.UniversalInterface;
import appeng.integration.modules.waila.BaseWailaDataProvider;
import appeng.util.Platform;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.List;


/**
 * Power storage provider for WAILA
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class InterfaceWailaDataProvider extends BaseWailaDataProvider
{
	/**
	 * Key used for the transferred {@link NBTTagCompound}
	 */
	private static final String ID_FLUIDS = "currentFluids";

	/**
	 * Adds the current and max power to the tool tip
	 * Will ignore if the tile has an energy buffer ( &gt; 0 )
	 *
	 * @param itemStack      stack of power storage
	 * @param currentToolTip current tool tip
	 * @param accessor       wrapper for various world information
	 * @param config         config to react to various settings
	 * @return modified tool tip
	 */
	@Override
	public List<String> getWailaBody( final ItemStack itemStack, final List<String> currentToolTip, final IWailaDataAccessor accessor, final IWailaConfigHandler config )
	{

		final TileEntity te = accessor.getTileEntity();
		if( te instanceof IUniversalInterfaceHost )
		{
			final IUniversalInterfaceHost storage = (IUniversalInterfaceHost) te;

			final NBTTagCompound tag = accessor.getNBTData();

			if( tag.hasKey( ID_FLUIDS ) )
			{
				NBTTagCompound fluids = tag.getCompoundTag(ID_FLUIDS);

				for(int i = 0; i < UniversalInterface.NUMBER_OF_STORAGE_SLOTS; i++) {
					if(fluids.hasKey("#" + i)) {
						NBTTagCompound c = fluids.getCompoundTag("#" + i);

						FluidStack fluid = FluidStack.loadFluidStackFromNBT(c);

						currentToolTip.add(fluid.getLocalizedName() + " " + fluid.amount + " mb");
					}

				}
			}
		}

		return currentToolTip;
	}

	/**
	 * Called on server to transfer information from server to client.
	 * <p/>
	 * If the {@link TileEntity} is a {@link IAEPowerStorage}, it
	 * writes the power information to the {@code #tag} using the {@code #ID_CURRENT_POWER} key.
	 *
	 * @param player player looking at the power storage
	 * @param te     power storage
	 * @param tag    transferred tag which is send to the client
	 * @param world  world of the power storage
	 * @param x      x pos of the power storage
	 * @param y      y pos of the power storage
	 * @param z      z pos of the power storage
	 * @return tag send to the client
	 */
	@Override
	public NBTTagCompound getNBTData( final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag, final World world, final int x, final int y, final int z )
	{
		if( te instanceof IUniversalInterfaceHost)
		{
			NBTTagCompound target = new NBTTagCompound();
			final IUniversalInterfaceHost universalInterfaceHost = (IUniversalInterfaceHost) te;

			IFluidHandler handler = universalInterfaceHost.getInterfaceDuality().getFluidStorage();

			FluidTankInfo[] info =  handler.getTankInfo(ForgeDirection.UNKNOWN);

			for(int i = 0; i < UniversalInterface.NUMBER_OF_STORAGE_SLOTS; i++) {
				if(info[i].fluid != null) {
					NBTTagCompound c = new NBTTagCompound();
					info[i].fluid.writeToNBT(c);
					target.setTag("#" + i, c);
				}
			}

			tag.setTag(ID_FLUIDS, target);
		}

		return tag;
	}

}
