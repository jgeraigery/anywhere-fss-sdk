/*
 * ========================================================================
 * 
 * Copyright (c) by Hitachi Data Systems, 2016. All rights reserved.
 * 
 * ========================================================================
 */

package com.hds.hcpaw.fss.api;

import com.google.common.collect.Sets;
import com.hds.hcpaw.fss.api.exception.AnywhereException;
import com.hds.hcpaw.fss.api.model.AuthToken;
import com.hds.hcpaw.fss.api.model.Entry;
import com.hds.hcpaw.fss.api.model.FolderListing;
import com.hds.hcpaw.fss.api.model.Link;
import com.hds.hcpaw.fss.api.model.LinkPermission;
import com.hds.hcpaw.fss.api.model.PathSearchResult;
import com.hds.hcpaw.fss.api.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.codec.binary.Hex;

/**
 * Sample application that demonstrates how to use the HCP Anywhere SDK. With this application, you
 * can enter supported commands on the command line. The commands are then sent to the HCP Anywhere
 * server of your choosing.
 */
public class AwSdkTest {

    private static final String PADDING = "                               ";
    private static final int PADDING_LENGTH = PADDING.length();

    private enum Command {
        QUIT("quit", "Exit this program"),
        HELP("help", "Prints available commands"),
        FOLDER_CREATE("createFolder", "Creates a folder"),
        FOLDER_LIST("listFolder", "Lists contents of a folder, one page at a time"),
        PATH_MOVE("move", "Moves a file or folder"),
        PATH_DELETE("delete", "Deletes a file or folder"),
        PATH_INFO("pathInfo", "Gets information (metadata) about a file or folder"),
        PATH_SEARCH("pathSearch",
                "Searches for files and folders whose name contains a specified substring"),
        FILE_CREATE("createFile", "Creates (uploads a new) a file or folder"),
        FILE_UPDATE("updateFile", "Updates (uploads a new version of) a file or folder"),
        FILE_READ("readFile", "Downloads a file or folder"),
        LINK_CREATE("createLink", "Creates a link to a file or folder"),
        USER_INFO("userInfo", "Gets information about the authenticated user");

        public final String name;
        public final String lowerName;
        public final String description;

        private Command(String name, String description) {
            this.name = name;
            this.lowerName = name.toLowerCase();
            this.description = description;
        }

        public static Command fromString(String str) {
            str = str.toLowerCase();
            for (Command cmd : Command.values()) {
                if (cmd.lowerName.equals(str)) {
                    return cmd;
                }
            }
            return null;
        }
    }

