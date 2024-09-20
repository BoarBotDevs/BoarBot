package dev.boarbot.util.python;

import dev.boarbot.util.logging.Log;
import dev.boarbot.util.resource.ResourceUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class PythonUtil {
    private final static Map<String, Path> scripts = new HashMap<>();

    public static byte[] getResult(Process pythonProcess, byte[]... byteArrays) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()));
        BufferedReader stdErr = new BufferedReader(new InputStreamReader(pythonProcess.getErrorStream()));
        OutputStream stdOut = pythonProcess.getOutputStream();

        PythonUtil.sendInput(stdOut, byteArrays);
        String result = stdIn.readLine();
        PythonUtil.checkException(result, stdErr);

        return Base64.getDecoder().decode(result);
    }

    private static void sendInput(OutputStream stdOut, byte[]... byteArrays) throws IOException {
        for (byte[] byteArray : byteArrays) {
            stdOut.write(byteArray);
        }

        stdOut.close();
    }

    private static void checkException(String result, BufferedReader stdErr) throws IOException {
        if (result == null) {
            String tempErrMessage;
            String errMessage = "";

            while ((tempErrMessage = stdErr.readLine()) != null) {
                errMessage = errMessage.concat(tempErrMessage + "\n");
            }

            Log.error(PythonUtil.class, "Python script threw an exception", new RuntimeException(errMessage));
        }
    }

    public static String getTempPath(String scriptPath) throws IOException {
        String scriptName = scriptPath.split("/")[1].split("\\.")[0];

        if (scripts.containsKey(scriptName)) {
            return scripts.get(scriptName).toString();
        }

        InputStream is = ResourceUtil.getResourceStream(scriptPath);

        Path tempScript = Files.createTempFile(scriptName, ".py");
        Files.copy(is, tempScript, StandardCopyOption.REPLACE_EXISTING);
        tempScript.toFile().deleteOnExit();

        scripts.put(scriptName, tempScript);
        return tempScript.toString();
    }
}
