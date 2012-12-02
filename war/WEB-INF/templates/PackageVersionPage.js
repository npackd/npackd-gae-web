function addFile(name, content) {
	YUI().use('node', function (Y) {
		var fileTable = Y.one('#fields');
		var n = fileTable.all('.fileContent').size();
		
		var newFileRow = Y.Node.create('<tr class="filePath">' + 
				'<td>File path ' + n + 
				':</td><td><input type="text" name="path.' + n + '" ' +
				'value="' + name + '" size="80"></input></td></tr>');
		fileTable.append(newFileRow);

		var newFileRow = Y.Node.create('<tr class="fileContent">' + 
				'<td>File content ' + n + 
				':</td><td><textarea name="content.' + n + '" ' +
				'rows="20" cols="80" wrap="off">' + content + 
				'</textarea></td></tr>');
		fileTable.append(newFileRow);
	});
}

function addEmptyFile() {
	addFile("", "");
}

function removeFile() {
	YUI().use('node', function (Y) {
		var fileTable = Y.one('#fields');
		fileTable.all('.filePath').slice(-1).remove(true);		
		fileTable.all('.fileContent').slice(-1).remove(true);		
	});
}

function addNSISFiles() {
	addFile(".Npackd\\Install.bat", 
			"for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n" +
			"\"%setup%\" /S /D=%CD%\r\n");
	addFile(".Npackd\\Uninstall.bat", "uninst.exe /S _?=%CD%\r\n");
}

function addInnoSetupFiles() {
	addFile(".Npackd\\Install.bat", 
			"for /f \"delims=\" %%x in ('dir /b *.exe') do set setup=%%x\r\n" +
			"\"%setup%\" /SP- /VERYSILENT /SUPPRESSMSGBOXES /NOCANCEL /NORESTART /DIR=\"%CD%\" /SAVEINF=\"%CD%\\.Npackd\\InnoSetupInfo.ini\" /LOG=\"%CD%\\.Npackd\\InnoSetupInstall.log\"\r\n" +
			"set err=%errorlevel%\r\n" +
			"type .Npackd\\InnoSetupInstall.log\r\n" +
			"if %err% neq 0 exit %err%\r\n");
	addFile(".Npackd\\Uninstall.bat", 
			"unins000.exe /VERYSILENT /SUPPRESSMSGBOXES /NORESTART /LOG=\"%CD%\\.Npackd\\InnoSetupUninstall.log\"\r\n" +
			"set err=%errorlevel%\r\n" +
			"type .Npackd\\InnoSetupUninstall.log\r\n" +
			"if %err% neq 0 exit %err%\r\n");
}