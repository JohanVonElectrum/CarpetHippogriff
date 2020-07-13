package carpet.commands;

import carpet.CarpetSettings;
import carpet.utils.DistanceCalculator;
import carpet.utils.Messenger;
import com.sun.media.jfxmedia.logging.Logger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.*;

public class CommandHelper extends CommandCarpetBase
{

    @Override
    public String getName() { return "helper"; }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/helper <type> <option> <args>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandHelper", sender)) return;
        if (args.length != 3) throw new WrongUsageException(getUsage(sender));

        if ("give".equalsIgnoreCase(args[0]) && "hex".equalsIgnoreCase(args[1])) server.commandManager.executeCommand(sender, getCommand(args[2]));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandHelper)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "info", "give");
        }
        if (args.length == 2 && "give".equalsIgnoreCase(args[0]))
        {
            return getListOfStringsMatchingLastWord(args, "hex");
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0]) && "hex".equalsIgnoreCase(args[1]))
        {
            return getListOfStringsMatchingLastWord(args, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f");
        }
        return Collections.<String>emptyList();
    }

    public String getCommand(String hex) {
        double n = 64 * 9 * Integer.parseInt(hex, 16) / 15;
        n = n < 1 ? Math.round(n) : Math.floor(n);
        String data = "";
        for (int i = 0; i < 9; i++) {
            int count = n > 64 ? 64 : (int)n;
            data += (i > 0 ? "," : "") + "{Slot:" + i + ",id:redstone,Count:" + count + "}";
            n -= count;
        }
        return "/give @p dispenser 1 0 {BlockEntityTag:{Items:[" + data + "]}}";
    }
}
