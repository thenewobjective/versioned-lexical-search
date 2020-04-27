# C:\Repositories\jpcsp\hooks\post-commit.ps1

[string]$repoPath = $args[0] # C:\Repositories\jpcsp
[string]$rev = $args[1]      #3563
[string]$txnName = $args[2]  #3562-2qy
[string]$repo = $repoPath.Substring($repoPath.LastIndexOf('\')+1)
[string]$url = "http://localhost:51529/lexical-search/Default.aspx?cmd=rebuild&repo=$repo&start=$rev&end=$rev"

Invoke-WebRequest -Uri $url -Method POST