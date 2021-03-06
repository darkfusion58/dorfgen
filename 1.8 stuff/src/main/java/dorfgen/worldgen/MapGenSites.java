package dorfgen.worldgen;

import static dorfgen.WorldGenerator.scale;
import static net.minecraftforge.common.ChestGenHooks.DUNGEON_CHEST;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dorfgen.WorldGenerator;
import dorfgen.conversion.DorfMap;
import dorfgen.conversion.SiteTerrain;
import dorfgen.conversion.DorfMap.Site;
import dorfgen.conversion.DorfMap.SiteType;
import dorfgen.conversion.SiteStructureGenerator.SiteStructures;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureStrongholdPieces;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;

public class MapGenSites extends MapGenVillage
{
	HashSet<Integer> set = new HashSet();
	HashSet<Integer> made = new HashSet();
	Site siteToGen = null;
	public MapGenSites()
	{
		super();
	}

	public MapGenSites(Map p_i2093_1_)
	{
		super(p_i2093_1_);
	}

	@Override
    protected boolean canSpawnStructureAtCoords(int x, int z)
    {
		int chunkX = x, chunkZ = z;
		x *= 16;
		z *= 16;
		x -= WorldGenerator.shift.getX();
		z -= WorldGenerator.shift.getZ();
		DorfMap dorfs = WorldGenerator.instance.dorfs;
		
		HashSet<Site> sites = dorfs.getSiteForCoords(x, z);
		
		if(sites==null)
			return false;
		
		for(Site site: sites)
		{
			
			if(shouldSiteSpawn(x, z, site) && !set.contains(site.id))
			{
				set.add(site.id);
				siteToGen = site;
				return true;
			}
		}
		
    	return false;
    }
	
	private boolean shouldSiteSpawn(int x, int z, Site site)
	{
		int[][] coords = site.corners;
		if(site.type == SiteType.LAIR)
		{
			int embarkX = (x/scale)*scale;
			int embarkZ = (z/scale)*scale;
			
			if(embarkX/scale != site.x || embarkZ/scale != site.z)
				return false;
			int relX = x%scale;
			int relZ = z%scale;
			boolean middle = relX/16 == scale/32 && relZ/16 == scale/32;
			
			return middle;
			
		}
		return false;
	}
	
	@Override
    protected StructureStart getStructureStart(int x, int z)
    {
		Site site = siteToGen;
		siteToGen = null;
		if(site==null)
		{
			return super.getStructureStart(x, z);
		}
		made.add(site.id);
		if(site.type == SiteType.FORTRESS)
		{
	        MapGenStronghold.Start start;

	        for (start = new MapGenStronghold.Start(this.worldObj, this.rand, x, z); start.getComponents().isEmpty() || ((StructureStrongholdPieces.Stairs2)start.getComponents().get(0)).strongholdPortalRoom == null; start = new MapGenStronghold.Start(this.worldObj, this.rand, x, z))
	        {
	            ;
	        }
	        return start;
		}
		else if(site.type == SiteType.DARKFORTRESS)
		{
			
		}
		else if(site.type == SiteType.DARKPITS)
		{
			
		}
		else if(site.type == SiteType.HIPPYHUTS)
		{
			return new Start(worldObj, rand, x, z, 0);
		}
		else if(site.type == SiteType.SHRINE)
		{
			return new Start(worldObj, rand, x, z, 2);
		}
		else if(site.type == SiteType.LAIR)
		{
			return new Start(worldObj, rand, x, z, 3);
		}
		else if(site.type == SiteType.CAVE)
		{
			return new Start(worldObj, rand, x, z, 1);
		}
		return super.getStructureStart(x, z);
    }
	
    public static class Start extends StructureStart
    {
        public Start() {}

