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

package appeng.client.render.items;


import appeng.api.util.AEColor;
import appeng.client.texture.ExtraItemTextures;
import appeng.items.misc.ItemPaintBall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;


public class ItemFluidRender implements IItemRenderer
{
	private Minecraft mc;

	public ItemFluidRender() {
		this.mc = Minecraft.getMinecraft();
	}

	@Override
	public boolean handleRenderType( final ItemStack item, final ItemRenderType type )
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper( final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper )
	{
		return helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION;
	}

	@Override
	public void renderItem( final ItemRenderType type, final ItemStack item, final Object... data )
	{
		IIcon par2Icon;

		Fluid aFluid = FluidRegistry.getFluid(item.getItemDamage());

		par2Icon = aFluid.getIcon();

		if(par2Icon == null)
			par2Icon = FluidRegistry.WATER.getIcon();


		final float f4 = par2Icon.getMinU();
		final float f5 = par2Icon.getMaxU();
		final float f6 = par2Icon.getMinV();
		final float f7 = par2Icon.getMaxV();

		int color = aFluid.getColor();

		mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

		final Tessellator tessellator = Tessellator.instance;

		GL11.glPushMatrix();

		GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));
		GL11.glEnable(GL11.GL_BLEND);

		if( type == ItemRenderType.INVENTORY )
		{
			GL11.glScalef( 16F, 16F, 10F );
			GL11.glTranslatef( 0.0F, 1.0F, 0.0F );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
			GL11.glEnable( GL11.GL_ALPHA_TEST );

			tessellator.startDrawingQuads();
			tessellator.setNormal( 0.0F, 1.0F, 0.0F );
			tessellator.addVertexWithUV( 0, 0, 0, f4, f7 );
			tessellator.addVertexWithUV( 1, 0, 0, f5, f7 );
			tessellator.addVertexWithUV( 1, 1, 0, f5, f6 );
			tessellator.addVertexWithUV( 0, 1, 0, f4, f6 );
			tessellator.draw();
		}
		else
		{
			if( type == ItemRenderType.EQUIPPED_FIRST_PERSON )
			{
				GL11.glTranslatef( 0.0F, 0.0F, 0.0F );
			}
			else
			{
				GL11.glTranslatef( -0.5F, -0.3F, 0.01F );
			}
			final float f12 = 0.0625F;
			ItemRenderer.renderItemIn2D( tessellator, f5, f6, f4, f7, par2Icon.getIconWidth(), par2Icon.getIconHeight(), f12 );

			GL11.glDisable( GL11.GL_CULL_FACE );
			GL11.glColor4f( 1, 1, 1, 1.0F );
			GL11.glScalef( 1F, 1.1F, 1F );
			GL11.glTranslatef( 0.0F, 1.07F, f12 / -2.0f );
			GL11.glRotatef( 180F, 1.0F, 0.0F, 0.0F );
		}

		GL11.glColor4f( 1, 1, 1, 1.0F );

		if( type == ItemRenderType.INVENTORY )
		{
			GL11.glDisable( GL11.GL_ALPHA_TEST );
		}
		else
		{
			GL11.glEnable( GL11.GL_CULL_FACE );
		}

		GL11.glPopMatrix();
	}
}
