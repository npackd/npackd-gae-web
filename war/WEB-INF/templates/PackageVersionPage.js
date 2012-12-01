function addFile() {
	YUI().use('node', function (Y) {
		var fileTable = Y.one('#fields');
		var n = fileTable.all('.fileContent').size();
		
		var newFileRow = Y.Node.create('<tr class="filePath">' + 
				'<td>File path ' + n + 
				':</td><td><input type="text" name="path.' + n + '" ' +
				'value="" size="80"></input></td></tr>');
		fileTable.append(newFileRow);

		var newFileRow = Y.Node.create('<tr class="fileContent">' + 
				'<td>File content ' + n + 
				':</td><td><textarea name="content.' + n + '" ' +
				'rows="20" cols="80" wrap="off"></textarea></td></tr>');
		fileTable.append(newFileRow);
	});
}

function removeFile() {
	YUI().use('node', function (Y) {
		var fileTable = Y.one('#fields');
		fileTable.all('.filePath').slice(-1).remove(true);		
		fileTable.all('.fileContent').slice(-1).remove(true);		
	});
}