        public Start(World world_, Random rand, int x, int z, int type)
        {
            super(x, z);
            if (type==0)
            {
            	for(int k = 0; k<15; k++)
            	{
            		int x1 = 40 - rand.nextInt(40); 
            		int z1  = 40 - rand.nextInt(40); 
            		
	                for(int i = 0; i<rand.nextInt(20); i++)
	                {
	                	
	                    ComponentScatteredFeaturePieces.SwampHut swamphut = new ComponentScatteredFeaturePieces.SwampHut(rand, x * 16 + x1, z * 16 + z1);
	                    
	                    this.components.add(swamphut);
	                }
            	}
            }
            else if(type==1)
            {
                ComponentScatteredFeaturePieces.DesertPyramid desertpyramid = new ComponentScatteredFeaturePieces.DesertPyramid(rand, x * 16, z * 16);
                this.components.add(desertpyramid);
            }
            else if(type==2)
            {
                ComponentScatteredFeaturePieces.JunglePyramid junglepyramid = new ComponentScatteredFeaturePieces.JunglePyramid(rand, x * 16, z * 16);
                this.components.add(junglepyramid);
            }
            else if(type==3)
            {
            	//TODO re-copy dungeon code from 1.8 again for this
//            	int h = 0;
//                int[][] map = WorldGenerator.instance.dorfs.elevationMap;
//        		int x1 = x * 16 - WorldGenerator.shift.getX();
//        		int z1 = z * 16 - WorldGenerator.shift.getZ();
//                
//                if(x1>0&&z1>0&&x1/scale < map.length && z1/scale < map[0].length)
//                {
//                	h = map[x1/scale][z1/scale];
//                }
//                else
//                {
//                	h = world_.getHeightValue(x*16, z*16);
//                }
//                x1 = x*16 + rand.nextInt(8);
//                z1 = z*16 + rand.nextInt(8);
//                for(int i = -4; i<=4; i++)
//                    for(int j = -4; j<=4; j++)
//                        for(int k = -4; k<=4; k++)
//                {
//                	world_.setBlock(x1 + i, h+j, z1+k, Blocks.cobblestone, 0, 2);
//                }
//                
//                byte b0 = 3;
//                int l = rand.nextInt(2) + 2;
//                int i1 = rand.nextInt(2) + 2;
//                int j1 = 3;
//                int k1;
//                int l1;
//                int i2;
//                
//                if (j1 >= 1 && j1 <= 5)
//                {
//                    for (k1 = x1 - l - 1; k1 <= x1 + l + 1; ++k1)
//                    {
//                        for (l1 = h + b0; l1 >= h - 1; --l1)
//                        {
//                            for (i2 = z1 - i1 - 1; i2 <= z1 + i1 + 1; ++i2)
//                            {
//                                if (k1 != x1 - l - 1 && l1 != h - 1 && i2 != z1 - i1 - 1 && k1 != x1 + l + 1 && l1 != h + b0 + 1 && i2 != z1 + i1 + 1)
//                                {
//                                    world_.setBlockToAir(k1, l1, i2);
//                                }
//                                else if (l1 >= 0 && !world_.getBlock(k1, l1 - 1, i2).getMaterial().isSolid())
//                                {
//                                    world_.setBlockToAir(k1, l1, i2);
//                                }
//                                else if (world_.getBlock(k1, l1, i2).getMaterial().isSolid())
//                                {
//                                    if (l1 == h - 1 && rand.nextInt(4) != 0)
//                                    {
//                                        world_.setBlock(k1, l1, i2, Blocks.mossy_cobblestone, 0, 2);
//                                    }
//                                    else
//                                    {
//                                        world_.setBlock(k1, l1, i2, Blocks.cobblestone, 0, 2);
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    k1 = 0;
//
//                    while (k1 < 2)
//                    {
//                        l1 = 0;
//
//                        while (true)
//                        {
//                            if (l1 < 3)
//                            {
//                                label197:
//                                {
//                                    i2 = x1 + rand.nextInt(l * 2 + 1) - l;
//                                    int j2 = z1 + rand.nextInt(i1 * 2 + 1) - i1;
//
//                                    if (world_.isAirBlock(i2, h, j2))
//                                    {
//                                        int k2 = 0;
//
//                                        if (world_.getBlock(i2 - 1, h, j2).getMaterial().isSolid())
//                                        {
//                                            ++k2;
//                                        }
//
//                                        if (world_.getBlock(i2 + 1, h, j2).getMaterial().isSolid())
//                                        {
//                                            ++k2;
//                                        }
//
//                                        if (world_.getBlock(i2, h, j2 - 1).getMaterial().isSolid())
//                                        {
//                                            ++k2;
//                                        }
//
//                                        if (world_.getBlock(i2, h, j2 + 1).getMaterial().isSolid())
//                                        {
//                                            ++k2;
//                                        }
//
//                                        if (k2 == 1)
//                                        {
//                                            world_.setBlock(i2, h, j2, Blocks.chest, 0, 2);
//                                            TileEntityChest tileentitychest = (TileEntityChest)world_.getTileEntity(i2, h, j2);
//
//                                            if (tileentitychest != null)
//                                            {
//                                                WeightedRandomChestContent.generateChestContents(rand, ChestGenHooks.getItems(DUNGEON_CHEST, rand), tileentitychest, ChestGenHooks.getCount(DUNGEON_CHEST, rand));
//                                            }
//
//                                            break label197;
//                                        }
//                                    }
//
//                                    ++l1;
//                                    continue;
//                                }
//                            }
//
//                            ++k1;
//                            break;
//                        }
//                    }
//
//                    world_.setBlock(x1, h, z1, Blocks.mob_spawner, 0, 2);
//                    TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world_.getTileEntity(x1, h, z1);
//                    
//                    if (tileentitymobspawner != null)
//                    {
//                        tileentitymobspawner.func_145881_a().setEntityName(DungeonHooks.getRandomDungeonMob(rand));
//                    }
//                    else
//                    {
//                        System.err.println("Failed to fetch mob spawner entity at (" + x1 + ", " + h + ", " + z1 + ")");
//                    }
//                }
            }

            this.updateBoundingBox();
        }
    }
}
