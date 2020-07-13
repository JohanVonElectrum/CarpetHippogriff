package carpet.commands;

import carpet.CarpetSettings;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class CommandHttp extends CommandCarpetBase {
    @Override
    public String getName() { return "http"; }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/http <method> <target> <json>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!command_enabled("commandHttp", sender)) return;

        if ("get".equalsIgnoreCase(args[0]) && args.length == 2) server.commandManager.executeCommand(sender,  getCommand(httpRequest(args[0], args[1], "")));
        else if ("post".equalsIgnoreCase(args[0]) && args.length == 3) httpRequest(args[0], args[1], args[2]);
        else throw new WrongUsageException(getUsage(sender));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        if (!CarpetSettings.commandHttp)
        {
            return Collections.<String>emptyList();
        }
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "get", "post");
        }
        if (args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, "http://localhost/registry/");
        }
        if (args.length == 3 && "post".equalsIgnoreCase(args[0]))
        {
            return getListOfStringsMatchingLastWord(args, "{\"data\":[{\"id\":\"\",\"value\":\"\"}]}");
        }
        return Collections.<String>emptyList();
    }

    public String httpRequest(String method, String target, String json) throws CommandException {
        try {
            URL url = new URL(target);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method.toUpperCase());
            connection.setConnectTimeout(50);
            connection.setReadTimeout(50);
            if ("POST".equals(method.toUpperCase())) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
            }

            int status = connection.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) content.append(inputLine);
            in.close();

            if (content.toString().length() > 1 && !"POST".equals(method.toUpperCase())) throw new WrongUsageException("Response is not a number between 0 and f in hexadecimal format. String: " + content.toString());

            return content.toString();
        } catch (MalformedURLException e) {
            throw new WrongUsageException("Malformed url.");
        } catch (IOException e) {
            throw new CommandException("commands.generic.exception");
        }
    }

    public String getCommand(String hex) {
        double n = 192 * Integer.parseInt(hex, 16) / 15;
        n = n < 1 ? Math.round(n) : Math.floor(n);
        String data = "";
        for (int i = 0; i < 3; i++) {
            int count = n > 64 ? 64 : (int)n;
            data += (i > 0 ? "," : "") + "{Slot:" + i + ",id:redstone,Count:" + count + "}";
            n -= count;
        }
        return "/blockdata ~ ~1 ~ {Items:[" + data + "]}";
    }
}