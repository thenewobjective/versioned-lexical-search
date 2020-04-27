<%@ Page Language="C#" %>
<%@ Import namespace="System.IO" %>
<script runat="server">
    protected const string lexemeFolder = @"C:\dev\lexical-search\lexemes\";
    protected const string svnUrl = @"http://localhost:8080/svn/";
    protected bool displayDefault = true;

    protected class FileInfo{
        public FileInfo(){}
        public string actionCode{get;set;}
        public string path{get;set;}
    }
    
    protected class RevInfo {
        public RevInfo(){}
        public int revision{get;set;}
        public IEnumerable<FileInfo> files{get;set;}
    }

    protected string execCommand(string command) {
        System.Diagnostics.Process si = new System.Diagnostics.Process();
        si.StartInfo.WorkingDirectory = @"c:\";
        si.StartInfo.UseShellExecute = false;
        si.StartInfo.FileName = "cmd.exe";
        si.StartInfo.Arguments = "/C " + command;
        si.StartInfo.CreateNoWindow = true;
        si.StartInfo.RedirectStandardInput = true;
        si.StartInfo.RedirectStandardOutput = true;
        si.StartInfo.RedirectStandardError = true;
        si.Start();
        string output = si.StandardOutput.ReadToEnd();
        si.Close();
        return output;
    }

    protected void extractLexemes(string projectPath) {
        var cmd = "DMSLexemeExtractor Java~Java1_6 @" + projectPath;
        execCommand(cmd);
    }

    protected void DMSSearchEngineIndex(string projectPath) {
        var cmd = "DMSSearchEngineIndex " + projectPath;
        execCommand(cmd);
    }

    protected string DMSSearchEngine(string dbPath, string query) {
        var cmd = "DMSSearchEngine " + dbPath + " -query:" + query;
        return execCommand(cmd);
    }
    
    protected IEnumerable<RevInfo> parseRevlog(string revLog) {
        var reRevSection = new Regex(@"\-[\n\r]+?r(\d+)([\s\S]+?)\n\-");
        var reJavaFiles = new Regex(@"\x20{3}([AMR])\x20([^\r\n]+?\.java)\b", RegexOptions.IgnoreCase);

        var revSections = 
            from Match match in reRevSection.Matches(revLog)
            let javaFiles = 
                from Match submatch in reJavaFiles.Matches(match.Groups[2].Value)
                select new FileInfo {
                    actionCode = submatch.Groups[1].Value,
                    path = submatch.Groups[2].Value
                }
            select new RevInfo {
                //TODO: is date and time wanted/needed?
                revision = int.Parse(match.Groups[1].Value),
                files = javaFiles
            };
        return revSections;
    }
    
    //<http://svnbook.red-bean.com/en/1.7/svn.ref.svn.c.export.html>
    protected string svnExport(string repo, int rev, string filePath) {
        var fullRepo = svnUrl + repo + "/" + filePath;
        fullRepo = fullRepo.Replace('\\', '/');
        var localCopy = lexemeFolder + repo + "/r" + rev + "/" + filePath;
        localCopy = localCopy.Replace('\\', '/');
        //the file has to exist on the filesystem for svn export to populate it
        Directory.CreateDirectory(Path.GetDirectoryName(localCopy));
        (File.Create(localCopy)).Close();
        var svnExpCmd = "svn export --force " + fullRepo + "@" + rev + " " + localCopy;
        return execCommand(svnExpCmd);
    }

    // <http://svnbook.red-bean.com/en/1.7/svn.ref.svn.c.log.html>
    protected string svnLogVerbose(string repo, int rev1, int rev2) {
        var fullRepo = svnUrl + repo;
        var svnLogCmd = "svn log -r " + rev1 + ":" + rev2 + " " + fullRepo + " --verbose";
        return execCommand(svnLogCmd);
    }

    protected int maxRevision(string repo) {
        var fullRepo = svnUrl + repo;
        var svnLogCmd = "svn log -r HEAD:HEAD " + fullRepo;
        var result = execCommand(svnLogCmd);

        var re = (new Regex(@"\-[\r\n]+?r(\d+)"));
        var match = re.Match(result).Groups[1].Value;
        return int.Parse(match);
    }

    protected bool rebuildLexemes(string repo, IEnumerable<RevInfo> revSections) {
        var lexPath = lexemeFolder + repo;

        if(!Directory.Exists(lexPath))
            Directory.CreateDirectory(lexPath);

        foreach (var revSection in revSections) {
            var revSrcPath = lexPath + "\\r" + revSection.revision;
            var revLexemePath = lexPath + "\\r" + revSection.revision + "-lexemes";
            if (!Directory.Exists(revLexemePath)) {
                Directory.CreateDirectory(revLexemePath);
                var lexProjectContent = "Java~Java1_6 LexemeExtractor 1.0"
                    + "\n<" + revSrcPath + "\n>" + revLexemePath;

                foreach (var file in revSection.files) {
                    svnExport(repo, revSection.revision, file.path.Substring(1));
                    lexProjectContent += "\n" + file.path.Substring(1);
                }

                File.WriteAllText(Path.Combine(revLexemePath, "projectFile.lxe"), lexProjectContent);
                extractLexemes(Path.Combine(revLexemePath, "projectFile.lxe"));
            }
        }

        return true;
    }

    protected bool rebuildDatabase(string repo) {
        var lexPath = lexemeFolder + repo;
        var dbPath = lexPath + "\\searchdatabase";
        var dbFullPath = Path.Combine(dbPath, "db.prj");

        if (Directory.Exists(dbPath))
            Directory.Delete(dbPath, true);

        Directory.CreateDirectory(dbPath);

        var dbProjectContent = "SearchEngine 1.0\n>" + dbPath;

        foreach (var dir in Directory.GetFiles(lexPath, "*.lxe", SearchOption.AllDirectories)) {
            dbProjectContent += "\n" + dir;
        }

        File.WriteAllText(dbFullPath, dbProjectContent);

        DMSSearchEngineIndex(dbFullPath);
        
        return true;
    }

    //TODO: should a search return the results in all revisions of a file, 
    //or only at the points of addition and modification?
    //currently the latter is used
    protected string executeSearch(string repo, int start, int end, string query) {
        var lexPath = lexemeFolder + repo;
        var dbPath = lexPath + "\\searchdatabase";
        var dbFullPath = Path.Combine(dbPath, "db.prj");
        var searchResult = DMSSearchEngine(dbFullPath, query);
        var re = new Regex(@"[\r\n]Line:\x20(\d+)\x20File:\x20[^\r\n]+?[\\/]r(\d+)[\\/]([^\r\n]+)[\r\n]+([^\r\n]+)");
        
        // LINE,REVISION,PATH,MATCH\r\n
        var sb = new StringBuilder();
        foreach(Match match in re.Matches(searchResult)) {
            sb.Append(match.Groups[1])
              .Append(",")
              .Append(match.Groups[2])
              .Append(",")
              .Append(match.Groups[3])
              .Append(",")
              .Append(match.Groups[4])
              .Append("\r\n");
        }
        
        return sb.ToString();
    }

    private void executeQuery(string repo, string start, string end, string query) {
        if ((new[] { repo, start, end, query }).All(p => !String.IsNullOrWhiteSpace(p))) {
            if (!Directory.Exists("C:\\Repositories\\" + repo)) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid repository";
                return;
            }

            var maxRev = maxRevision(repo);
            var iStart = start == "HEAD" ? maxRev : Int32.Parse(start);
            var iEnd = end == "HEAD" ? maxRev : Int32.Parse(end);

            if (iEnd < iStart || iStart < 1 || iEnd < 1 || iEnd > maxRev) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid revision range";
                return;
            }

            string searchResult = executeSearch(repo, iStart, iEnd, query);

            Response.ContentType = "text/csv";
            Response.Write(searchResult);
        } else {
            Response.StatusCode = 400;
            Response.ContentType = "text/plain";
            Response.Write("Invalid parameters");
        }
    }
   
    private void executeMetrics(string repo, string start, string end, string path) {
        if ((new[] { repo, start, end, path }).All(p => !String.IsNullOrWhiteSpace(p))) {
            if (!Directory.Exists("C:\\Repositories\\" + repo)) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid repository";
                return;
            }

            var maxRev = maxRevision(repo);
            var iStart = start == "HEAD" ? maxRev : Int32.Parse(start);
            var iEnd = end == "HEAD" ? maxRev : Int32.Parse(end);

            if (iEnd < iStart || iStart < 1 || iEnd < 1 || iEnd > maxRev) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid revision range";
                return;
            }

            var metricsPath = lexemeFolder + repo + @"\searchdatabase\metrics.xml";
            var re = new Regex(@"/r(\d+)/([\s\S]+)");
            var lPath = path.ToLower();
            var results = (from XElement element in XDocument.Load(metricsPath).Descendants("LexemeFile")
                          let m = re.Match(element.Attribute("FileName").Value)
                          let revision = int.Parse(m.Groups[1].Value)
                          let subpath = m.Groups[2].Value
                          let lsubPath = subpath.ToLower()
                          where iStart <= revision && revision <= iEnd
                          where lPath == lsubPath
                          orderby revision
                          //REVISION,PATH,TOTAL_LINES,CODE_LINES,COMMENT_LINES,BLANK_LINES,COMMENTS,CYCLO
                          select 
                                revision.ToString() + "," +
                                subpath + "," +
                                element.Attribute("SLOC").Value + "," +
                                element.Attribute("totalCodeLines").Value + "," +
                                element.Attribute("totalCommentLines").Value + "," +
                                element.Attribute("totalBlankLines").Value + "," +
                                element.Attribute("totalComments").Value + "," +
                                element.Attribute("Cyclomatic").Value
                          ).ToArray();
            
            Response.ContentType = "text/csv";
            Response.Write(string.Join("\r\n",results));
        } else {
            Response.StatusCode = 400;
            Response.ContentType = "text/plain";
            Response.Write("Invalid parameters");
        }
    }
    
    private void downloadFile(string repo, string rev, string path) {
        if ((new[] { repo, rev, path }).All(p => !String.IsNullOrWhiteSpace(p))) {
            var maxRev = maxRevision(repo);
            var iRev = rev == "HEAD" ? maxRev : Int32.Parse(rev);
            
            if(iRev < 1 || iRev > maxRev) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid revision";
                return;
            }

            if (!Directory.Exists("C:\\Repositories\\" + repo)) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid repository";
                return;
            }
            
            var lexPath = lexemeFolder + repo + "\\r" + iRev + "\\" + path.Replace('/','\\');

            if (!File.Exists(lexPath)) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid path";
                return;
            }

            Response.ContentType = "text/plain";
            Response.Write(File.ReadAllText(lexPath));
        } else {
            Response.StatusCode = 400;
            Response.ContentType = "text/plain";
            Response.Write("Invalid parameters");
        }
    }
    
    private void executeRebuild(string repo, string start, string end) {
        if ((new[]{repo,start,end}).All(p => !String.IsNullOrWhiteSpace(p))) {
            var maxRev = maxRevision(repo);
            var iStart = start == "HEAD" ? maxRev : Int32.Parse(start);
            var iEnd = end == "HEAD" ? maxRev : Int32.Parse(end);

            if (iEnd < iStart || iStart < 1 || iEnd < 1 || iEnd > maxRev) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid revision range";
                return;
            }

            if (!Directory.Exists("C:\\Repositories\\" + repo)) {
                Response.StatusCode = 400;
                Response.StatusDescription = "Invalid repository";
                return;
            }

            var revSections = parseRevlog(svnLogVerbose(repo, iStart, iEnd));

            if (!rebuildLexemes(repo, revSections)) {
                Response.StatusCode = 500;
                Response.StatusDescription = "Unable to build lexemes";
                return;
            }

            if (!rebuildDatabase(repo)) {
                Response.StatusCode = 500;
                Response.StatusDescription = "Unable to build database";
                return;
            }

            Response.StatusCode = 204;
        } else {
            Response.StatusCode = 400;
            Response.ContentType = "text/plain";
            Response.Write("Invalid parameters");
        }
    }
    
    protected void Page_Load(object sender, EventArgs e) {
        var repo = Request.QueryString["repo"] ?? Request.Form["repo"];
        var cmd = Request.QueryString["cmd"] ?? Request.Form["cmd"];
        var rev = Request.QueryString["rev"] ?? Request.Form["rev"];
        var start = Request.QueryString["start"] ?? Request.Form["start"];
        var end = Request.QueryString["end"] ?? Request.Form["end"];
        var query = Request.QueryString["query"] ?? Request.Form["query"];
        var path = Request.QueryString["path"] ?? Request.Form["path"];

        if (Request.HttpMethod == "GET" && cmd == "query") {
            displayDefault = false;
            executeQuery(repo, start, end, query);
        } else if (Request.HttpMethod == "GET" && cmd == "metrics") {
            displayDefault = false;
            executeMetrics(repo, start, end, path);
        } else if (Request.HttpMethod == "GET" && cmd == "download") {
            displayDefault = false;
            downloadFile(repo, rev, path);
        } else if (Request.HttpMethod == "GET" && String.IsNullOrWhiteSpace(cmd)) {
            displayDefault = true;
        } else if (Request.HttpMethod == "POST" && cmd == "rebuild") {
            displayDefault = false;
            executeRebuild(repo, start, end);
        } else {
            displayDefault = false;
            Response.StatusCode = 400;
            Response.ContentType = "text/plain";
            Response.Write("Invalid parameters");
        }
    }
