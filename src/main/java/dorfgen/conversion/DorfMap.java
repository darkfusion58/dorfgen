package dorfgen.conversion;

import static dorfgen.WorldGenerator.scale;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dorfgen.WorldGenerator;
import dorfgen.conversion.DorfMap.Region;
import dorfgen.conversion.DorfMap.SiteType;
import dorfgen.conversion.Interpolator.BicubicInterpolator;
import dorfgen.conversion.Interpolator.CachedBicubicInterpolator;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

public class DorfMap {

	public int[][] biomeMap = new int[0][0];
	public int[][] elevationMap = new int[0][0];
	public int[][] waterMap = new int[0][0];
	public int[][] riverMap = new int[0][0];
	public int[][] evilMap = new int[0][0];
	public int[][] rainMap = new int[0][0];
	public int[][] drainageMap = new int[0][0];
	public int[][] temperatureMap = new int[0][0];
	public int[][] volcanismMap = new int[0][0];
	public int[][] vegitationMap = new int[0][0];
	public static HashMap<Integer, Site> sitesByCoord = new HashMap();
	public static HashMap<Integer, Site> sitesById = new HashMap();
	public static HashMap<Integer, Region> regionsById = new HashMap();
	public static HashMap<Integer, Region> regionsByCoord = new HashMap();
	public static HashMap<Integer, Region> ugRegionsById = new HashMap();
	public static HashMap<Integer, Region> ugRegionsByCoord = new HashMap();
	static int waterShift = -35;
	
	public BicubicInterpolator			biomeInterpolator	= new BicubicInterpolator();
	public CachedBicubicInterpolator	heightInterpolator	= new CachedBicubicInterpolator();
	public CachedBicubicInterpolator	miscInterpolator	= new CachedBicubicInterpolator();
	
	public DorfMap() {
		populateBiomeMap();
		populateElevationMap();
		populateWaterMap();
		populateTemperatureMap();
		populateVegitationMap();
		populateDrainageMap();
		populateRainMap();
		
		postProcessRegions();
		if(biomeMap.length > 0)
		{
			postProcessBiomeMap();
		}
	}

