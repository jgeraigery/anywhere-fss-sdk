/*
 * ========================================================================
 *
 * Copyright (c) by Hitachi Data Systems, 2016. All rights reserved.
 *
 * ========================================================================
 */

package com.hds.hcpaw.fss.api;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hds.hcpaw.fss.api.exception.AnywhereException;
import com.hds.hcpaw.fss.api.exception.AwUnsupportedApiVersionException;
import com.hds.hcpaw.fss.api.model.AccountActivity;
import com.hds.hcpaw.fss.api.model.AuthToken;
import com.hds.hcpaw.fss.api.model.ClientListing;
import com.hds.hcpaw.fss.api.model.CollaborationActivity;
import com.hds.hcpaw.fss.api.model.CollaborationActivityRequest.CollaborationActivityType;
import com.hds.hcpaw.fss.api.model.Entry;
import com.hds.hcpaw.fss.api.model.FileVersionListing;
import com.hds.hcpaw.fss.api.model.FilesActivity;
import com.hds.hcpaw.fss.api.model.FilesActivityRequest.FilesActivityType;
import com.hds.hcpaw.fss.api.model.FilesystemListing;
import com.hds.hcpaw.fss.api.model.FolderListing;
import com.hds.hcpaw.fss.api.model.InvitationListing;
import com.hds.hcpaw.fss.api.model.Link;
import com.hds.hcpaw.fss.api.model.LinkAccessListing;
import com.hds.hcpaw.fss.api.model.LinkBrowseListing;
import com.hds.hcpaw.fss.api.model.LinkPermission;
import com.hds.hcpaw.fss.api.model.PageAction;
import com.hds.hcpaw.fss.api.model.PathSearchResult;
import com.hds.hcpaw.fss.api.model.ProviderListing;
import com.hds.hcpaw.fss.api.model.ReadAccessListing;
import com.hds.hcpaw.fss.api.model.ShareMemberListing;
import com.hds.hcpaw.fss.api.model.ShareRole;
import com.hds.hcpaw.fss.api.model.SharedFolder;
import com.hds.hcpaw.fss.api.model.SharedFolderListing;
import com.hds.hcpaw.fss.api.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
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
        FOLDER_LIST_AT_TIME("listFolderAtTime",
                "Lists contents of a folder at a given time, one page at a time"),
        FOLDER_LIST_INCLUDE_DELETED("listFolderIncludeDeleted",
                "Lists contents of a folder including deleted items, one page at a time"),
        FOLDER_RESTORE("restoreFolder", "Restore a folder"),
        FOLDER_GET_SIZE("getFolderSize", "Gets the size of a folder"),
        PATH_MOVE("move", "Moves a file or folder"),
        PATH_DELETE("delete", "Deletes a file or folder"),
        PATH_INFO("pathInfo", "Gets information (metadata) about a file or folder"),
        PATH_SEARCH("pathSearch",
                "Searches for files and folders whose name contains a specified substring"),
        LIST_LINKS_FOR_PATH("listLinksForPath", "List all non-expired links for a specified path"),
        FILE_CREATE("createFile", "Creates (uploads a new) a file or folder"),
        FILE_UPDATE("updateFile", "Updates (uploads a new version of) a file or folder"),
        FILE_READ("readFile", "Downloads a file or folder"),
        FILE_RESTORE("restoreFile", "Restore a file that has been deleted"),
        LINK_CREATE("createLink", "Creates a link to a file or folder"),
        LINK_DELETE("deleteLink", "Deletes a link"),
        LINK_UPDATE("updateLink", "Updates an existing link"),
        LINK_LIST("listLinks", "Lists user's links"),
        LINK_BROWSE("linkBrowse", "Browse a link"),
        LINK_PATH_METADATA("linkPathMetadata", "Get metadata of entry through link"),
        LINK_READ_FILE("linkReadFile", "Read a file through link"),
        LINK_UPDATE_FILE("linkUpdateFile", "Update a file through link"),
        LINK_COPY_TO_LOCAL("linkCopyToLocal", "Copy files from link to local"),
        USER_INFO("userInfo", "Gets information about the authenticated user"),
        USER_SETTINGS_UPDATE("userSettingsUpdate", "Update user's account settings"),
        PROVIDER_LIST("listProviders", "Lists the authentication providers"),
        SHARE_CREATE("createShare", "Creates a shared folder"),
        INVITE("invite", "Invite users/dlist to a shared folder"),
        LIST_GROUP_MEMBERS("listGroupMembers", "List members of a group"),
        RENAME_CLIENT("renameClient", "Rename a client"),
        DEREGISTER_CLIENT("deregisterClient", "Deregister a client"),
        LIST_CLIENTS("listClients", "List clients"),
        CLEAR_CLIENT_CREDENTIALS("clearClientCredentials", "Clear a client's credentials"),
        LIST_INVITATIONS("listInvitations", "List invitations"),
        ACCEPT_INVITATION("acceptInvitation", "Accept an invitation"),
        REJECT_INVITATION("rejectInvitation", "Reject an Invitation"),
        LEAVE_SHARE("leaveShare", "Leave share for user"),
        UNSHARE_FOLDER("unshareFolder", "Unshare a user's shared folder"),
        REMOVE_MEMBER("removeMember", "Remove member of a shared folder"),
        CREATE_TEAM_FOLDER("createTeamFolder", "Create a team folder"),
        CONVERT_TO_TEAM_FOLDER("convertToTeamFolder", "Convert a shared folder to a team folder"),
        UPDATE_SHARE_SETTINGS("updateShareSettings", "Update setting of a shared folder"),
        CANCEL_TEAM_FOLDER_REQ("cancelTeamFolderRequest", "Cancel a team folder request"),
        APPROVE_TEAM_FOLDER_REQ("approveTeamFolderRequest", "Approve a team folder request"),
        UPDATE_TEAM_FOLDER("updateTeamFolder", "Update a team folder"),
        LIST_SHARED_FOLDERS("listShares",
                "List all shared folders the authenticated user belongs to"),
        LIST_VERSIONS("listVersions", "List all the versions of a specified file"),
        LIST_READ_HISTORY("listReadHistory", "List all the read activity for this file"),
        LIST_LINK_READ_HISTORY("listLinkReadHistory",
                "List all the link read activity for this file"),
        PROMOTE_VERSION("promoteVersion", "Promote a version of a file to the current version"),
        FILES_ACTIVITY("filesActivity", "Get files activity of a filesystem"),
        COLLABORATION_ACTIVITY("collaborationActivity", "Get collaboration activity"),
        ACCOUNT_ACTIVITY("accountActivity", "Get account activity"),
        LIST_CONFLICTS("listConflicts", "List conflicts"),
        CANCEL_INVITATION("cancelInvitation", "Cancel an invitation"),
        DELETE_TEAM_FOLDER("deleteTeamFolder", "Delete a team folder"),
        LIST_FILESYSTEMS("listFilesystems", "List the filesystems of the authenticated user."),
        SYNC_MEMBERSHIP("syncMembership", "Sync the membership of a shared folder"),
        PRESERVE_MEMBERSHIP("preserveMembership", "Preserve the membership of a shared folder"),
        LIST_SHARE_GROUPS("listShareGroups", "List the groups that are members of a shared folder"),
        LIST_SHARE_MEMBERS("listShareMembers", "List the members of a shared folder"),
        REMOVE_GROUP("removeGroup", "Remove a group from a share"),
        SEARCH_INVITE("searchInvite", "Search for users and groups to invite"),
        LIST_RESTORE_POINTS("listRestorePoints", "List restore points for a user");

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
     * @param args Run without arguments to see a list of required arguments.
     */
    public static void main(String[] args) {
        try {
            if (args.length < 4) {
                System.out.println("Usage: AwSdkTest <server> <port> <username> <password>");
                System.exit(-1);
            }

            try (Scanner scanner = new Scanner(System.in)) {

                AnywhereAPI api = new AnywhereAPI.Builder("https://" + args[0] + ":" + args[1])
                        .insecureSSL().skipHostnameVerification().build();
                AuthToken authToken;
                try {
                    authToken = api.authenticate(args[2], args[3]);
                } catch (AnywhereException e) {
                    throw e;
                } catch (Exception e) {
                    String technicalMessage = (e instanceof AnywhereException
                            ? ((AnywhereException) e).getTechnicalMessage() : e.getMessage());
                    throw new AnywhereException(technicalMessage,
                            String.format("An error occurred authenticating user %s with HCP AW.  "
                                    + "Make sure that the user belongs to a profile with FSS API access.",
                                          args[2]),
                            e);
                }

                AnywhereFolderAPI folderApi = api.getFolderAPI();
                AnywherePathAPI pathApi = api.getPathAPI();
                AnywhereFileAPI fileApi = api.getFileAPI();
                AnywhereLinkAPI linkApi = api.getLinkAPI();
                AnywhereUserAPI userApi = api.getUserAPI();
                AnywhereProviderAPI providerApi = api.getProviderAPI();
                AnywhereShareAPI shareApi = api.getShareAPI();
                AnywhereClientAPI clientApi = api.getClientAPI();
                AnywhereActivityAPI activityApi = api.getActivityAPI();

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
                            case FOLDER_LIST_AT_TIME:
                                listFolderAtTime(scanner, authToken, folderApi);
                                break;
                            case FOLDER_LIST_INCLUDE_DELETED:
                                listFolderIncludeDeleted(scanner, authToken, folderApi);
                                break;
                            case FOLDER_RESTORE:
                                restoreFolder(scanner, authToken, folderApi);
                                break;
                            case FOLDER_GET_SIZE:
                                getFolderSize(scanner, authToken, folderApi);
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
                            case LIST_LINKS_FOR_PATH:
                                listLinksForPath(scanner, authToken, pathApi);
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
                            case FILE_RESTORE:
                                restoreFile(scanner, authToken, fileApi);
                                break;
                            case LINK_CREATE:
                                createLink(scanner, authToken, linkApi);
                                break;
                            case LINK_DELETE:
                                deleteLink(scanner, authToken, linkApi);
                                break;
                            case LINK_UPDATE:
                                updateLink(scanner, authToken, linkApi);
                                break;
                            case LINK_LIST:
                                listLinks(scanner, authToken, linkApi);
                                break;
                            case LINK_BROWSE:
                                linkBrowse(scanner, authToken, linkApi);
                                break;
                            case LINK_PATH_METADATA:
                                linkPathMetadata(scanner, authToken, linkApi);
                                break;
                            case LINK_READ_FILE:
                                linkReadFile(scanner, authToken, linkApi);
                                break;
                            case LINK_UPDATE_FILE:
                                linkUpdateFile(scanner, authToken, linkApi);
                                break;
                            case LINK_COPY_TO_LOCAL:
                                linkCopyToLocal(scanner, authToken, linkApi);
                            case USER_INFO:
                                getUserInfo(authToken, userApi);
                                break;
                            case USER_SETTINGS_UPDATE:
                                updateUserSettings(scanner, authToken, userApi);
                                break;
                            case PROVIDER_LIST:
                                listProviders(scanner, providerApi);
                                break;
                            case SHARE_CREATE:
                                createShare(scanner, authToken, shareApi);
                                break;
                            case INVITE:
                                invite(scanner, authToken, shareApi);
                                break;
                            case LIST_GROUP_MEMBERS:
                                listGroupMembers(scanner, authToken, shareApi);
                                break;
                            case RENAME_CLIENT:
                                renameClient(scanner, authToken, clientApi);
                                break;
                            case DEREGISTER_CLIENT:
                                deregisterClient(scanner, authToken, clientApi);
                                break;
                            case LIST_CLIENTS:
                                listClients(scanner, authToken, clientApi);
                                break;
                            case CLEAR_CLIENT_CREDENTIALS:
                                clearClientCredentials(scanner, authToken, clientApi);
                                break;
                            case LIST_INVITATIONS:
                                listInvitations(scanner, authToken, shareApi);
                                break;
                            case ACCEPT_INVITATION:
                                acceptInvitation(scanner, authToken, shareApi);
                                break;
                            case REJECT_INVITATION:
                                rejectInvitation(scanner, authToken, shareApi);
                                break;
                            case LEAVE_SHARE:
                                leaveShare(scanner, authToken, shareApi);
                                break;
                            case UNSHARE_FOLDER:
                                unshareFolder(scanner, authToken, shareApi);
                                break;
                            case REMOVE_MEMBER:
                                removeMember(scanner, authToken, shareApi);
                                break;
                            case CREATE_TEAM_FOLDER:
                                createTeamFolder(scanner, authToken, shareApi);
                                break;
                            case CONVERT_TO_TEAM_FOLDER:
                                convertToTeamFolder(scanner, authToken, shareApi);
                                break;
                            case UPDATE_SHARE_SETTINGS:
                                updateShareSettings(scanner, authToken, shareApi);
                                break;
                            case CANCEL_TEAM_FOLDER_REQ:
                                cancelTeamFolderRequest(scanner, authToken, shareApi);
                                break;
                            case APPROVE_TEAM_FOLDER_REQ:
                                approveTeamFolderRequest(scanner, authToken, shareApi);
                                break;
                            case UPDATE_TEAM_FOLDER:
                                updateTeamFolder(scanner, authToken, shareApi);
                                break;
                            case LIST_SHARED_FOLDERS:
                                listSharedFolders(scanner, authToken, shareApi);
                                break;
                            case LIST_VERSIONS:
                                listVersions(scanner, authToken, fileApi);
                                break;
                            case LIST_READ_HISTORY:
                                listReadHistory(scanner, authToken, fileApi);
                                break;
                            case LIST_LINK_READ_HISTORY:
                                listLinkReadHistory(scanner, authToken, fileApi);
                                break;
                            case PROMOTE_VERSION:
                                promoteVersion(scanner, authToken, fileApi);
                                break;
                            case FILES_ACTIVITY:
                                getFilesActivity(scanner, authToken, activityApi);
                                break;
                            case COLLABORATION_ACTIVITY:
                                getCollaborationActivity(scanner, authToken, activityApi);
                                break;
                            case ACCOUNT_ACTIVITY:
                                getAccountActivity(scanner, authToken, activityApi);
                                break;
                            case LIST_CONFLICTS:
                                listConflicts(scanner, authToken, fileApi);
                                break;
                            case CANCEL_INVITATION:
                                cancelInvitation(scanner, authToken, shareApi);
                                break;
                            case DELETE_TEAM_FOLDER:
                                deleteTeamFolder(scanner, authToken, shareApi);
                                break;
                            case LIST_FILESYSTEMS:
                                listFilesystems(scanner, authToken, userApi);
                                break;
                            case SYNC_MEMBERSHIP:
                                syncMembership(scanner, authToken, shareApi);
                                break;
                            case PRESERVE_MEMBERSHIP:
                                preserveMembership(scanner, authToken, shareApi);
                                break;
                            case LIST_SHARE_MEMBERS:
                                listShareMembers(scanner, authToken, shareApi);
                                break;
                            case LIST_SHARE_GROUPS:
                                listShareGroups(scanner, authToken, shareApi);
                                break;
                            case REMOVE_GROUP:
                                removeGroup(scanner, authToken, shareApi);
                                break;
                            case SEARCH_INVITE:
                                searchInvite(scanner, authToken, shareApi);
                                break;
                            case LIST_RESTORE_POINTS:
                                listRestorePoints(scanner, authToken, userApi);
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
     * List the user's filesystems.
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param userApi API for performing user-based operations.
     * @throws AwUnsupportedApiVersionException
     * @throws IOException
     * @throws AnywhereException
     */
    private static void listFilesystems(Scanner scanner, AuthToken authToken,
                                        AnywhereUserAPI userApi)
            throws AwUnsupportedApiVersionException, IOException, AnywhereException {
        FilesystemListing listing = userApi.listFilesystems(authToken);
        printObject("Success! Resulting filesystem listing: ", listing);
    }

    /**
     * Creates a folder.
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param folderApi API for performing folder operations.
     * @throws Exception
     */
    private static void createFolder(Scanner scanner, AuthToken authToken,
                                     AnywhereFolderAPI folderApi)
            throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        boolean createParents = nextBoolean(scanner, "Create parents?  Enter true or false");
        Entry entry = folderApi.create(authToken, path, createParents);
        printObject("Success!  Resulting entry: ", entry);
    }

    /**
     * Lists the contents of a folder.
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param folderApi API for performing folder operations.
     * @throws Exception
     */
    private static void listFolder(Scanner scanner, AuthToken authToken,
                                   AnywhereFolderAPI folderApi)
            throws Exception {
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
                    boolean listMore = nextBoolean(scanner,
                                                   "List more entries?  Enter true or false");
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
     * Lists the contents of a folder at a time.
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param folderApi API for performing folder operations.
     * @throws Exception
     */
    private static void listFolderAtTime(Scanner scanner, AuthToken authToken,
                                         AnywhereFolderAPI folderApi)
            throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        int pageSize = nextInt(scanner, "Enter page size");
        long timestamp = nextLong(scanner, "Enter point in time");
        String pageToken = null;

        do {
            FolderListing folderListing = folderApi.listEntriesAtTime(authToken, path, timestamp,
                                                                      pageToken, pageSize);
            if (folderListing != null) {
                System.out.println(folderListing.toString());
                pageToken = folderListing.getPageToken();

                if (pageToken != null) {
                    boolean listMore = nextBoolean(scanner,
                                                   "List more entries?  Enter true or false");
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
     * Lists the contents of a folder, including deleted items
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param folderApi API for performing folder operations.
     * @throws Exception
     */
    private static void listFolderIncludeDeleted(Scanner scanner, AuthToken authToken,
                                                 AnywhereFolderAPI folderApi)
            throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        int pageSize = nextInt(scanner, "Enter page size");
        String pageToken = null;

        do {
            FolderListing folderListing = folderApi.listEntriesIncludeDeleted(authToken, path,
                                                                              pageToken, pageSize);
            if (folderListing != null) {
                System.out.println(folderListing.toString());
                pageToken = folderListing.getPageToken();

                if (pageToken != null) {
                    boolean listMore = nextBoolean(scanner,
                                                   "List more entries?  Enter true or false");
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
     * Restore a folder
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param folderApi API for performing folder operations.
     * @throws Exception
     */
    private static void restoreFolder(Scanner scanner, AuthToken authToken,
                                      AnywhereFolderAPI folderApi)
            throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        int deltaSeconds = nextInt(scanner, "Enter delta in seconds");

        System.out.println(folderApi.restore(authToken, path, deltaSeconds).toString());
    }

    /**
     * Get the size of a folder
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param folderApi API for performing folder operations.
     * @throws Exception
     */
    private static void getFolderSize(Scanner scanner, AuthToken authToken,
                                      AnywhereFolderAPI folderApi)
            throws Exception {
        String path = nextString(scanner, "Enter the folder path");
        long pointInTime = nextLong(scanner, "Enter a pointInTime in milliseconds");

        System.out.println(folderApi.getSize(authToken, path, pointInTime).getSize());
    }

    /**
     * Deletes a file or folder.
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param pathApi API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void deletePath(Scanner scanner, AuthToken authToken, AnywherePathAPI pathApi)
            throws Exception {
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
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param pathApi API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void movePath(Scanner scanner, AuthToken authToken, AnywherePathAPI pathApi)
            throws Exception {
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
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param pathApi API for performing path (file or folder) operations.
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
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param pathApi API for performing path (file or folder) operations.
     * @throws Exception
     */
    private static void searchPath(Scanner scanner, AuthToken authToken, AnywherePathAPI pathApi)
            throws Exception {
        String prefix = nextString(scanner,
                                   "Enter a string that matching file or folder names must contain");
        String path = nextString(scanner, "Enter a path to search under");
        int maxResults = nextInt(scanner,
                                 "Enter the maximum number of matching files and folders to return");

        PathSearchResult result = pathApi.search(authToken, path, prefix, maxResults);
        System.out.println(result.toString());
    }

    /**
     * List of existing links for the path
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing thre request
     * @param pathApi API for performing path (file or folder) operations.
     * @throws AwUnsupportedApiVersionException
     * @throws IOException
     * @throws AnywhereException
     */
    private static void listLinksForPath(Scanner scanner, AuthToken authToken,
                                         AnywherePathAPI pathApi)
            throws AwUnsupportedApiVersionException, IOException, AnywhereException {
        String path = nextString(scanner, "Enter the path");
        System.out.println("List of existing links for the path: "
                + pathApi.listLinks(authToken, path));
    }

    /**
     * Uploads a new or modified file.
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param fileApi API for performing file operations.
     * @param isNewFile True indicates that no file currently exists at the specified path. False
     *            indicates that there the file already exists at the specified path.
     * @throws Exception
     */
    private static void uploadFile(Scanner scanner, AuthToken authToken, AnywhereFileAPI fileApi,
                                   boolean isNewFile)
            throws Exception {
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
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param fileApi API for performing file operations.
     * @throws Exception
     */
    private static void downloadFile(Scanner scanner, AuthToken authToken, AnywhereFileAPI fileApi)
            throws Exception {
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
     * Restore a file that has been deleted
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing file operations
     * @throws Exception
     */
    private static void restoreFile(Scanner scanner, AuthToken authToken, AnywhereFileAPI fileApi)
            throws Exception {
        String path = nextString(scanner, "Enter the file path");
        Entry entry = fileApi.restore(authToken, path);
        System.out.println("Successfully restored file " + path);
        System.out.println("Entry: " + entry);
    }

    /**
     * Creates a shared link
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void createLink(Scanner scanner, AuthToken authToken, AnywhereLinkAPI linkApi)
            throws Exception {
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

    /**
     * Updates a shared link
     *
     * @param scanner User input is read from this scanner.
     * @param authtoken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void updateLink(Scanner scanner, AuthToken authtoken, AnywhereLinkAPI linkApi)
            throws Exception {
        String path = nextString(scanner,
                                 "Enter the path to the file or folder of the shared link");
        String url = nextString(scanner, "Enter the url of the shared link");
        boolean newAccessCode = nextBoolean(scanner,
                                            "Update link with a new access code? Enter true or false");
        long expirationDate = nextLong(scanner,
                                       "Enter the new expiration date (epoch time in millis).");
        Link link = linkApi.update(authtoken, path, url, newAccessCode, expirationDate);
        printObject("Successfully updated link: ", link);
    }

    /**
     * Deletes a shared link
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void deleteLink(Scanner scanner, AuthToken authToken, AnywhereLinkAPI linkApi)
            throws Exception {
        String path = nextString(scanner,
                                 "Enter the path to the file or folder of the shared link");
        String url = nextString(scanner, "Enter the url of the shared link");
        linkApi.delete(authToken, path, url);
        System.out.println("Successfully deleted link.");
    }

    /**
     * Lists a user's links
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void listLinks(Scanner scanner, AuthToken authToken, AnywhereLinkAPI linkApi)
            throws Exception {
        System.out.println("List of links: " + linkApi.list(authToken));
    }

    /**
     * Browse a link
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void linkBrowse(Scanner scanner, AuthToken authToken, AnywhereLinkAPI linkApi)
            throws Exception {
        Boolean publicLink = nextBoolean(scanner, "Is the link a public link? Enter true or false");
        String linkToken = nextString(scanner, "Enter the link token of the link");
        String itemName = nextString(scanner, "Enter the item name of the link");
        Boolean hasAccessCode = nextBoolean(scanner,
                                            "Does the link have an access code? Enter true or false");
        String accessCode = null;
        if (hasAccessCode) {
            accessCode = nextString(scanner, "Enter the access code of the link");
        }
        String path = nextString(scanner, "Enter the path relative to the link");
        String pageToken = null;
        String answer = null;
        do {
            answer = nextString(scanner, "Use page token? [y/n]").toLowerCase();
        } while (!answer.equals("y") && !answer.equals("n"));
        if (answer.equals("y")) {
            pageToken = nextString(scanner, "Enter page token");
        }
        int pageSize = nextInt(scanner, "Enter page size");
        LinkBrowseListing linkBrowseListing;
        if (publicLink) {
            linkBrowseListing = linkApi.browsePublicLink(authToken, linkToken, itemName, accessCode,
                                                         path, pageToken, pageSize, null, null);
        } else {
            linkBrowseListing = linkApi.browsePrivateLink(authToken, linkToken, itemName,
                                                          accessCode, path, pageToken, pageSize,
                                                          null, null);
        }
        System.out.println("Successfully invited users: " + linkBrowseListing);
    }

    /**
     * Get metadata of entry through link
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void linkPathMetadata(Scanner scanner, AuthToken authToken,
                                         AnywhereLinkAPI linkApi)
            throws Exception {
        Boolean publicLink = nextBoolean(scanner, "Is the link a public link? Enter true or false");
        String linkToken = nextString(scanner, "Enter the link token of the link");
        String itemName = nextString(scanner, "Enter the item name of the link");
        Boolean hasAccessCode = nextBoolean(scanner,
                                            "Does the link have an access code? Enter true or false");
        String accessCode = null;
        if (hasAccessCode) {
            accessCode = nextString(scanner, "Enter the access code of the link");
        }
        String path = nextString(scanner, "Enter the path relative to the link");
        Entry entryMetadata;
        if (publicLink) {
            entryMetadata = linkApi.getPublicLinkPathMetadata(authToken, linkToken, itemName,
                                                              accessCode, path);
        } else {
            entryMetadata = linkApi.getPrivateLinkPathMetadata(authToken, linkToken, itemName,
                                                               accessCode, path);
        }
        System.out.println("Successfully retrieved entry metadata: " + entryMetadata);
    }

    /**
     * Read file through link
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void linkReadFile(Scanner scanner, AuthToken authToken, AnywhereLinkAPI linkApi)
            throws Exception {
        Boolean publicLink = nextBoolean(scanner, "Is the link a public link? Enter true or false");
        String linkToken = nextString(scanner, "Enter the link token of the link");
        String itemName = nextString(scanner, "Enter the item name of the link");
        Boolean hasAccessCode = nextBoolean(scanner,
                                            "Does the link have an access code? Enter true or false");
        String accessCode = null;
        if (hasAccessCode) {
            accessCode = nextString(scanner, "Enter the access code of the link");
        }
        String path = nextString(scanner, "Enter the path (including file name)");
        Boolean hasEtag = nextBoolean(scanner,
                                      "Does the file has a known etag ? Enter true or false");
        String etag = null;
        if (hasEtag) {
            etag = nextString(scanner, "Enter the etag of the file");
        }
        String destPath = nextString(scanner,
                                     "Enter the local path (including file name) to download into");

        File f = new File(destPath);
        try (OutputStream out = new FileOutputStream(f)) {
            Entry entry;
            if (publicLink) {
                entry = linkApi.readFilePublicLink(authToken, linkToken, itemName, accessCode, path,
                                                   etag, out);
            } else {
                entry = linkApi.readFilePrivateLink(authToken, linkToken, itemName, accessCode,
                                                    path, etag, out);
            }
            if (entry == null) {
                System.out.println("File unchanged; nothing downloaded");
            } else {
                printObject("Successfully downloaded entry: ", entry);
            }
        }
    }

    /**
     * Update file through link
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void linkUpdateFile(Scanner scanner, AuthToken authToken,
                                       AnywhereLinkAPI linkApi)
            throws Exception {
        Boolean publicLink = nextBoolean(scanner, "Is the link a public link? Enter true or false");
        String linkToken = nextString(scanner, "Enter the link token of the link");
        String itemName = nextString(scanner, "Enter the item name of the link");
        Boolean hasAccessCode = nextBoolean(scanner,
                                            "Does the link have an access code? Enter true or false");
        String accessCode = null;
        if (hasAccessCode) {
            accessCode = nextString(scanner, "Enter the access code of the link");
        }
        String sourcePath = nextString(scanner,
                                       "Enter the path (including file name) of the local file");
        String destPath = nextString(scanner,
                                     "Enter the remote destination path (including file name)");
        String etag = nextString(scanner, "Enter the etag for the destination file");

        System.out.print("Computing size and hash of local file, please wait ...");
        System.out.flush();
        File f = new File(sourcePath);
        long size = f.length();
        String hash = computeHash(f);
        System.out.println(" Done computing size and hash");

        try (InputStream in = new FileInputStream(f)) {
            Entry entry;
            if (publicLink) {
                entry = linkApi.updateFilePublicLink(authToken, linkToken, itemName, accessCode,
                                                     destPath, size, hash, etag, in);
            } else {
                entry = linkApi.updateFilePrivateLink(authToken, linkToken, itemName, accessCode,
                                                      destPath, size, hash, etag, in);
            }
            printObject("Success!  Resulting entry: ", entry);
        }
    }

    /**
     * Copy files from link to local
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param linkApi API for performing link operations.
     * @throws Exception
     */
    private static void linkCopyToLocal(Scanner scanner, AuthToken authToken,
                                        AnywhereLinkAPI linkApi)
            throws Exception {
        Boolean publicLink = nextBoolean(scanner, "Is the link a public link? Enter true or false");
        String linkToken = nextString(scanner, "Enter the link token of the link");
        String itemName = nextString(scanner, "Enter the item name of the link");
        Boolean hasAccessCode = nextBoolean(scanner,
                                            "Does the link have an access code? Enter true or false");
        String accessCode = null;
        if (hasAccessCode) {
            accessCode = nextString(scanner, "Enter the access code of the link");
        }
        List<String> pathList = Lists.newArrayList();
        boolean pathListHasNext = true;
        while (pathListHasNext) {
            String path = nextString(scanner,
                                     "Enter the path relative to the link to be copied to local");
            pathList.add(path);
            pathListHasNext = nextBoolean(scanner,
                                          "Is there another path to be copied to local? Enter true of false");
        }
        String destinationPath = nextString(scanner, "Enter the destination to copy to");
        if (publicLink) {
            linkApi.copyFilesToLocalPublicLink(authToken, linkToken, itemName, accessCode, pathList,
                                               destinationPath);
        }
        System.out.println("Successfully copied to local.");
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
     * @param authToken Authentication token for the user performing the request.
     * @param userApi API for performing user operations.
     * @throws Exception
     */
    private static void getUserInfo(AuthToken authToken, AnywhereUserAPI userApi) throws Exception {
        User user = userApi.getInfo(authToken);
        System.out.println("Success!  User info: " + user.toString());
    }

    private static void updateUserSettings(Scanner scanner, AuthToken authToken,
                                           AnywhereUserAPI userApi)
            throws Exception {
        String email = nextString(scanner, "Enter the email");
        boolean emailQuota = nextBoolean(scanner,
                                         "Send email when quota is nearly used up? Enter true or false");
        boolean emailDevices = nextBoolean(scanner,
                                           "Send email when another device has joined the account? Enter true or false");
        boolean emailAuth = nextBoolean(scanner,
                                        "Send email if there are authentication failures? Enter true or false");
        boolean emailShareFile = nextBoolean(scanner,
                                             "Send email if files and folders shared via links are accessed? Enter true or false");
        boolean emailUpload = nextBoolean(scanner,
                                          "Send email if there are files uploaded to folders shared via links? Enter true or false");
        boolean emailShareFolder = nextBoolean(scanner,
                                               "Send email if is invited to a shared folder? Enter true or false");
        String language = nextString(scanner, "Language used by the user");
        User user = userApi.updateSetting(authToken, email, emailQuota, emailDevices, emailAuth,
                                          emailShareFile, emailUpload, emailShareFolder, language);
        System.out.println("Successfully updated user settings!  User info: " + user.toString());
    }

    /**
     * List the authentication providers
     *
     * @param scanner User input is read from this scanner.
     * @param providerApi API for performing provider operations
     * @throws Exception
     */
    private static void listProviders(Scanner scanner, AnywhereProviderAPI providerApi)
            throws Exception {
        ProviderListing providers = providerApi.listProviders();
        if (providers != null) {
            System.out.println("List of providers: " + providers.toString());
        }
    }

    /**
     * Create share folder
     *
     * @param scanner User input is read from this scanner.
     * @param authToken Authentication token for the user performing the request.
     * @param shareApi API for performing share operations.
     * @throws Exception
     */
    private static void createShare(Scanner scanner, AuthToken authToken, AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the path");
        String label = nextString(scanner, "Enther the label");
        System.out
                .println("Successfully shared folder: " + shareApi.create(authToken, path, label));
    }

    /**
     * Invite users to a shared folder
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void invite(Scanner scanner, AuthToken authToken, AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the path");
        String uniqueId = nextString(scanner, "Enter the invitee's uniqueId");
        String providerId = nextString(scanner, "Enter the invitee's providerId");
        String role = nextString(scanner, "Enter the invitee's role, (VIEWER or COLLABORATOR)");
        System.out.println("Successfully invited to shared folder: "
                + shareApi.invite(authToken, path, uniqueId, providerId, ShareRole.valueOf(role)));
    }

    /**
     * List members in a group
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void listGroupMembers(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String name = nextString(scanner, "Enter the name");
        String providerId = nextString(scanner, "Enter the providerId");
        System.out.println("Successfully listed group members: "
                + shareApi.listGroupMembers(authToken, name, providerId));
    }

    /**
     * Rename a client
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing thre request
     * @param clientApi API for performing client operations
     * @throws Exception
     */
    private static void renameClient(Scanner scanner, AuthToken authToken,
                                     AnywhereClientAPI clientApi)
            throws Exception {
        String id = nextString(scanner, "Enter the id");
        String name = nextString(scanner, "Enter the name");
        System.out.println("Successfully renamed client: " + clientApi.rename(authToken, id, name));
    }

    /**
     * Deregister a client
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param clientApi API for performing client operations
     * @throws Exception
     */
    private static void deregisterClient(Scanner scanner, AuthToken authToken,
                                         AnywhereClientAPI clientApi)
            throws Exception {
        String id = nextString(scanner, "Enter the id");
        boolean wipe = nextBoolean(scanner, "Enter whether to wipe");
        System.out.println("Successfully deregistered client: "
                + clientApi.deregister(authToken, id, wipe));
    }

    /**
     * List clients
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing thre request
     * @param clientApi API for performing client operations
     * @throws Exception
     */
    private static void listClients(Scanner scanner, AuthToken authToken,
                                    AnywhereClientAPI clientApi)
            throws Exception {
        ClientListing listing = clientApi.list(authToken);
        System.out.println("List of clients: " + listing);
    }

    /**
     * Clear client credentials
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param clientApi API for performing client operations
     * @throws Exception
     */
    private static void clearClientCredentials(Scanner scanner, AuthToken authToken,
                                               AnywhereClientAPI clientApi)
            throws Exception {
        String id = nextString(scanner, "Enter the id");
        System.out.println("Successfully cleared client credentials: "
                + clientApi.clearCredentials(authToken, id));
    }

    /**
     * List invitations
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void listInvitations(Scanner scanner, AuthToken authToken,
                                        AnywhereShareAPI shareApi)
            throws Exception {
        InvitationListing listing = shareApi.listInvitations(authToken);
        System.out.println("List of invitations: " + listing);
    }

    /**
     * Accept an invitation
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void acceptInvitation(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String fsId = nextString(scanner, "Enter the share id");
        boolean sync = nextBoolean(scanner, "Enter whether to sync");
        SharedFolder folder = shareApi.acceptInvitation(authToken, fsId, sync);
        System.out.println("Shared folder: " + folder);
    }

    /**
     * Cancel an invitation.
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void cancelInvitation(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the share path to cancel the invite on.");
        String uniqueId = nextString(scanner,
                                     "Enter the uniqueId of the user whose invitation should be cancelled.");
        String providerId = nextString(scanner,
                                       "Enter the providerId of the user whose invitation should be cancelled.");
        shareApi.cancelInvitation(authToken, path, uniqueId, providerId);
        System.out.println("Successfully cancelled invitation.");
    }

    /**
     * Reject an invitation
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void rejectInvitation(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String fsId = nextString(scanner, "Enter the share id");
        shareApi.rejectInvitation(authToken, fsId);
        System.out.println("Successfully rejected invitation");
    }

    /**
     * Leave a share for a user
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void leaveShare(Scanner scanner, AuthToken authToken, AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the share path");
        shareApi.leaveShare(authToken, path);
        System.out.println("Successfully left: " + path);
    }

    /**
     * Leave a share for a user
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void unshareFolder(Scanner scanner, AuthToken authToken,
                                      AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the share path");
        Entry entry = shareApi.unshareFolder(authToken, path);
        System.out.println("Folder:" + entry);
    }

    /**
     * Remove member from a shared folder
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void removeMember(Scanner scanner, AuthToken authToken,
                                     AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the path");
        String uniqueId = nextString(scanner, "Enter the uniqueId of the user to be removed");
        String providerId = nextString(scanner, "Enter the providerId");
        shareApi.removeMember(authToken, path, uniqueId, providerId);
        System.out.println("Successfully removed member");
    }

    /**
     * Update settings of a shared folder
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void updateShareSettings(Scanner scanner, AuthToken authToken,
                                            AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the path");
        boolean sync = nextBoolean(scanner, "Enter whether to sync");
        SharedFolder folder = shareApi.updateShareSettings(authToken, path, sync);
        System.out.println("Shared folder: " + folder);
    }

    /**
     * Create a team folder
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void createTeamFolder(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the share path");
        String label = nextString(scanner, "Enter the label");
        String contact = nextString(scanner, "Enter the contact information");
        Long quotaBytes = nextLong(scanner, "Enter the quota in bytes");
        SharedFolder folder = shareApi.createTeamFolder(authToken, path, label, contact,
                                                        quotaBytes);
        System.out.println("Shared folder:" + folder);
    }

    /**
     * Convert a shared folder to a team folder
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void convertToTeamFolder(Scanner scanner, AuthToken authToken,
                                            AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the share path");
        String contact = nextString(scanner, "Enter the contact information");
        Long quotaBytes = nextLong(scanner, "Enter the quota in bytes");
        shareApi.convertToTeamFolder(authToken, path, contact, quotaBytes);
    }

    /**
     * Cancel a team folder request
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void cancelTeamFolderRequest(Scanner scanner, AuthToken authToken,
                                                AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the shared folder path");
        SharedFolder folder = shareApi.cancelTeamFolderRequest(authToken, path);
        System.out.println("Shared folder:" + folder);
    }

    /**
     * Approve a team folder request
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void approveTeamFolderRequest(Scanner scanner, AuthToken authToken,
                                                 AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the shared folder path");
        SharedFolder folder = shareApi.approveTeamFolderRequest(authToken, path);
        System.out.println("Shared folder:" + folder);
    }

    /**
     * Update a team folder
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void updateTeamFolder(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter the team folder path");
        String contact = null;
        Long quotaBytes = null;
        String answer = "";
        do {
            answer = nextString(scanner, "Update contact information? [y/n]");
        } while (!answer.toLowerCase().equals("y") && !answer.toLowerCase().equals("n"));
        if (answer.toLowerCase().equals("y")) {
            contact = nextString(scanner, "Enter the contact information");
        }
        answer = "";
        do {
            answer = nextString(scanner, "Update quota? [y/n]");
        } while (!answer.toLowerCase().equals("y") && !answer.toLowerCase().equals("n"));
        if (answer.toLowerCase().equals("y")) {
            quotaBytes = nextLong(scanner, "Enter the quota in bytes");
        }
        SharedFolder folder = shareApi.updateTeamFolder(authToken, path, contact, quotaBytes);
        System.out.println("Shared folder:" + folder);
    }

    /**
     * Delete a team folder, abandoning it.
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws AwUnsupportedApiVersionException
     * @throws IOException
     * @throws AnywhereException
     */
    public static void deleteTeamFolder(Scanner scanner, AuthToken authToken,
                                        AnywhereShareAPI shareApi)
            throws AwUnsupportedApiVersionException, IOException, AnywhereException {
        String path = nextString(scanner, "Enter the team folder path");
        shareApi.deleteTeamFolder(authToken, path);
        System.out.println("Successfully deleted the team folder.");
    }

    /**
     * List all shared folders the authenticated user belongs to.
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void listSharedFolders(Scanner scanner, AuthToken authToken,
                                          AnywhereShareAPI shareApi)
            throws Exception {
        SharedFolderListing shares = shareApi.listSharedFolders(authToken);
        System.out.println("Shared folder listing:" + shares);
    }

    /**
     * List all versions of a file
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing file operations
     * @throws Exception
     */
    private static void listVersions(Scanner scanner, AuthToken authToken, AnywhereFileAPI fileApi)
            throws Exception {
        String path = nextString(scanner, "Enter the file path");
        String type;
        do {
            type = nextString(scanner, "Private or shared").toLowerCase();
        } while (!type.equals("private") && !type.equals("shared"));
        Boolean showPrivate = type.equals("private");
        String pageToken = null;
        Integer pageSize = nextInt(scanner, "Enter the page size [0 for default]");
        String pageAction;
        do {
            pageAction = nextString(scanner, "Which entries? [first/last/default]").toUpperCase();
        } while (!pageAction.equals("FIRST") && !pageAction.equals("LAST")
                && !pageAction.equals("DEFAULT"));

        do {
            FileVersionListing versionListing = fileApi
                    .listVersions(authToken, path, showPrivate, pageToken,
                                  (pageSize == 0) ? null : pageSize, pageAction.equals("DEFAULT")
                                          ? null : PageAction.valueOf(pageAction));
            if (versionListing != null) {
                pageToken = versionListing.getPageToken();
                // If the token is null, there are no versions of the specified file
                if (pageToken == null) {
                    System.out.println("No entries");
                } else {
                    if (versionListing.getEntries().size() > 0) {
                        System.out.println(versionListing.toString());
                    } else {
                        System.out.println("No more entries in that direction");
                    }
                    boolean listMore = false;
                    String answer;
                    do {
                        answer = nextString(scanner, "List more entries? [y/n]").toLowerCase();
                    } while (!answer.equals("y") && !answer.equals("n"));
                    if (answer.equals("y")) {
                        listMore = true;
                        do {
                            pageAction = nextString(scanner,
                                                    "Which entries? [first/last/next/prev/default]")
                                                            .toUpperCase();
                        } while (!pageAction.equals("FIRST") && !pageAction.equals("LAST")
                                && !pageAction.equals("NEXT") && !pageAction.equals("PREV")
                                && !pageAction.equals("DEFAULT"));
                    }
                    if (!listMore) {
                        pageToken = null;
                    }
                }
            }
        } while (pageToken != null);
        System.out.flush();
    }

    /**
     * List all reads of a file
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing file operations
     * @throws Exception
     */
    private static void listReadHistory(Scanner scanner, AuthToken authToken,
                                        AnywhereFileAPI fileApi)
            throws Exception {
        String path = nextString(scanner, "Enter the file path");
        String type;
        do {
            type = nextString(scanner, "Private or shared?").toLowerCase();
        } while (!type.equals("private") && !type.equals("shared"));
        Boolean showPrivate = type.equals("private");
        String pageToken = null;
        Integer pageSize = nextInt(scanner, "Enter the page size [0 for default]");
        String pageAction;
        do {
            pageAction = nextString(scanner, "Which entries? [first/last/default]").toUpperCase();
        } while (!pageAction.equals("FIRST") && !pageAction.equals("LAST")
                && !pageAction.equals("DEFAULT"));

        do {
            ReadAccessListing versionListing = fileApi
                    .listReadHistory(authToken, path, showPrivate, pageToken,
                                     (pageSize == 0) ? null : pageSize, pageAction.equals("DEFAULT")
                                             ? null : PageAction.valueOf(pageAction));
            if (versionListing != null) {
                pageToken = versionListing.getPageToken();
                // If the token is null, there are no versions of the specified file
                if (pageToken == null) {
                    System.out.println("No entries");
                } else {
                    if (versionListing.getEntries().size() > 0) {
                        System.out.println(versionListing.toString());
                    } else {
                        System.out.println("No more entries in that direction");
                    }
                    boolean listMore = false;
                    String answer;
                    do {
                        answer = nextString(scanner, "List more entries? [y/n]").toLowerCase();
                    } while (!answer.equals("y") && !answer.equals("n"));
                    if (answer.equals("y")) {
                        listMore = true;
                        do {
                            pageAction = nextString(scanner,
                                                    "Which entries? [first/last/next/prev/default]")
                                                            .toUpperCase();
                        } while (!pageAction.equals("FIRST") && !pageAction.equals("LAST")
                                && !pageAction.equals("NEXT") && !pageAction.equals("PREV")
                                && !pageAction.equals("DEFAULT"));
                    }
                    if (!listMore) {
                        pageToken = null;
                    }
                }
            }
        } while (pageToken != null);
        System.out.flush();
    }

    /**
     * List all link reads of a file
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing file operations
     * @throws Exception
     */
    private static void listLinkReadHistory(Scanner scanner, AuthToken authToken,
                                            AnywhereFileAPI fileApi)
            throws Exception {
        String path = nextString(scanner, "Enter the file path");
        String type;
        do {
            type = nextString(scanner, "Private or shared?").toLowerCase();
        } while (!type.equals("private") && !type.equals("shared"));
        Boolean showPrivate = type.equals("private");
        String pageToken = null;
        Integer pageSize = nextInt(scanner, "Enter the page size [0 for default]");
        String pageAction;
        do {
            pageAction = nextString(scanner, "Which entries? [first/last/default]").toUpperCase();
        } while (!pageAction.equals("FIRST") && !pageAction.equals("LAST")
                && !pageAction.equals("DEFAULT"));

        do {
            LinkAccessListing versionListing = fileApi
                    .listLinkReadHistory(authToken, path, showPrivate, pageToken,
                                         (pageSize == 0) ? null : pageSize,
                                         pageAction.equals("DEFAULT") ? null
                                                 : PageAction.valueOf(pageAction));
            if (versionListing != null) {
                pageToken = versionListing.getPageToken();
                // If the token is null, there are no versions of the specified file
                if (pageToken == null) {
                    System.out.println("No entries");
                } else {
                    if (versionListing.getEntries().size() > 0) {
                        System.out.println(versionListing.toString());
                    } else {
                        System.out.println("No more entries in that direction");
                    }
                    boolean listMore = false;
                    String answer;
                    do {
                        answer = nextString(scanner, "List more entries? [y/n]").toLowerCase();
                    } while (!answer.equals("y") && !answer.equals("n"));
                    if (answer.equals("y")) {
                        listMore = true;
                        do {
                            pageAction = nextString(scanner,
                                                    "Which entries? [first/last/next/prev/default]")
                                                            .toUpperCase();
                        } while (!pageAction.equals("FIRST") && !pageAction.equals("LAST")
                                && !pageAction.equals("NEXT") && !pageAction.equals("PREV")
                                && !pageAction.equals("DEFAULT"));
                    }
                    if (!listMore) {
                        pageToken = null;
                    }
                }
            }
        } while (pageToken != null);
        System.out.flush();
    }

    /**
     * Promote a version of a file
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing file operations
     * @throws Exception
     */
    private static void promoteVersion(Scanner scanner, AuthToken authToken,
                                       AnywhereFileAPI fileApi)
            throws Exception {
        String path = nextString(scanner, "Enter the file path");
        String etag = nextString(scanner, "Enter the etag for the path");
        System.out.println(fileApi.promoteVersion(authToken, path, etag).toString());
    }

    /**
     * Get files activity of a filesystem
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param activityApi API for performing activity operations
     * @throws Exception
     */
    private static void getFilesActivity(Scanner scanner, AuthToken authToken,
                                         AnywhereActivityAPI activityApi)
            throws Exception {
        String pageToken = null;
        String fsId = null;
        FilesActivityType activityType = null;
        String answer = null;
        do {
            answer = nextString(scanner, "Use page token? [y/n]").toLowerCase();
        } while (!answer.equals("y") && !answer.equals("n"));
        if (answer.equals("y")) {
            pageToken = nextString(scanner, "Enter page token");
        } else {
            fsId = nextString(scanner, "Enter fsId");
            activityType = FilesActivityType
                    .valueOf(nextString(scanner, "Enter activity type [MODIFY/READ_LINK/READ]"));
        }
        int pageSize = nextInt(scanner, "Enter page size");
        PageAction pageAction = PageAction
                .valueOf(nextString(scanner, "Enter page action [FIRST/LAST/NEXT/PREV]"));
        FilesActivity filesActivity = activityApi.getFilesActivity(authToken, activityType, fsId,
                                                                   pageToken, pageSize, pageAction);
        System.out.println("Files activity:" + filesActivity);
    }

    /**
     * Get collaboration activity
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param activityApi API for performing activity operations
     * @throws Exception
     */
    private static void getCollaborationActivity(Scanner scanner, AuthToken authToken,
                                                 AnywhereActivityAPI activityApi)
            throws Exception {
        String pageToken = null;
        CollaborationActivityType activityType = null;
        String answer = null;
        do {
            answer = nextString(scanner, "Use page token? [y/n]").toLowerCase();
        } while (!answer.equals("y") && !answer.equals("n"));
        if (answer.equals("y")) {
            pageToken = nextString(scanner, "Enter page token");
        } else {
            activityType = CollaborationActivityType
                    .valueOf(nextString(scanner,
                                        "Enter activity type [SHARE/LINK/TEAM_MANAGEMENT]"));
        }
        int pageSize = nextInt(scanner, "Enter page size");
        PageAction pageAction = PageAction
                .valueOf(nextString(scanner, "Enter page action [FIRST/LAST/NEXT/PREV]"));
        CollaborationActivity collaborationActivity = activityApi
                .getCollaborationActivity(authToken, activityType, pageToken, pageSize, pageAction);
        System.out.println("Collaboration activity:" + collaborationActivity);
    }

    /**
     * Get account activity
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param activityApi API for performing activity operations
     * @throws Exception
     */
    private static void getAccountActivity(Scanner scanner, AuthToken authToken,
                                           AnywhereActivityAPI activityApi)
            throws Exception {
        String pageToken = null;
        String answer = null;
        do {
            answer = nextString(scanner, "Use page token? [y/n]").toLowerCase();
        } while (!answer.equals("y") && !answer.equals("n"));
        if (answer.equals("y")) {
            pageToken = nextString(scanner, "Enter page token");
        }
        int pageSize = nextInt(scanner, "Enter page size");
        PageAction pageAction = PageAction
                .valueOf(nextString(scanner, "Enter page action [FIRST/LAST/NEXT/PREV]"));
        AccountActivity accountActivity = activityApi.getAccountActivity(authToken, pageToken,
                                                                         pageSize, pageAction);
        System.out.println("Account activity:" + accountActivity);
    }

    /**
     * List conflicts
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param fileApi API for performing file operations
     * @throws Exception
     */
    private static void listConflicts(Scanner scanner, AuthToken authToken, AnywhereFileAPI fileApi)
            throws Exception {
        System.out.println(fileApi.listConflicts(authToken));
    }

    /**
     * Sync membership
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void syncMembership(Scanner scanner, AuthToken authToken,
                                       AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter share path");
        boolean increaseRole = nextBoolean(scanner, "Enter increase role");
        boolean inviteDeclined = nextBoolean(scanner, "Enter invite declined");
        SharedFolder result = shareApi.syncMembership(authToken, path, increaseRole,
                                                      inviteDeclined);
        System.out.println("Sync membership result: " + result);
    }

    /**
     * Preserve membership
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void preserveMembership(Scanner scanner, AuthToken authToken,
                                           AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter share path");
        boolean removeGroups = nextBoolean(scanner, "Enter remove groups");
        ShareMemberListing result = shareApi.preserveMembership(authToken, path, removeGroups);
        System.out.println("Preserve membership result: " + result);
    }

    /**
     * List members in a share
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void listShareMembers(Scanner scanner, AuthToken authToken,
                                         AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter share path");
        ShareMemberListing result = shareApi.listShareMembers(authToken, path);
        System.out.println(result);
    }

    /**
     * List groups in a share
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void listShareGroups(Scanner scanner, AuthToken authToken,
                                        AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter share path");
        ShareMemberListing result = shareApi.listShareGroups(authToken, path);
        System.out.println(result);
    }

    /**
     * Remove group from a share
     *
     * @param scanner User input is read from this scanner
     * @param authToken Authentication token for the user performing the request
     * @param shareApi API for performing share operations
     * @throws Exception
     */
    private static void removeGroup(Scanner scanner, AuthToken authToken, AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter share path");
        String uniqueId = nextString(scanner, "Enter the invitee's uniqueId");
        String providerId = nextString(scanner, "Enter the invitee's providerId");
        shareApi.removeGroup(authToken, path, uniqueId, providerId);
        System.out.println("Successfully removed group");
    }

    private static void searchInvite(Scanner scanner, AuthToken authToken,
                                     AnywhereShareAPI shareApi)
            throws Exception {
        String path = nextString(scanner, "Enter share path");
        String filter = nextString(scanner, "Enter filter");
        System.out.println(shareApi.searchInvite(authToken, path, filter));
    }

    private static void listRestorePoints(Scanner scanner, AuthToken authToken,
                                          AnywhereUserAPI userApi)
            throws Exception {
        System.out.println(userApi.listRestorePoints(authToken));
    }

    /**
     * Prints an object with a label to stdout.
     *
     * @param label Prints before the object.
     * @param obj Prints after the label.
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
     * Writes the prompt to stdout and then reads an integer from the scanner.
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
     * Writes the prompt to stdout and then reads a long from the scanner.
     *
     * @param scanner
     * @param prompt
     * @return
     */
    private static long nextLong(Scanner scanner, String prompt) {
        System.out.print(prompt + ": ");
        System.out.flush();
        return scanner.nextLong();
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
     * @param file The file which you want to compute the SHA-384 hash of.
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
            throw new Exception(
                    "An error occurred computing the hash of file " + file.getAbsolutePath(), e);
        }
    }
}
