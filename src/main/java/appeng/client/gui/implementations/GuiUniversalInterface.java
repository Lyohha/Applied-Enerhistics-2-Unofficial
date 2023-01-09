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

package appeng.client.gui.implementations;


import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.container.implementations.ContainerUniversalInterface;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IUniversalInterfaceHost;
import appeng.helpers.UniversalInterface;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;


public class GuiUniversalInterface extends GuiUpgradeable
{

	private GuiTabButton priority;
	private GuiImgButton BlockMode;
	private GuiToggleButton interfaceMode;
	private GuiToggleButton fluidMode;
	private final UniversalInterface myDuality;

	public GuiUniversalInterface(final InventoryPlayer inventoryPlayer, final IUniversalInterfaceHost te )
	{
		super( new ContainerUniversalInterface( inventoryPlayer, te ) );
		this.myDuality = te.getInterfaceDuality();
		this.ySize = 211;
	}

	@Override
	protected void addButtons()
	{
		this.priority = new GuiTabButton( this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender );
		this.buttonList.add( this.priority );

		this.BlockMode = new GuiImgButton( this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO );
		this.buttonList.add( this.BlockMode );

		this.interfaceMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 26, 84, 85, GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal() );
		this.buttonList.add( this.interfaceMode );

		this.fluidMode = new GuiToggleButton( this.guiLeft - 18, this.guiTop + 44, 86, 87, GuiText.FluidMode.getLocal(), GuiText.FluidModeHint.getLocal() );
		this.buttonList.add( this.fluidMode );
	}

	@Override
	public void drawFG( final int offsetX, final int offsetY, final int mouseX, final int mouseY )
	{
		if( this.BlockMode != null )
		{
			this.BlockMode.set( ( (ContainerUniversalInterface) this.cvb ).getBlockingMode() );
		}

		if( this.interfaceMode != null )
		{
			this.interfaceMode.setState( ( (ContainerUniversalInterface) this.cvb ).getInterfaceTerminalMode() == YesNo.YES );
		}

		if( this.fluidMode != null )
		{
			this.fluidMode.setState( ( (ContainerUniversalInterface) this.cvb ).getFluidMode() == YesNo.YES );
		}

		this.fontRendererObj.drawString( this.getGuiDisplayName( GuiText.Interface.getLocal() ), 8, 6, 4210752 );

		this.fontRendererObj.drawString( GuiText.Config.getLocal(), 18, 6 + 11 + 7, 4210752 );
		this.fontRendererObj.drawString( GuiText.StoredItems.getLocal(), 18, 6 + 60 + 7, 4210752 );
		this.fontRendererObj.drawString( GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752 );

		this.fontRendererObj.drawString( GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752 );

//		FluidTankInfo[] info = myDuality.getFluidStorage().getTankInfo(ForgeDirection.UNKNOWN);
//
//		for(int i = 0; i < info.length; i++) {
//			drawFluid(info[i].fluid, 8 + 18 * i, 35 + 18);
//		}
	}

	private void drawFluid(FluidStack fluidStack, int x, int y) {
		if(fluidStack == null)
			return;

		IIcon icon = fluidStack.getFluid().getIcon();
		if (icon == null)
			return;

		mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

		int color = fluidStack.getFluid().getColor();
		GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));
		GL11.glEnable(GL11.GL_BLEND);

		double minU = icon.getMinU();
		double maxU = icon.getMaxU();
		double minV = icon.getMinV();
		double maxV = icon.getMaxV();

		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + 16, 0, minU, maxV);
		tessellator.addVertexWithUV(x + 16, y + 16, 0, maxU, maxV);
		tessellator.addVertexWithUV(x + 16, y, 0, maxU, minV);
		tessellator.addVertexWithUV(x, y, 0, minU, minV);
		tessellator.draw();
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	protected String getBackground()
	{
		return "guis/interface.png";
	}

	@Override
	protected void actionPerformed( final GuiButton btn )
	{
		super.actionPerformed( btn );

		final boolean backwards = Mouse.isButtonDown( 1 );

		if( btn == this.priority )
		{
			NetworkHandler.instance.sendToServer( new PacketSwitchGuis( GuiBridge.GUI_PRIORITY ) );
		}

		if( btn == this.interfaceMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.INTERFACE_TERMINAL, backwards ) );
		}

		if( btn == this.fluidMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.INTERFACE_FLUID_MODE, backwards ) );
		}

		if( btn == this.BlockMode )
		{
			NetworkHandler.instance.sendToServer( new PacketConfigButton( Settings.INTERFACE_FLUID_MODE, backwards ) );
		}
	}
}