	public void populateBiomeMap()
	{
		BufferedImage img = WorldGenerator.instance.biomeMap;
		if(img == null)
			return;
		biomeMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = WorldGenerator.instance.biomeMap.getRGB(x, y);
            	biomeMap[x][y] = BiomeList.GetBiomeIndex(rgb);
            }
        }
        WorldGenerator.instance.biomeMap = null;
	}
	
	public void populateElevationMap()
	{
		BufferedImage img = WorldGenerator.instance.elevationMap;
		if(img == null)
			return;
		int shift = 10;
		elevationMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = WorldGenerator.instance.elevationMap.getRGB(x, y);
            	
            	int r = (rgb >> 16) & 0xFF, g= (rgb >> 8) & 0xFF, b = (rgb >> 0) & 0xFF;
            	int h = b - shift;
            	if(r==0)
            	{
            		h = b + waterShift;
            	}
            	h = Math.max(0, h);
            	elevationMap[x][y] = h;
            	if(biomeMap.length>0)
            	if(h < 145 && biomeMap[x][y] == BiomeGenBase.extremeHillsPlus.biomeID)
            	{
            		biomeMap[x][y] = BiomeGenBase.extremeHills.biomeID;
            	}
            }
        }
        WorldGenerator.instance.elevationMap = null;
	}
	
	public void populateWaterMap()
	{
		BufferedImage img = WorldGenerator.instance.elevationWaterMap;
		if(img == null)
			return;
		waterMap = new int[img.getWidth()][img.getHeight()];
		riverMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = WorldGenerator.instance.elevationWaterMap.getRGB(x, y);
            	
            	int r = (rgb >> 16) & 0xFF, g= (rgb >> 8) & 0xFF, b = (rgb >> 0) & 0xFF;
                if (r == 0 && g == 0)
                {
                    waterMap[x][y] = b+25 + waterShift;
                    if(biomeMap.length>0)
                    if(waterMap[x][y] < 50)
                    {
                        biomeMap[x][y] = BiomeGenBase.deepOcean.biomeID;
                    }
                    else
                    {
                        biomeMap[x][y] = BiomeGenBase.ocean.biomeID;
                    }
                    riverMap[x][y] = -1;
                }
                else if (r == 0)
                {
                    waterMap[x][y] = -1;
                    riverMap[x][y] = b;
                    if(biomeMap.length>0)
                    	biomeMap[x][y] = BiomeGenBase.river.biomeID;
                }
                else
                {
                    waterMap[x][y] = -1;
                    riverMap[x][y] = -1;
                }
            }
        }
        joinRivers();
        WorldGenerator.instance.elevationWaterMap = null;
	}

	public void populateTemperatureMap()
	{
		BufferedImage img = WorldGenerator.instance.temperatureMap;
		if(img == null)
			return;
		temperatureMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = img.getRGB(x, y);
            	temperatureMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.temperatureMap = null;
	}
	public void populateVegitationMap()
	{
		BufferedImage img = WorldGenerator.instance.vegitationMap;
		if(img == null)
			return;
		vegitationMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = img.getRGB(x, y);
            	vegitationMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.vegitationMap = null;
	}
	
	public void populateDrainageMap()
	{
		BufferedImage img = WorldGenerator.instance.drainageMap;
		if(img == null)
			return;
		drainageMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = img.getRGB(x, y);
            	drainageMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.drainageMap = null;
	}
	
	public void populateRainMap()
	{
		BufferedImage img = WorldGenerator.instance.rainMap;
		if(img == null)
			return;
		rainMap = new int[img.getWidth()][img.getHeight()];
        for (int y = 0; y < img.getHeight(); y++)
        {
            for (int x = 0; x < img.getWidth(); x++)
            {
            	int rgb = img.getRGB(x, y);
            	rainMap[x][y] = rgb & 255;
            }
        }
        WorldGenerator.instance.rainMap = null;
	}
	
	private void joinRivers()
	{
        for (int y = 0; y < riverMap[0].length; y++)
        {
            for (int x = 0; x < riverMap.length; x++)
            {
            	int r = riverMap[x][y];
            	if(r>0)
            	{
            		int num = countLarger(0, waterMap, x, y, 1);
            		int num2 = countLarger(0, waterMap, x, y, 2);
            		if(num == 0 && num2 > 0)
            		{
            			int[] dir = getDirToWater(x, y);
            			riverMap[x+dir[0]][y+dir[1]] = r;
                        if(biomeMap.length>0)
                        	biomeMap[x+dir[0]][y+dir[1]] = BiomeGenBase.river.biomeID;
            			riverMap[x+2*dir[0]][y+2*dir[1]] = r;
                        if(biomeMap.length>0)
                        	biomeMap[x+2*dir[0]][y+2*dir[1]] = BiomeGenBase.river.biomeID;
            			
            		}
            	}
            }
        }
	}
	
	private int[] getDirToWater(int x, int y)
	{
		int[] ret = new int[2];
		if(waterMap[x+2][y]>0)
			ret[0] = 1;
		else if(waterMap[x-2][y]>0)
			ret[0] = -1;
		else if(waterMap[x][y+2]>0)
			ret[1] = 1;
		else if(waterMap[x][y-2]>0)
			ret[1] = -1;
		
		return ret;
	}
	
	public int countNear(int toCheck, int[][] image, int pixelX, int pixelY, int distance)
	{
		int ret = 0;
		for(int i = -distance; i<=distance; i++)
		{
			for(int j = -distance; j<=distance; j++)
			{
				if(i==0&&j==0)
					continue;
				int x = pixelX + i,y = pixelY + j;
				if(x >=0 && x < image.length && y >=0 && y < image[0].length)
				{
					if(image[x][y]==toCheck)
					{
						ret++;
					}
				}
			}
		}
		return ret;
	}
	
	public int countLarger(int toCheck, int[][] image, int pixelX, int pixelY, int distance)
	{
		int ret = 0;
		for(int i = -distance; i<=distance; i++)
		{
			for(int j = -distance; j<=distance; j++)
			{
				if(i==0&&j==0)
					continue;
				int x = pixelX + i,y = pixelY + j;
				if(x >=0 && x < image.length && y >=0 && y < image[0].length)
				{
					if(image[x][y]>toCheck)
					{
						ret++;
					}
				}
			}
		}
		return ret;
	}

	public void postProcessBiomeMap()
	{
		boolean hasHeightmap = elevationMap.length > 0;
		boolean hasThermalMap = temperatureMap.length > 0;

		for(int x = 0; x<biomeMap.length; x++)
			for(int z = 0; z<biomeMap[0].length; z++)
			{
				int biome = biomeMap[x][z];
				int temperature = hasThermalMap?temperatureMap[x][z]:128;
				int drainage = drainageMap.length>0?drainageMap[x][z]:100;
				int rain = rainMap.length>0?rainMap[x][z]:100;
				int evil = evilMap.length>0?evilMap[x][z]:100;
				Region region = getRegionForCoords(x * scale, z * scale);
				int newBiome =  BiomeList.getBiomeFromValues(biome, temperature, drainage, rain, evil, region);
				biomeMap[x][z] = newBiome;
			}
	}
	
	public Region getRegionForCoords(int x, int z)
	{
    	x = x/(scale * 16);
    	z = z/(scale * 16);
    	int key = x + 2048 * z;
    	return regionsByCoord.get(key);
	}
	
	public Region getUgRegionForCoords(int x, int depth, int z)
	{
    	x = x/(scale * 16);
    	z = z/(scale * 16);
    	int key = x + 2048 * z + depth * 4194304;
    	return ugRegionsByCoord.get(key);
	}
	
	public Site getSiteForCoords(int x, int z)
	{
    	x = x/(scale * 16);
    	z = z/(scale * 16);
    	int key = x + 2048 * z;
    	return sitesByCoord.get(key);
	}
	
	public void postProcessRegions()
	{
		for(Region region: regionsById.values())
		{
			for(int i: region.coords)
			{
				if(!regionsByCoord.containsKey(i))
				{
					regionsByCoord.put(i, region);
				}
				else
				{
					System.err.println("Existing region for "+(i&2047)+" "+(i/2048));
				}
			}
		}
		for(Region region: ugRegionsById.values())
		{
			for(int i: region.coords)
			{
				if(!ugRegionsByCoord.containsKey(i))
				{
					ugRegionsByCoord.put(i, region);
				}
				else
				{
					System.err.println("Existing region for "+(i&2047)+" "+(i/2048));
				}
			}
		}
	}
	
	public static enum SiteType
	{
		CAVE("cave"),
		FORTRESS("fortress"),
		TOWN("town"),
		HIPPYHUTS("forest retreat"),
		DARKFORTRESS("dark fortress"),
		HAMLET("hamlet"),
		VAULT("vault"),
		DARKPITS("dark pits"),
		HILLOCKS("hillocks"),
		TOMB("tomb"),
		TOWER("tower"),
		MOUNTAINHALLS("mountain halls"),
		CAMP("camp"),
		LAIR("lair"),
		SHRINE("shrine"),
		LABYRINTH("labyrinth");
		
		public final String name;
		SiteType(String name_)
		{
			name = name_;
		}
		
		public static SiteType getSite(String name)
		{
			for(SiteType t: SiteType.values())
			{
				if(t.name.equalsIgnoreCase(name))
				{
					return t;
				}
			}
			return null;
		}
		
		public boolean isVillage()
		{
			return this == TOWN || this==HAMLET || this==HILLOCKS || this==HIPPYHUTS;
		}
	}
	
	public static class Site
	{
		public final String name;
		public final int id;
		public final SiteType type;
		public final int x;
		public final int z;
		public final Set<Structure> structures = new HashSet<DorfMap.Structure>();
		public Site(String name_, int id_, SiteType type_, int x_, int z_)
		{
			name = name_;
			id = id_;
			type = type_;
			x = x_;
			z = z_;
			if(type==null)
			{
				throw new NullPointerException();
			}
		}
		@Override
		public String toString()
		{
			return name+" "+type+" "+id+" "+(x*16*scale)+" "+(z*16*scale);
		}
		
		@Override
		public int hashCode()
		{
			return id;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof Site)
			{
				return ((Site)o).id == id;
			}
			return super.equals(o);
		}
	}
	
	public static enum StructureType
	{
		MARKET("market"),
		UNDERWORLDSPIRE("underworld spire"),
		TEMPLE("temple");
		
		public final String name;
		StructureType(String name_)
		{
			name = name_;
		}
	}
	
	public static class Structure
	{
		final String name;
		final String name2;
		final int id;
		final StructureType type;
		
		public Structure(String name_, String name2_, int id_, StructureType type_)
		{
			if(name_ == null)
			{
				name_ = "";
			}
			if(name2_ == null)
			{
				name2_ = "";
			}
			name = name_;
			name2 = name2_;
			id = id_;
			type = type_;
		}
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof Structure)
			{
				return ((Structure)o).id == id;
			}
			return super.equals(o);
		}
	}
	
	public static enum RegionType
	{
		OCEAN,
		TUNDRA,
		GLACIER,
		FOREST,
		HILLS,
		GRASSLAND,
		WETLAND,
		MOUNTAINS,
		DESERT,
		LAKE,
		CAVERN,
		MAGMA,
		UNDERWORLD;
	}
	
	public static class Region
	{
		public final int id;
		public final String name;
		public final RegionType type;
		final int depth;
		public final HashSet<Integer> coords = new HashSet();
		public final HashMap<Integer, Integer> biomeMap = new HashMap();
		public Region(int id_, String name_, RegionType type_)
		{
			id = id_;
			name = name_;
			type = type_;
			depth = 0;
		}
		public Region(int id_, String name_, int depth_, RegionType type_)
		{
			id = id_;
			name = name_;
			type = type_;
			depth = depth_;
		}
		public boolean isInRegion(int x, int z)
		{
	    	x = x/(scale * 16);
	    	z = z/(scale * 16);
	    	int key = x + 2048 * z + depth * 4194304;
			return coords.contains(key);
		}
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof Region)
			{
				return ((Region)o).id == id;
			}
			return super.equals(o);
		}
		
		@Override
		public String toString()
		{
			return id+" "+name+" "+type;
		}
	}
}