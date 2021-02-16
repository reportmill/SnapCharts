package snapcharts.app;
import snap.util.JSONNode;
import snap.web.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A Class to work with DropBox.
 */
public class DropBox extends WebSite {

    // The Email for this DropBox
    private String  _email;

    // The Site path
    private String _sitePath;

    // Constants for DropBox endpoints
    private static final String GET_METADATA = "https://api.dropboxapi.com/2/files/get_metadata";
    private static final String LIST_FOLDER = "https://api.dropboxapi.com/2/files/list_folder";
    private static final String CREATE_FOLDER = "https://api.dropboxapi.com/2/files/create_folder_v2";
    private static final String DELETE = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String UPLOAD = "https://content.dropboxapi.com/2/files/upload";
    private static final String GET_CONTENT = "https://content.dropboxapi.com/2/files/download";

    // Header value
    private static String _atok = null;

    // Date format
    private static DateFormat _fmt = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    // Shared instance
    private static Map<String,DropBox> _dboxes = new HashMap<>();

    /**
     * Constructor.
     */
    private DropBox(String anEmail)
    {
        // Set ivars
        _email = anEmail;
        _sitePath = getPathForEmail(anEmail);

        // Create/set URL
        String DROPBOX_ROOT = "dbox://dbox.com";
        String urls = DROPBOX_ROOT + _sitePath;
        WebURL url = WebURL.getURL(urls);
        setURL(url);
    }

    /**
     * Returns the DropBox email.
     */
    public String getEmail()  { return _email; }

    /**
     * Handles getting file info, contents or directory files.
     */
    @Override
    protected void doGetOrHead(WebRequest aReq, WebResponse aResp, boolean isHead)
    {
        // Always do Head
        doHead(aReq, aResp);
        if (isHead)
            return;

        // If error, just return
        if (aResp.getCode()!=WebResponse.OK)
            return;

        // If directory, get files
        FileHeader fhdr = aResp.getFileHeader();
        if (fhdr.isDir())
            doGetDir(aReq, aResp);

        // Otherwise, get contents
        else doGetFileContents(aReq, aResp);
    }

    /**
     * Get Head for request.
     */
    protected void doHead(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(GET_METADATA);
        req.addHeader("Authorization", "Bearer " + _atok);
        req.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(req, false, "path", dboxPath);

        // Get HTTP Response
        HTTPResponse resp = getResponseHTTP(req, aResp);
        if (resp==null || resp.getCode()!=HTTPResponse.OK)
            return;

        // Get JSON response
        JSONNode json = resp.getJSON();
        if (json==null)
            return;

        // Get JSON response
        FileHeader fhdr = createFileHeaderForJSON(json);
        fhdr.setPath(aReq.getURL().getPath());
        aResp.setFileHeader(fhdr);
    }

    /**
     * Get Directory listing for request.
     */
    protected void doGetDir(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(LIST_FOLDER);
        req.addHeader("Authorization", "Bearer " + _atok);
        req.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(req, false, "path", dboxPath);

        // Get HTTP Response
        HTTPResponse resp = getResponseHTTP(req, aResp);
        if (resp==null || resp.getCode()!=HTTPResponse.OK)
            return;

        // Get JSON response
        JSONNode json = resp.getJSON();
        if (json==null)
            return;

        // Get json for entries
        JSONNode entriesNode = json.getNode("entries");
        List<JSONNode> entryNodes = entriesNode.getNodes();

        // Get FileHeader List for json entries
        List<FileHeader> fhdrs = getMappedList(entryNodes, e -> createFileHeaderForJSON(e));

        // Strip SitePath from FileHeaders
        for (FileHeader fhdr : fhdrs)
            fhdr.setPath(fhdr.getPath().substring(_sitePath.length()));

        // Set FileHeaders
        aResp.setFileHeaders(fhdrs);
    }

    /**
     * Get file request.
     */
    protected void doGetFileContents(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(GET_CONTENT);
        req.addHeader("Authorization", "Bearer " + _atok);

        // Add path param as JSON header
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(req, true, "path", dboxPath);

        // Get response
        getResponseHTTP(req, aResp);
    }

    /**
     * Handle a PUT request.
     */
    protected void doPut(WebRequest aReq, WebResponse aResp)
    {
        WebFile file = aReq.getFile();
        if (file.isFile())
            doPutFile(aReq, aResp);
        else doPutDir(aReq, aResp);
    }

    /**
     * Handle a PUT request.
     */
    protected void doPutFile(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(UPLOAD);
        req.addHeader("Authorization", "Bearer " + _atok);
        req.addHeader("Content-Type", "application/octet-stream");

        // Add path param as JSON header
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(req, true, "path", dboxPath, "mode", "overwrite");

        // Add bytes
        byte bytes[] = aReq.getSendBytes();
        req.setBytes(bytes);

        // Get HTTP Response
        HTTPResponse resp = getResponseHTTP(req, aResp);
        if (resp==null || resp.getCode()!=HTTPResponse.OK) {
            System.err.println("DropBox.putFile: " + (resp!=null ? resp.getMessage() : "null"));
            return;
        }

        // Get JSON response
        JSONNode json = resp.getJSON();
        if (json!=null) {
            String mod = json.getNodeString("server_modified");
            if (mod!=null && mod.endsWith("Z")) {
                try {
                    Date date = _fmt.parse(mod);
                    aResp.setModTime(date.getTime());
                    System.out.println("Save ModTime: " + date);
                }
                catch (Exception e) { System.err.println(e); }
            }
            else System.err.println("DropBox.doPutFile: Can't get save mod time: " + json);
        }
    }