</script>
<% if(displayDefault) { %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" dir="ltr" lang="en-us">
    <head runat="server">
        <meta charset="utf-8" />
        <title>Versioned Lexical Search</title>
        <style type="text/css">
            label{
                display:block;
                margin: 1em 0;
            }
        </style>
    </head>
    <body>
        <h2>Search Repository History</h2>
        <form method="get" action=".">
            <input type="hidden" name="cmd" value="query" />

            <label>Repository:
                <input type="text" name="repo" />
            </label>

            <label>Start:
                <input type="text" name="start" />
            </label>

            <label>End:
                <input type="text" name="end" />
            </label>
            
            <label>Query:
                <input type="text" name="query" />
            </label>
            
            <button type="reset">Reset</button>
            <button type="submit">Submit</button>
        </form>
        <h2>Query Metrics</h2>
        <form method="get" action=".">
            <input type="hidden" name="cmd" value="metrics" />

            <label>Repository:
                <input type="text" name="repo" />
            </label>

            <label>Start:
                <input type="text" name="start" />
            </label>

            <label>End:
                <input type="text" name="end" />
            </label>

            <label>Path:
                <input type="text" name="path" />
            </label>

            <button type="reset">Reset</button>
            <button type="submit">Submit</button>
        </form>
        <h2>Rebuild Search Database</h2>
        <form method="post" action=".">
            <input type="hidden" name="cmd" value="rebuild" />

            <label>Repository:
                <input type="text" name="repo" />
            </label>

            <label>Start:
                <input type="text" name="start" />
            </label>

            <label>End:
                <input type="text" name="end" />
            </label>
            
            <button type="reset">Reset</button>
            <button type="submit">Submit</button>
        </form>
        <h2>Download File</h2>
        <form method="get" action=".">
            <input type="hidden" name="cmd" value="download" />

            <label>Repository:
                <input type="text" name="repo" />
            </label>

            <label>Revision:
                <input type="text" name="rev" />
            </label>

            <label>Path:
                <input type="text" name="path" />
            </label>
            
            <button type="reset">Reset</button>
            <button type="submit">Submit</button>
        </form>
    </body>
</html>
<% }%>