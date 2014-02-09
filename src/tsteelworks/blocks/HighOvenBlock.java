package tsteelworks.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import tconstruct.blocks.logic.MultiServantLogic;
import tconstruct.library.blocks.InventoryBlock;
import tconstruct.library.util.IFacingLogic;
import tconstruct.library.util.IMasterLogic;
import tconstruct.library.util.IServantLogic;
import tsteelworks.TSteelworks;
import tsteelworks.blocks.logic.HighOvenDrainLogic;
import tsteelworks.blocks.logic.HighOvenLogic;
import tsteelworks.lib.Repo;
import tsteelworks.lib.TSteelworksRegistry;

public class HighOvenBlock extends InventoryBlock
{
    Random rand;
    String texturePrefix = "";

    public HighOvenBlock (int id)
    {
        super(id, Material.rock);
        setHardness(3F);
        setResistance(20F);
        setStepSound(soundMetalFootstep);
        rand = new Random();
        setCreativeTab(TSteelworksRegistry.SteelworksCreativeTab);
        setUnlocalizedName("tsteelworks.HighOven");
    }

    public HighOvenBlock (int id, String prefix)
    {
        this(id);
        texturePrefix = prefix;
    }

    @Override
    public int damageDropped (int meta)
    {
        return meta;
    }

    @Override
    public int quantityDropped (final Random random)
    {
        return 1;
    }

    @Override
    public boolean onBlockActivated (World world, int x, int y, int z, EntityPlayer player, int side, float clickX,
                                     float clickY, float clickZ)
    {
        if (player.isSneaking() || (world.getBlockMetadata(x, y, z) != 0)) return false;
        final Integer integer = getGui(world, x, y, z, player);
        if ((integer == null) || (integer == -1))
            return false;
        else
        {
            player.openGui(getModInstance(), integer, world, x, y, z);
            return true;
        }
    }

    @Override
    public TileEntity createNewTileEntity (World world)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TileEntity createTileEntity (World world, int metadata)
    {
        switch (metadata)
        {
            case 0:
                return new HighOvenLogic();
            case 1:
                return new HighOvenDrainLogic();
        }
        return new MultiServantLogic();
    }

    @Override
    public void onBlockPlacedBy (World world, int x, int y, int z, EntityLivingBase entityliving, ItemStack stack)
    {
        super.onBlockPlacedBy(world, x, y, z, entityliving, stack);
        if (world.getBlockMetadata(x, y, z) == 0)
        {
            onBlockPlacedElsewhere(world, x, y, z, entityliving);
        }
    }

    public void onBlockPlacedElsewhere (World world, int x, int y, int z, EntityLivingBase entityliving)
    {
        final HighOvenLogic logic = (HighOvenLogic) world.getBlockTileEntity(x, y, z);
        logic.checkValidPlacement();
    }

    @Override
    public void getSubBlocks (int id, CreativeTabs tab, List list)
    {
        for (int iter = 0; iter < 12; iter++)
            if (iter != 3)
            {
                list.add(new ItemStack(id, 1, iter));
            }
    }

    @Override
    public void onNeighborBlockChange (World world, int x, int y, int z, int nBlockID)
    {
        final TileEntity logic = world.getBlockTileEntity(x, y, z);
        if (logic instanceof IServantLogic)
        {
            ((IServantLogic) logic).notifyMasterOfChange();
        }
        else
            if (logic instanceof IMasterLogic)
            {
                ((IMasterLogic) logic).notifyChange(null, x, y, z);
            }
    }

    @Override
    public void breakBlock (World world, int x, int y, int z, int blockID, int meta)
    {
        final TileEntity logic = world.getBlockTileEntity(x, y, z);
        if (logic instanceof IServantLogic)
        {
            ((IServantLogic) logic).notifyMasterOfChange();
        }
        super.breakBlock(world, x, y, z, blockID, meta);
    }

    @Override
    public Integer getGui (World world, int x, int y, int z, EntityPlayer entityplayer)
    {
        return TSteelworks.proxy.highOvenGuiID;
    }

    @Override
    public Object getModInstance ()
    {
        return TSteelworks.instance;
    }

    @Override
    public String[] getTextureNames ()
    {
        // TODO: Moar textures
        final String[] textureNames = { "highoven_side", "highoven_inactive", "highoven_active", "drain_side", "drain_out",
            "drain_basin", "scorchedbrick" };
        if (!texturePrefix.equals(""))
        {
            for (int i = 0; i < textureNames.length; i++)
            {
                textureNames[i] = texturePrefix + "_" + textureNames[i];
            }
        }
        return textureNames;
    }

    @Override
    public Icon getIcon (int side, int meta)
    {
        if (meta < 2)
        {
            final int sideTex = side == 3 ? 1 : 0;
            return icons[sideTex + (meta * 3)];
        }
        else
            if (meta == 2) return icons[6];
        //return icons[3 + meta]; 
        return icons[6];
    }

    @Override
    public Icon getBlockTexture (IBlockAccess world, int x, int y, int z, int side)
    {
        final TileEntity logic = world.getBlockTileEntity(x, y, z);
        final short direction = (logic instanceof IFacingLogic) ? ((IFacingLogic) logic).getRenderDirection() : 0;
        final int meta = world.getBlockMetadata(x, y, z);
        if (meta == 0)
        {
            if (side == direction)
            {
                if (isActive(world, x, y, z))
                    return icons[2];
                else
                    return icons[1];
            }
            else
                return icons[0];
        }
        else
            if (meta == 1)
            {
                if (side == direction)
                    return icons[5];
                else
                    if ((side / 2) == (direction / 2))
                        return icons[4];
                    else
                        return icons[3];
            }
            else
                if (meta == 2) return icons[0];
        return icons[3 + meta];
    }

    @Override
    public void registerIcons (IconRegister iconRegister)
    {
        final String[] textureNames = getTextureNames();
        icons = new Icon[textureNames.length];
        for (int i = 0; i < icons.length; ++i)
        {
            icons[i] = iconRegister.registerIcon(Repo.textureDir + textureNames[i]);
        }
    }

    @Override
    public void randomDisplayTick (World world, int x, int y, int z, Random random)
    {
        if (isActive(world, x, y, z))
        {
            final TileEntity logic = world.getBlockTileEntity(x, y, z);
            byte face = 0;
            if (logic instanceof IFacingLogic)
            {
                face = ((IFacingLogic) logic).getRenderDirection();
            }
            final float f = x + 0.5F;
            final float f1 = y + 0.5F + ((random.nextFloat() * 6F) / 16F);
            final float f2 = z + 0.5F;
            final float f3 = 0.52F;
            final float f4 = (random.nextFloat() * 0.6F) - 0.3F;
            switch (face)
            {
                case 4:
                    world.spawnParticle("smoke", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle("flame", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                    break;
                case 5:
                    world.spawnParticle("smoke", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle("flame", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
                    break;
                case 2:
                    world.spawnParticle("smoke", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle("flame", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
                    break;
                case 3:
                    world.spawnParticle("smoke", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                    world.spawnParticle("flame", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
                    break;
            }
        }
    }

    @Override
    public int getLightValue (IBlockAccess world, int x, int y, int z)
    {
        return !isActive(world, x, y, z) ? 0 : 9;
    }
}