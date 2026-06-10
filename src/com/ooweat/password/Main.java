package com.ooweat.password;

import com.ooweat.password.model.Password;

public class Main {

    public static void main(String[] args) {
        String algorithm = Password.SHA_256;
        boolean upper = true, lower = true, number = true, special = true;
        int maxLength = 12;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--encoding": case "-e":
                    if (i + 1 < args.length) {
                        String val = args[++i];
                        algorithm = val.equalsIgnoreCase("none") ? null : val;
                    }
                    break;
                case "--length": case "-l":
                    if (i + 1 < args.length) {
                        try {
                            maxLength = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid length: " + args[i]);
                            printUsage();
                            return;
                        }
                    }
                    break;
                case "--rules": case "-r":
                    if (i + 1 < args.length) {
                        upper = lower = number = special = false;
                        for (String r : args[++i].split(",")) {
                            switch (r.trim().toUpperCase()) {
                                case "UPPER":   upper   = true; break;
                                case "LOWER":   lower   = true; break;
                                case "NUMBER":  number  = true; break;
                                case "SPECIAL": special = true; break;
                                default:
                                    System.err.println("Unknown rule: " + r.trim());
                                    printUsage();
                                    return;
                            }
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option: " + args[i]);
                    printUsage();
                    return;
            }
        }

        String result = Password.generator(upper, lower, number, special, maxLength, algorithm);
        if (algorithm == null) {
            System.out.println("Generated (raw): " + result);
        } else {
            System.out.println("Generated (" + algorithm + "): " + result);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java Main [options]");
        System.out.println("  -e, --encoding <algorithm>  Hash algorithm: SHA-256, SHA-512, MD5, none (default: SHA-256)");
        System.out.println("  -l, --length <n>            Password length (default: 12)");
        System.out.println("  -r, --rules <rules>         Comma-separated: UPPER,LOWER,NUMBER,SPECIAL (default: all)");
    }
}
