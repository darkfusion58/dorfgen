package dorfgen;

import java.util.ArrayList;
import java.util.List;

import dorfgen.conversion.DorfMap.Site;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class Commands implements ICommand
{
	private List aliases;

	public Commands()
	{
		this.aliases = new ArrayList();
		this.aliases.add("dorfgen");
		this.aliases.add("dg");
	}

	@Override
	public int compareTo(Object arg0)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCommandName()
	{
		return "dorfgen";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		// TODO Auto-generated method stub
		return "dorfgen <text>";
	}

	@Override
	public List getCommandAliases()
	{
		return aliases;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(args.length > 1 && args[0].equalsIgnoreCase("tp") && sender instanceof EntityPlayer)
		{
			String name = args[1];
			EntityPlayer entity = (EntityPlayer) sender;
			Site telesite = null;
			try
			{
				int id = Integer.parseInt(name);
				telesite = WorldGenerator.instance.dorfs.sitesById.get(id);
			}
			catch (NumberFormatException e)
			{
				ArrayList<Site> sites = new ArrayList(WorldGenerator.instance.dorfs.sitesById.values());
				for(Site s: sites)
				{
					if(s.name.equalsIgnoreCase(name))
					{
						telesite = s;
						break;
					}
				}
			}
			if(telesite!=null)
			{
				int x = telesite.x * 16 * WorldGenerator.scale + WorldGenerator.scale * 8;
				int z = telesite.z * 16 * WorldGenerator.scale + WorldGenerator.scale * 8;
				
				int y = WorldGenerator.instance.dorfs.elevationMap[(x - WorldGenerator.instance.shift.posX) / WorldGenerator.scale]
						[(z - WorldGenerator.instance.shift.posZ) / WorldGenerator.scale];
				
				entity.setPositionAndUpdate(x, y, z);
			}
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
	{
		// TODO Auto-generated method stub
		return false;
	}

}