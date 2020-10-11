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

function copyOnClick() {
    var id = $('#version').val();
    var msg = prompt("Please enter the new version number for the copy.", 
            id);
    if (msg !== null) {
        window.location.href='/package-version/copy-confirmed?version=' + 
                msg + 
                "&name=" + $('#package').val() + "@" + id;
    }
}

function addFile(name, content) {
    var files = $('#files');
    var n = files.children().size();
    files.append('<div><div>File path ' + n + ':</div><input class=\"form-control\" type=\"text\" name=\"path.' + n + '\" value=\"' + name + '\" size=\"80\"><div>File content ' + n + ':</div><textarea class=\"form-control nw-autosize\" name=\"content.' + n + '\" rows=\"5\" cols=\"80\" wrap=\"off\">' + content + '</textarea></div>');
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
                        "\"%npackd_package_binary%\" /S /D=%CD% && del /f /q \"%npackd_package_binary%\"\r\n");
        addFile(".Npackd\\Uninstall.bat", "uninst.exe /S _?=%CD%\r\n");
        event.preventDefault();
    });

    $('#addInnoSetupFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "call \"%nih%\\InstallInnoSetup.bat\"\r\n");
        addFile(".Npackd\\Uninstall.bat",
                "call \"%nih%\\UninstallInnoSetup.bat\" unins000.exe\r\n");
        addDependency("com.googlecode.windows-package-manager.NpackdInstallerHelper", 
                "[1.3, 2)", "nih");
        event.preventDefault();
    });

    $('#addMSIFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "call \"%nih%\\InstallMSI.bat\" INSTALLDIR yes\r\n");
        addDependency("com.googlecode.windows-package-manager.NpackdInstallerHelper", 
                "[1.1, 2)", "nih");
        event.preventDefault();
    });

    $('#addZIPDirFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
                "for /f \"delims=\" %%x in ('dir /b " + $('#package').val() + 
                "*') do set name=%%x\r\n" + 
		"\"%clu%\\clu\" unwrap-dir -p \"%name%\"> .Npackd\\Output.txt && type .Npackd\\Output.txt\r\n");
        addDependency("com.googlecode.windows-package-manager.CLU", "[1.25, 2)", "clu");
        $('#zip').prop('checked', true);
        event.preventDefault();        
    });

    $('#addSevenZIPFiles').click(function(event) {
        addFile(".Npackd\\Install.bat",
		"\"%sevenzipa%\\7za\" x \"%npackd_package_binary%\"> .Npackd\\Output.txt && type .Npackd\\Output.txt && del /f /q \"%npackd_package_binary%\"\r\n");
        addDependency("org.7-zip.SevenZIPA", "[19, 100)", "sevenzipa");
        $('#oneFile').prop('checked', true);
        event.preventDefault();        
    });

    $('#addRemoveSCPFiles').click(function(event) {
        addFile(".Npackd\\Uninstall.bat",
		"\"%ncl%\\ncl\" remove-scp --title \"/%npackd_package_name% %npackd_package_version%/i\"> .Npackd\\Output.txt && type .Npackd\\Output.txt\r\n");
        addDependency("com.googlecode.windows-package-manager.NpackdCL", "[1.25, 2)", "ncl");
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
    
    autosize($(".nw-autosize"));
});