    /**
     * Main entry point for this command-line utility in order to test the SDK.
     * 
     * @param args
     *            Run without arguments to see a list of required arguments.
     */
    public static void main(String[] args) {
        try {
            if (args.length < 4) {
                System.out.println("Usage: AwSdkTest <server> <port> <username> <password>");
                System.exit(-1);
            }

            Scanner scanner = new Scanner(System.in);

            AnywhereAPI api = new AnywhereAPI.Builder("https://" + args[0] + ":" + args[1])
                    .insecureSSL().build();
            AuthToken authToken;
            try {
                authToken = api.authenticate(args[2], args[3]);
            } catch (AnywhereException e) {
                throw e;
            } catch (Exception e) {
                String technicalMessage = (e instanceof AnywhereException ? ((AnywhereException) e)
                        .getTechnicalMessage() : e.getMessage());
                throw new AnywhereException(
                        technicalMessage,
                        String.format("An error occurred authenticating user %s with HCP AW.  "
                                + "Make sure that the user belongs to a profile with FSS API access.",
                                      args[2]
                                ), e);
            }

            AnywhereFolderAPI folderApi = api.getFolderAPI();
            AnywherePathAPI pathApi = api.getPathAPI();
            AnywhereFileAPI fileApi = api.getFileAPI();
            AnywhereLinkAPI linkApi = api.getLinkAPI();
            AnywhereUserAPI userApi = api.getUserAPI();

            boolean done = false;
            while (!done) {
                try {
                    String commandStr = nextString(scanner, "Enter a command");
                    Command command = Command.fromString(commandStr);
                    if (command == null) {
                        System.out.println("Invalid command: " + commandStr);
                        command = Command.HELP;
                    }

                    switch (command) {
                        default:
                        case HELP:
                            printHelp();
                            break;
                        case QUIT:
                            done = true;
                            break;
                        case FOLDER_CREATE:
                            createFolder(scanner, authToken, folderApi);
                            break;
                        case FOLDER_LIST:
                            listFolder(scanner, authToken, folderApi);
                            break;
                        case PATH_DELETE:
                            deletePath(scanner, authToken, pathApi);
                            break;
                        case PATH_MOVE:
                            movePath(scanner, authToken, pathApi);
                            break;
                        case PATH_INFO:
                            getPathInfo(scanner, authToken, pathApi);
                            break;
                        case PATH_SEARCH:
                            searchPath(scanner, authToken, pathApi);
                            break;
                        case FILE_CREATE:
                            uploadFile(scanner, authToken, fileApi, true);
                            break;
                        case FILE_UPDATE:
                            uploadFile(scanner, authToken, fileApi, false);
                            break;
                        case FILE_READ:
                            downloadFile(scanner, authToken, fileApi);
                            break;
                        case LINK_CREATE:
                            createLink(scanner, authToken, linkApi);
                            break;
                        case USER_INFO:
                            getUserInfo(authToken, userApi);
                            break;
                    }
                } catch (AnywhereException e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    System.out.println("Details: " + e.getTechnicalMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    System.out.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                }
                System.out.println();
            }
        } catch (AnywhereException e) {
            System.out.println("Unexpected error: " + e.getMessage() + " : "
                    + e.getTechnicalMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prints a list of supported commands to stdout.
     */
    private static void printHelp() {
        System.out.println("The following commands are supported:");
        for (Command cmd : Command.values()) {
            System.out.println(String.format("%s%s%s", cmd.name,
                                             PADDING.substring(cmd.name.length(), PADDING_LENGTH),
                                             cmd.description));
        }
    }

    /**
     * Creates a folder.
     * 
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param folderApi
     *            API for performing folder operations.
     * @throws Exception
     */
    private static void createFolder(Scanner scanner, AuthToken authToken,
                                     AnywhereFolderAPI folderApi) throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        boolean createParents = nextBoolean(scanner, "Create parents?  Enter true or false");
        Entry entry = folderApi.create(authToken, path, createParents);
        printObject("Success!  Resulting entry: ", entry);
    }

    /**
     * Lists the contents of a folder.
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param folderApi
     *            API for performing folder operations.
     * @throws Exception
     */
    private static void listFolder(Scanner scanner, AuthToken authToken,
                                   AnywhereFolderAPI folderApi) throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        int pageSize = nextInt(scanner, "Enter page size");
        String pageToken = null;

        do {
            FolderListing folderListing = folderApi.listEntries(authToken, path, pageToken,
                                                                pageSize);
            if (folderListing != null) {
                System.out.println(folderListing.toString());
                pageToken = folderListing.getPageToken();

                if (pageToken != null) {
                    boolean listMore =
                            nextBoolean(scanner, "List more entries?  Enter true or false");
                    if (!listMore) {
                        pageToken = null;
                    }
                } else {
                    System.out.println("No more entries");
                    System.out.flush();
                }
            }
        } while (pageToken != null);
    }

    /**
     * Deletes a file or folder.
     * 
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param pathApi
     *            API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void deletePath(Scanner scanner, AuthToken authToken,
                                   AnywherePathAPI pathApi) throws Exception {
        String path = nextString(scanner,
                                 "Enter the path (including file or folder name) to delete");
        String etag = nextString(scanner, "Enter the etag for the path");
        boolean recursive = nextBoolean(scanner, "Recursive delete?  Enter true or false");

        pathApi.delete(authToken, path, etag, recursive);
        System.out.println("Successfully deleted path");
    }

    /**
     * Moves a file or folder to a different location.
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param pathApi
     *            API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void movePath(Scanner scanner, AuthToken authToken,
                                 AnywherePathAPI pathApi) throws Exception {
        String sourcePath = nextString(scanner,
                                       "Enter the source path (including file or folder name)");
        String destPath = nextString(scanner,
                                     "Enter the destination path (including file or folder name)");
        String etag = nextString(scanner, "Enter the etag for the source path");
        boolean createParents = nextBoolean(scanner, "Create parents?  Enter true or false");

        Entry entry = pathApi.move(authToken, sourcePath, destPath, etag, createParents);
        printObject("Success!  Resulting entry: ", entry);
    }

    /**
     * Gets information (metadata) about a file or folder.
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param pathApi
     *            API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void getPathInfo(Scanner scanner, AuthToken authToken, AnywherePathAPI pathApi)
            throws Exception {
        String path = nextString(scanner, "Enter the remote path (including file or folder name)");

        Entry entry = pathApi.getInfo(authToken, path);
        printObject("Success!  Found entry: ", entry);
    }

    /**
     * Searches for files and folders whose name contains a specified substring
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param pathApi
     *            API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void searchPath(Scanner scanner, AuthToken authToken,
                                   AnywherePathAPI pathApi) throws Exception {
        String prefix = nextString(scanner,
                                   "Enter a string that matching file or folder names must contain");
        String path = nextString(scanner, "Enter a path to search under");
        int maxResults = nextInt(scanner,
                                 "Enter the maximum number of matching files and folders to return");

        PathSearchResult result = pathApi.search(authToken, path, prefix, maxResults);
        System.out.println(result.toString());
    }

    /**
     * Uploads a new or modified file.
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param fileApi
     *            API for performing file operations.
     * @param isNewFile
     *            True indicates that no file currently exists at the specified path. False
     *            indicates that there the file already exists at the specified path.
     * @throws Exception
     */
    private static void uploadFile(Scanner scanner, AuthToken authToken,
                                   AnywhereFileAPI fileApi, boolean isNewFile) throws Exception {
        String sourcePath = nextString(scanner,
                                       "Enter the path (including file name) of the local file");
        String destPath = nextString(scanner,
                                     "Enter the remote destination path (including file name)");
        String etag = null;
        boolean createParents = false;
        if (isNewFile) {
            createParents = nextBoolean(scanner, "Create parents?  Enter true or false");
        } else {
            etag = nextString(scanner, "Enter the etag for the destination file");
        }

        System.out.print("Computing size and hash of local file, please wait ...");
        System.out.flush();
        File f = new File(sourcePath);
        long size = f.length();
        String hash = computeHash(f);
        System.out.println(" Done computing size and hash");

        try (InputStream in = new FileInputStream(f)) {

            Entry entry;
            if (isNewFile) {
                entry = fileApi.upload(authToken, destPath, size, hash, createParents, in);
            } else {
                entry = fileApi.update(authToken, destPath, size, hash, etag, in);
            }
            printObject("Success!  Resulting entry: ", entry);
        }
    }

    /**
     * Downloads a file.
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param fileApi
     *            API for performing file operations.
     * @throws Exception
     */
    private static void downloadFile(Scanner scanner, AuthToken authToken,
                                     AnywhereFileAPI fileApi) throws Exception {
        String sourcePath = nextString(scanner,
                                       "Enter the remote path (including file name) of the file");
        String etag = nextString(scanner,
                                 "To download the file only if it has changed, enter the last known etag of the remote path, or \"none\" if you want to download the file regardless if it has changed");
        if ("none".equalsIgnoreCase(etag)) {
            etag = null;
        }
        String destPath = nextString(scanner,
                                     "Enter the local path (including file name) to download into");

        File f = new File(destPath);
        try (OutputStream out = new FileOutputStream(f)) {
            Entry entry = fileApi.read(authToken, sourcePath, etag, out);
            if (entry == null) {
                System.out.println("File unchanged; nothing downloaded");
            } else {
                printObject("Successfully downloaded entry: ", entry);
            }
        }
    }

    /**
     * Creates a shared link
     *
     * @param scanner
     *            User input is read from this scanner.
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param linkApi
     *            API for performing link operations.
     * @throws Exception
     */
    private static void createLink(Scanner scanner, AuthToken authToken,
                                   AnywhereLinkAPI linkApi) throws Exception {
        String path = nextString(scanner, "Enter the path to the file or folder to share as link");
        int expirationDays = nextInt(scanner, "Enter the number of days until the link expires");
        boolean isPublic = nextBoolean(scanner, "Is link public?  Enter true or false");
        boolean useAccessCode = nextBoolean(scanner, "Use an access code? Enter true or false");
        Set<LinkPermission> permissions = null;
        while (permissions == null) {
            permissions = toPermissions(nextString(scanner,
                                                   "Enter the link permissions (R, W, or R/W"));
        }

        Link link = linkApi.create(authToken, path, expirationDays, isPublic, useAccessCode,
                                   permissions);
        printObject("Success! Resulting link: ", link);
    }

    private static Set<LinkPermission> toPermissions(String str) {
        switch (str.trim().toUpperCase()) {
            case "R":
                return Sets.newHashSet(LinkPermission.READ);
            case "W":
                return Sets.newHashSet(LinkPermission.UPLOAD);
            case "R/W":
                return Sets.newHashSet(LinkPermission.READ, LinkPermission.UPLOAD);
            default:
                return null;
        }
    }

    /**
     * Gets information about a user.
     *
     * @param authToken
     *            Authentication token for the user performing the request.
     * @param userApi
     *            API for performing user operations.
     * @throws Exception
     */
    private static void getUserInfo(AuthToken authToken, AnywhereUserAPI userApi)
            throws Exception {
        User user = userApi.getInfo(authToken);
        System.out.println("Success!  User info: " + user.toString());
    }

    /**
     * Prints an object with a label to stdout.
     * 
     * @param label
     *            Prints before the object.
     * @param obj
     *            Prints after the label.
     */
    private static void printObject(String label, Object obj) {
        System.out.println(label + " " + (obj == null ? "null" : obj.toString()));
    }

    /**
     * Writes the prompt to stdout and then reads a string from the scanner.
     * 
     * @param scanner
     * @param prompt
     * @return
     */
    private static String nextString(Scanner scanner, String prompt) {
        System.out.print(prompt + ": ");
        System.out.flush();
        return scanner.next();
    }

    /**
     * Writes the prompt to stdout and then reads an integar from the scanner.
     *
     * @param scanner
     * @param prompt
     * @return
     */
    private static int nextInt(Scanner scanner, String prompt) {
        System.out.print(prompt + ": ");
        System.out.flush();
        return scanner.nextInt();
    }

    /**
     * Writes the prompt to stdout and then reads a boolean from the scanner.
     *
     * @param scanner
     * @param prompt
     * @return
     */
    private static boolean nextBoolean(Scanner scanner, String prompt) {
        System.out.print(prompt + ": ");
        System.out.flush();
        return scanner.nextBoolean();
    }

    /**
     * Computes the SHA-384 hash of the specified file.
     * 
     * @param file
     *            The file which you want to compute the SHA-384 hash of.
     * @return The SHA-384 hash of the specified file, which is represented as a hex string.
     * @throws Exception
     */
    private static String computeHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        try (InputStream in = new FileInputStream(file);
                DigestInputStream digestIn = new DigestInputStream(in, digest)) {
            byte[] buf = new byte[8192];
            while (digestIn.read(buf) >= 0)
                ; // just read entire file
            byte[] hashBytes = digest.digest();
            return Hex.encodeHexString(hashBytes);
        } catch (Exception e) {
            throw new Exception("An error occurred computing the hash of file "
                    + file.getAbsolutePath(), e);
        }
    }
}
