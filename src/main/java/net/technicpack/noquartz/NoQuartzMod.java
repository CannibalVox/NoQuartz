package net.technicpack.noquartz;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;

@Mod(modid = NoQuartzMod.MODID, version = NoQuartzMod.VERSION)
public class NoQuartzMod
{
    public static final String MODID = "noquartz";
    public static final String VERSION = "1.0";

    private static Block ubcStairs;
    private static Block ubcSlab;
    private static Block ubcDoubleSlab;
    private static Block ubcBlock;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ubcStairs = (Block)Block.blockRegistry.getObject("UndergroundBiomes:stairs");
        ubcSlab = (Block)Block.blockRegistry.getObject("UndergroundBiomes:metamorphicStoneHalfSlab");
        ubcDoubleSlab = (Block)Block.blockRegistry.getObject("UndergroundBiomes:metamorphicStoneFullSlab");
        ubcBlock = (Block)Block.blockRegistry.getObject("UndergroundBiomes:metamorphicStone");
    }

    @SubscribeEvent
    public void loadMapChunk(ChunkEvent.Load chunkLoad) {
        //Only convert overworld
        Chunk chunk = chunkLoad.getChunk();
        World world = chunk.worldObj;
        if (world.provider.dimensionId != 0)
            return;
        if (world.isRemote)
            return;

        //For each block

        int height = world.getHeight();
        int worldX = chunk.xPosition*16;
        int worldZ = chunk.zPosition*16;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block block = chunk.getBlock(x,y,z);
                    String blockName = Block.blockRegistry.getNameForObject(block);

                    if (blockName.equals("minecraft:quartz_stairs"))
                        convertStairs(world, x+worldX,y,z+worldZ);
                    else if (blockName.equals("minecraft:stone_slab") && (chunk.getBlockMetadata(x,y,z) & 7) == 7)
                        convertSingleSlab(world, x+worldX,y,z+worldZ);
                    else if (blockName.equals("minecraft:double_stone_slab") && (chunk.getBlockMetadata(x,y,z) & 7) == 7)
                        convertDoubleSlab(world, x+worldX,y,z+worldZ);
                    else if (blockName.equals("minecraft:quartz_block"))
                        convertBlock(world, x+worldX, y, z+worldZ);
                }
            }
        }
    }

    private void convertStairs(World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x,y,z);
        world.setBlock(x, y, z, ubcStairs, metadata, 3);
        makeTileEntityMarble(world, x, y, z);
    }

    private void convertSingleSlab(World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x,y,z) & 8;
        world.setBlock(x, y, z, ubcSlab, metadata|2, 3);
    }

    private void convertDoubleSlab(World world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z) & 8;
        world.setBlock(x, y, z, ubcDoubleSlab, metadata|2, 3);
    }

    private void convertBlock(World world, int x, int y, int z) {
        world.setBlock(x, y, z, ubcBlock, 2, 3);
    }

    private void makeTileEntityMarble(World world, int x, int y, int z) {
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity == null)
            return;

        NBTTagCompound tileEntityData = new NBTTagCompound();
        tileEntity.writeToNBT(tileEntityData);
        tileEntityData.setInteger("index", 2);
        tileEntity.readFromNBT(tileEntityData);
        tileEntity.markDirty();
    }
}