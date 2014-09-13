/*
 *   
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */
package mobi.cyann.shifttools;

import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Helper {

    private static Helper instance = null;
    private List<String> errResult;
    private List<String> outResult;
    private List<String> kmsg;
    private Process p;
    OutputStreamWriter osw;
    OutputStreamWriter oswk;
    OutputStreamWriter oswl;
//        BufferedReader err;
//        BufferedReader out;

    protected Helper() {
        errResult = new ArrayList<String>();
        outResult = new ArrayList<String>();
        kmsg = new ArrayList<String>();
    }

    public static Helper getInstance() {
        if (instance == null) {
            synchronized (Helper.class) {
                if (instance == null) {
                    instance = new Helper();
                }
            }
        }
        return instance;
    }

    public List<String> getErrResult() {
        return errResult;
    }

    public List<String> getOutResult() {
        return outResult;
    }

    public List<String> getKmsg() {
        return kmsg;
    }

    public void openShell() {
        ProcessBuilder pb = new ProcessBuilder("su", "-c", "/system/bin/sh");
        try {
            p = pb.start();
            OutputStream os = p.getOutputStream();
            osw = new OutputStreamWriter(os);

//            err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//            out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            // Preform su to get root privledges
            //p = Runtime.getRuntime().exec("su");

            // Attempt to write a file to a root-only
        } catch (IOException ex) {
        }
    }

    public void closeShell() {
        try {
            // Close the terminal
            osw.write("\nexit\n");
            osw.close();
        } catch (IOException ex) {
        }
    }

    public boolean needSU(String path) {
        File f = new File(path);
        if (f.exists() && f.isFile() && f.canWrite()) {
            return false;
        }
        return true;
    }

    public int readFile(String path) {
        int result = 1;

        outResult.clear();
        File f = new File(path);
        if (f.exists() && f.isFile() && f.canRead()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f), 512);
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        outResult.add(line);
                    }

                    if (!outResult.isEmpty()) {
                        result = 0;
                    }
                    br.close();
                } catch (IOException ex) {
                    Logger.getLogger(Helper.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (FileNotFoundException ex) {
                Log.e(Helper.class.getName(), "Error reading file: ".concat(path));
            }
        }

        return result;
    }

    public int writeFile(String path, String value) {
        try {
            File file = new File(path);

            if (!file.exists() || !file.canWrite()) {
                return 1;
            }
            
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            fw.write(value);
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.write(value);
            fw.close();
            
            return 0;
        } catch (IOException e) {
            Log.e(Helper.class.getName(), "Error writing file: ".concat(path));
            return 1;
        }
    }

    public int runSuBatch(List<String> cmds) {
        int result = 0;
        int exitValue = -99;

        ProcessBuilder pb = new ProcessBuilder("su", "-c", "/system/bin/sh");
        try {
            p = pb.start();
            OutputStream os = p.getOutputStream();
            osw = new OutputStreamWriter(os);

            for (String s : cmds) {
                osw.write(s + "\n");
            }
            osw.write("\nexit\n");
            osw.flush();
            osw.close();

            errResult.clear();
            outResult.clear();

            Thread errt = new streamReader(p.getErrorStream(), errResult);
            Thread outt = new streamReader(p.getInputStream(), outResult);
            errt.start();
            outt.start();

            try {
                exitValue = p.waitFor();
                try {
                    errt.join();
                    outt.join();
                } catch (InterruptedException ex) {
                }

                if (exitValue == 0) {
                    if (errResult.isEmpty()) {
                        result = 0;
                    } else {
                        result = 1;
                    }

                } else {
                    result = 1;
                }
            } catch (InterruptedException e) {
                result = 1;
            }
        } catch (IOException e) {
            result = 1;
        }
        return result;
    }

    public int run(String cmd, boolean su) {
        int result = 0;

        ProcessBuilder pb;
        if (su) {
            pb = new ProcessBuilder("su", "-c", "/system/bin/sh");
        } else {
            pb = new ProcessBuilder("/system/bin/sh");
        }
        try {
            p = pb.start();
            OutputStream os = p.getOutputStream();
            osw = new OutputStreamWriter(os);

            osw.write(cmd);
            osw.write("\nexit\n");
            osw.flush();
            osw.close();

            errResult.clear();
            outResult.clear();

            Thread errt = new streamReader(p.getErrorStream(), errResult);
            Thread outt = new streamReader(p.getInputStream(), outResult);
            errt.start();
            outt.start();

            try {
                p.waitFor();
                try {
                    errt.join();
                    outt.join();
                } catch (InterruptedException ex) {
                }

                if (p.exitValue() == 0) {
                    if (errResult.isEmpty()) {
                        result = 0;
                    } else {
                        result = 1;
                    }

                } else {
                    result = 1;
                }
            } catch (InterruptedException e) {
                result = 1;
            }
        } catch (IOException e) {
            result = 1;
        }
        return result;
    }

    public int readKmsg() {
        int result = 0;

        ProcessBuilder pb = new ProcessBuilder("su", "-c", "/system/bin/sh");
        try {
            p = pb.start();
            OutputStream os = p.getOutputStream();
            oswk = new OutputStreamWriter(os);

            oswk.write("cat /proc/kmsg\n");
            oswk.flush();
            oswk.close();

            kmsg.clear();

            Thread outt = new streamReader(p.getInputStream(), kmsg);
            outt.start();

        } catch (IOException e) {
        }
        return result;
    }

    public void clearKmsg() {
        kmsg.clear();
    }

    private class streamReader extends Thread {

        InputStream inputStream;
        List<String> result;

        streamReader(InputStream inputStrem, List<String> result) {
            this.inputStream = inputStrem;
            this.result = result;
        }

        @Override
        public void run() {
            try {
                BufferedReader out = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = out.readLine()) != null) {
                    result.add(line);
                }
            } catch (java.io.IOException e) {
            }
        }

        public List<String> getResult() {
            return result;
        }
    }
}
