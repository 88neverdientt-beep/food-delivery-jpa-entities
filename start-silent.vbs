Set WshShell = CreateObject("WScript.Shell")
strCurrentDir = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
WshShell.CurrentDirectory = strCurrentDir

' Chay file JAR an (0 = khong hien cua so console)
WshShell.Run "java -jar target\food-delivery-0.0.1-SNAPSHOT.jar", 0, False

' Cho 3 giay de server len roi mo trinh duyet
WScript.Sleep 3000
WshShell.Run "http://localhost:8080"
