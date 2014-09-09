function deleteOnClick() {
	var msg = prompt("Deleting the package. Please enter the explanations.", 
			"This package contains OpenCandy. See https://code.google.com/p/windows-package-manager/wiki/RejectedSoftware for more details.");
	if (msg != null) {
		var id = $('#name').val();
		window.location.href='/package/delete-confirmed?name=' + id + 
				"&message=" + encodeURIComponent(msg);
	}
}

function initEvents() {
    $('#url-link').click(function(event) {
        window.open($('#url').val());
        event.preventDefault();
    });
    
    $('#title').on('input', function(event) {
    	var s = $.trim($(this).val());
    	s = s.toLowerCase().replace(/[^-0..9\w]/g, "-");
    	s = s.replace(/\-+/g, "-");
    	if (s.indexOf("-") === 0)
    		s = s.substring(1);
    	if (s.lastIndexOf('-') == s.length - 1)
    		s = s.substring(0, s.length - 1);
    	$('#id').val(s);
    });
}

$(document).ready(initEvents);