    /**
     * Handle a PUT request.
     */
    protected void doPutDir(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(CREATE_FOLDER);
        req.addHeader("Authorization", "Bearer " + _atok);
        req.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(req, false, "path", dboxPath);

        // Get HTTP Response
        HTTPResponse resp = getResponseHTTP(req, aResp);
        if (resp==null || resp.getCode()!=HTTPResponse.OK) {
            System.err.println("DropBox.createFolder: " + (resp!=null ? resp.getMessage() : "null"));
            return;
        }

        // Get JSON response
        JSONNode json = resp.getJSON();
        if (json!=null)
            System.out.println(json);
    }

    /**
     * Handle a DELETE request.
     */
    protected void doDelete(WebRequest aReq, WebResponse aResp)
    {
        // Create Request
        HTTPRequest req = new HTTPRequest(DELETE);
        req.addHeader("Authorization", "Bearer " + _atok);
        req.addHeader("Content-Type", "application/json");

        // Add path param as JSON content
        String dboxPath = getDropBoxPathForURL(aReq.getURL());
        addParamsToRequestAsJSON(req, false, "path", dboxPath);

        // Get response
        getResponseHTTP(req, aResp);
    }

    /**
     * Returns the dropbox path for URL.
     */
    private String getDropBoxPathForURL(WebURL aURL)
    {
        String path = aURL.getPath();
        return _sitePath + (path.length()>1 ? path : "");
    }

    /**
     * Adds a JSON Header to given HTTP Request.
     */
    private static void addParamsToRequestAsJSON(HTTPRequest aReq, boolean asHeader, String ... thePairs)
    {
        // Create JSON Request and add pairs
        JSONNode jsonReq = new JSONNode();
        for (int i=0; i<thePairs.length; i+=2)
            jsonReq.addKeyValue(thePairs[i], thePairs[i+1]);

        // Add as header
        if (asHeader) {
            String jsonReqStr = jsonReq.toStringCompacted();
            jsonReqStr = jsonReqStr.replace("\"", "\\\"");
            jsonReqStr = jsonReqStr.replace("\\", "");
            aReq.addHeader("Dropbox-API-Arg", jsonReqStr);
        }

        // Add as send-bytes
        else {
            String jsonReqStr = jsonReq.toString();
            aReq.setBytes(jsonReqStr.getBytes());
        }
    }

    /**
     * Sends the HTTP request and loads results into WebResponse.
     */
    private static HTTPResponse getResponseHTTP(HTTPRequest aReq, WebResponse aResp)
    {
        // Get response
        HTTPResponse resp;
        try { resp = aReq.getResponse(); }
        catch (Exception e)
        {
            aResp.setException(e);
            return null;
        }

        // Copy response
        aResp.copyResponse(resp);
        return resp;
    }

    /**
     * Returns a FileHeader for DropBox File Entry JSONNode.
     */
    private static FileHeader createFileHeaderForJSON(JSONNode aFileEntryNode)
    {
        // Get attributes
        String path = aFileEntryNode.getNodeString("path_display");
        String tag = aFileEntryNode.getNodeString(".tag");
        boolean isFile = tag.equals("file");

        // Create FileHeader
        FileHeader fhdr = new FileHeader(path, !isFile);

        // Get additional file attributes
        if (isFile) {

            // Get/set size
            String sizeStr = aFileEntryNode.getNodeString("size");
            long size = Long.parseLong(sizeStr);
            fhdr.setSize(size);

            // Get/set ModTime
            String mod = aFileEntryNode.getNodeString("server_modified");
            if (mod.endsWith("Z")) {
                try {
                    Date date = _fmt.parse(mod);
                    fhdr.setModTime(date.getTime());
                }
                catch (Exception e) { System.err.println(e); }
            }
        }

        // Return FileHeader
        return fhdr;
    }

    /**
     * Returns a path for email. E.G.: jack@abc.com = /com/abc/jack.
     * We're storing files at DropBox in this format.
     */
    private String getPathForEmail(String anEmail)
    {
        // Get email name
        int domInd = anEmail.indexOf('@'); if (domInd<0) return null;
        String name = anEmail.substring(0, domInd).replace('.', '_');

        // Get email domain parts
        String domain = anEmail.substring(domInd+1);
        String dirs[] = domain.split("\\.");

        // Add domain parts
        String path = "/";
        for (int i=dirs.length-1; i>=0; i--)
            path += dirs[i] + '/';

        // Add name and return
        path += name;
        return path;
    }

    /**
     * Returns a list of derived items for given collection of original items.
     */
    private static <T,R> List<R> getMappedList(Collection<T> aList, Function<? super T, ? extends R> mapper)
    {
        return aList.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Returns shared instance.
     */
    public static DropBox getSiteForEmail(String anEmail)
    {
        // Get cached dropbox for email
        String email = anEmail.toLowerCase();
        DropBox dbox = _dboxes.get(email);
        if (dbox!=null || _atok==null)
            return dbox;

        // Otherwise, create and set
        //System.setProperty("javax.net.debug","all");
        dbox = new DropBox(anEmail);
        _dboxes.put(email, dbox);
        return dbox;
    }
}
