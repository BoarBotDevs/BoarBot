package dev.boarbot.util.python;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Base64;

@Slf4j
public class PythonUtil {
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

            log.error(errMessage);
        }
    }
}
