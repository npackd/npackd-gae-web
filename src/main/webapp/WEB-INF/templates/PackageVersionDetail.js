function addDependency(package_, versions, variable) {
	var files = $('#deps');
    var n = files.children().first().children().size() - 1;
    files.children().first().append(
    		'<tr><td><input class="form-control" type="text" ' + 
    		'name="depPackage.' + n + '" value="' + package_ + '" size="80"></td><td>' + 
    		'<input class="form-control" type="text" ' + 
    		'name="depVersions.' + n + '" value="' + versions + '" size="20"></td><td>' + 
    		'<input class="form-control" type="text" ' + 
    		'name="depEnvVar.' + n + '" value="' + variable + '" size="20"></td></tr>');
}

function deleteOnClick() {
	if (confirm("Do you really want to delete this package version?")) {
		window.location.href='/package-version/delete-confirmed?name=' + 
                        $('#package').val() + "@" + $('#version').val();
	}
}

function addFile(name, content) {
	var files = $('#files');
    var n = files.children().size();
    files.append('<div><div>File path ' + n + ':</div><input class=\"form-control\" type=\"text\" name=\"path.' + n + '\" value=\"' + name + '\" size=\"80\"><div>File content ' + n + ':</div><textarea class=\"form-control\" name=\"content.' + n + '\" rows=\"20\" cols=\"80\" wrap=\"off\">' + content + '</textarea></div>');
}

function updateTagCheckboxes() {
	var tags = $('#tags').val();
	var tags_ = tags.split(/[\s,]+/);
        
        // if (window.console) console.log(tags_);
        
        var checkboxes = $(".nw-tag-checkbox");
        checkboxes.prop('checked', function() {
            var r = $.inArray(this.value, tags_) >= 0;
            // if (window.console) console.log(this.value + " " + r);
            return r;
        });
}

/**
 * Updates the input text field for the tags from the values of selected
 * checkboxes.
 */
function updateTagInput() {
    var tags = $('#tags').val();
    var tags_ = tags.split(/[\s,]+/);
    
    $(".nw-tag-checkbox").each(function() {
        var p = $.inArray(this.value, tags_);
        if ($(this).prop("checked")) {
            if (p < 0)
                tags_.push(this.value);
        } else {
            if (p >= 0)
                tags_.splice(p, 1);
        }
    });

    $('#tags').val(tags_.join(", "));
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
        event.preventDefault();
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
        event.preventDefault();
    });

    $('#addMSIFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "set onecmd=\"%npackd_cl%\\npackdcl.exe\" \"path\" \"--package=com.googlecode.windows-package-manager.NpackdInstallerHelper\" \"--versions=[1.1, 2)\"\r\n"
                        + "for /f \"usebackq delims=\" %%x in (`%%onecmd%%`) do set npackdih=%%x\r\n"
                        + "call \"%npackdih%\\InstallMSI.bat\" INSTALLDIR yes\r\n");
        event.preventDefault();
    });

    $('#addVimFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "mkdir \"%ALLUSERSPROFILE%/Npackd/VimPlugins/\"\r\n"
                        + "mklink /D \"%ALLUSERSPROFILE%/Npackd/VimPlugins/%NPACKD_PACKAGE_NAME%\" \"%CD%\"\r\n");
        addFile(".Npackd\\Uninstall.bat",
                "rmdir \"%ALLUSERSPROFILE%/Npackd/VimPlugins/%NPACKD_PACKAGE_NAME%\"\r\n"
                        + "verify\r\n");
        event.preventDefault();
    });

    $('#addSevenZIPFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
				"set onecmd=\"%npackd_cl%\\npackdcl.exe\" \"path\" \"--package=org.7-zip.SevenZIPA\" \"--versions=[9.20, 10)\"\r\n"
				+ "for /f \"usebackq delims=\" %%x in (`%%onecmd%%`) do set sevenzipa=%%x\r\n"
				+ "for /f %%x in ('dir /b *.7z') do set setup=%%x\r\n"
				+ "\"%sevenzipa%\\7za.exe\" x \"%setup%\" > .Npackd\\Output.txt && type .Npackd\\Output.txt && del /f /q \"%setup%\"\r\n");
		addDependency("org.7-zip.SevenZIPA", "[9.20, 10)", "");
		addDependency("com.googlecode.windows-package-manager.NpackdCL",
				"[1.15.7, 2)", "");
		$('#oneFile').prop('checked', true);
        event.preventDefault();
    });

    $('#addDep').click(function(event) {
    	addDependency("", "", "");
    });

    $('#removeDep').click(function(event) {
    	var ch = $('#deps').children().first().children();
    	if (ch.size() > 1)
    		ch.last().remove();
    });
    
    updateTagCheckboxes();
    
    $("#tags").on('input', function() {
    	updateTagCheckboxes();
    });
    
    $(".nw-tag-checkbox").change(function() {
        updateTagInput();
    });
});

