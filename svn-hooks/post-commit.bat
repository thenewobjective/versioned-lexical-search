:: C:\Repositories\jpcsp\hooks\post-commit.bat

@ECHO OFF

:: [1] REPOS-PATH   (the path to this repository)
:: [2] REV          (the number of the revision just committed)
:: [3] TXN-NAME     (the name of the transaction that has become REV)

:: POSTing to a url directly from a *.BAT is non-trivial at best
PowerShell.exe C:\Repositories\jpcsp\hooks\post-commit.ps1 %1 %2 %3