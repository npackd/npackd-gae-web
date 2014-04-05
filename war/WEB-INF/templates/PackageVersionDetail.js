function addDependency() {
	var files = $('#deps');
    var n = files.children().first().children().size() - 1;
    files.children().first().append('<tr><td><input class="form-control" type="text" name="depPackage.' + n + '" size="80"></td><td><input class="form-control" type="text" name="depVersions.' + n + '" size="20"></td><td><input class="form-control" type="text" name="depEnvVar.' + n + '" size="20"></td></tr>');
}

function addFile(name, content) {
	var files = $('#files');
    var n = files.children().size();
    files.append('<div><div>File path ' + n + ':</div><input class=\"form-control\" type=\"text\" name=\"path.' + n + '\" value=\"' + name + '\" size=\"80\"><div>File content ' + n + ':</div><textarea class=\"form-control\" name=\"content.' + n + '\" rows=\"20\" cols=\"80\" wrap=\"off\">' + content + '</textarea></div>');
}

$(document).ready(function() {
    $('#url-link').click(function(event) {
        window.open($('#url').val());
        event.preventDefault();
    });

    $('#addFile').click(function(event) {
    	var files = $('#files');
        var n = files.children().size();
        files.append('<div><div>File path ' + n + ':</div><input class=\"form-control\" type=\"text\" name=\"path.' + n + '\" value=\"\" size=\"80\"><div>File content ' + n + ':</div><textarea class=\"form-control\" name=\"content.' + n + '\" rows=\"20\" cols=\"80\" wrap=\"off\"></textarea></div>');
    });

    $('#removeFile').click(function(event) {
        $('#files').children().last().remove();
    });

    $('#addNSISFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n"
                        + "\"%setup%\" /S /D=%CD% && del /f /q \"%setup%\"\r\n");
        addFile(".Npackd\\Uninstall.bat", "uninst.exe /S _?=%CD%\r\n");
    });

    $('#addInnoSetupFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n"
                        + "\"%setup%\" /SP- /VERYSILENT /SUPPRESSMSGBOXES /NOCANCEL /NORESTART /DIR=\"%CD%\" /SAVEINF=\"%CD%\\.Npackd\\InnoSetupInfo.ini\" /LOG=\"%CD%\\.Npackd\\InnoSetupInstall.log\" && del /f /q \"%setup%\"\r\n"
                        + "set err=%errorlevel%\r\n"
                        + "type .Npackd\\InnoSetupInstall.log\r\n"
                        + "if %err% neq 0 exit %err%\r\n");
        addFile(".Npackd\\Uninstall.bat",
                "unins000.exe /VERYSILENT /SUPPRESSMSGBOXES /NORESTART /LOG=\"%CD%\\.Npackd\\InnoSetupUninstall.log\"\r\n"
                        + "set err=%errorlevel%\r\n"
                        + "type .Npackd\\InnoSetupUninstall.log\r\n"
                        + "if %err% neq 0 exit %err%\r\n");
    });

    $('#addMSIFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "set onecmd=\"%npackd_cl%\\npackdcl.exe\" \"path\" \"--package=com.googlecode.windows-package-manager.NpackdInstallerHelper\" \"--versions=[1.1, 2)\"\r\n"
                        + "for /f \"usebackq delims=\" %%x in (`%%onecmd%%`) do set npackdih=%%x\r\n"
                        + "call \"%npackdih%\\InstallMSI.bat\" INSTALLDIR yes\r\n");
    });

    $('#addVimFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "mkdir \"%ALLUSERSPROFILE%/Npackd/VimPlugins/\"\r\n"
                        + "mklink /D \"%ALLUSERSPROFILE%/Npackd/VimPlugins/%NPACKD_PACKAGE_NAME%\" \"%CD%\"\r\n");
        addFile(".Npackd\\Uninstall.bat",
                "rmdir \"%ALLUSERSPROFILE%/Npackd/VimPlugins/%NPACKD_PACKAGE_NAME%\"\r\n"
                        + "verify\r\n");
    });

    $('#addDep').click(function(event) {
    	addDependency();
    });

    $('#removeDep').click(function(event) {
    	var ch = $('#deps').children().first().children();
    	if (ch.size() > 1)
    		ch.last().remove();
    });
});

