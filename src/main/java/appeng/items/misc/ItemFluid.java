package appeng.items.misc;

import appeng.client.render.items.ItemFluidRender;
import appeng.client.render.items.PaintBallRender;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.EnumSet;
import java.util.List;

public class ItemFluid extends AEBaseItem {

    public ItemFluid() {
        this.setMaxStackSize( 1 );
        this.setHasSubtypes( true );
        this.setFeature( EnumSet.of( AEFeature.Core ) );

        if( Platform.isClient() )
        {
            MinecraftForgeClient.registerItemRenderer( this, new ItemFluidRender() );
        }
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister aIconRegister) {

    }

    @Override
    protected void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
    {
        NBTTagCompound data = stack.getTagCompound();

        if(data != null) {
            long tToolTipAmount = data.getLong("mFluidDisplayAmount");
            if (tToolTipAmount > 0L) {
                lines.add(EnumChatFormatting.GRAY + "Amount: " + tToolTipAmount + EnumChatFormatting.GRAY);
            }
        }
    }

    public String getUnlocalizedName(ItemStack aStack) {
        if (aStack != null) {
            return this.getFluidName(FluidRegistry.getFluid(aStack.getItemDamage()), false);
        }
        return "";
    }

    public String getItemStackDisplayName(ItemStack aStack) {
        if (aStack != null) {
            return this.getFluidName(FluidRegistry.getFluid(aStack.getItemDamage()), true);
        }
        return "";
    }

    private String getFluidName(Fluid aFluid, boolean aLocalized) {
        if(aFluid == null)
            return "";

        return aLocalized ? aFluid.getLocalizedName(new FluidStack(aFluid, 0)) : aFluid.getUnlocalizedName();
    }

    @Override
    protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
    {
        int i = 0;
        for (int j = FluidRegistry.getMaxID(); i < j; i++) {
            ItemStack tStack = this.getStackFromFluid(FluidRegistry.getFluid(i));
            if (tStack != null) {
                itemStacks.add(tStack);
            }
        }
    }

    public ItemStack getStackFromFluid(Fluid aFluid) {

        if(aFluid == null)
            return null;

        ItemStack stack = new ItemStack(this);

        stack.setItemDamage(aFluid.getID());

        return stack;
    }
}
