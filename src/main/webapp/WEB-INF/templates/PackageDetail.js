var defaultTags = ["stable", "stable64", "libs",
        "unstable", 
        "same-url", "end-of-life", "reupload"];
                                    
function updateTagCheckboxes() {
    var allTags = defaultTags.slice();
    var tags = $('#tags').val();
    var tags_ = tags.split(",");
    for (var i = 0; i < tags_.length; i++) {
        var tag = $.trim(tags_[i]);
        var index = $.inArray(tag, allTags);
        if (index >= 0) {
            $("#tag-" + tag).prop('checked', true);
            allTags.splice(index, 1);
        }
    }
    for (var i = 0; i < allTags.length; i++) {
        $("#tag-" + allTags[i]).prop('checked', false);
    }
}

/**
 * Updates the input text field for the tags from the values of selected
 * checkboxes.
 */
function updateTagInput() {
    var allTags = defaultTags.slice();

    var tags = $('#tags').val();
    var tags_ = tags.split(",");
    for (var i = 0; i < tags_.length; i++) {
        tags_[i] = $.trim(tags_[i]);
    }

    for (var i = 0; i < allTags.length; i++) {
        var tag = allTags[i];
        var index = $.inArray(tag, tags_);
        if ($("#tag-" + tag).prop('checked')) {
            if (index < 0) {
                tags_.push(tag);
            }
        } else {
            if (index >= 0) {
                tags_.splice(index, 1);
            }
        }
    }

    $('#tags').val(tags_.join(", "));
}

function deleteOnClick() {
    var msg = prompt("Deleting the package. Please enter the explanations.", 
                    "Seems to be just a test.");
    if (msg !== null) {
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
    
    $('#name-link').click(function(event) {
        window.open("https://repology.org/projects/?search=" + 
                encodeURIComponent($('#name').val()));
        event.preventDefault();
    });
    
    $('#changelog-link').click(function(event) {
        window.open($('#changelog').val());
        event.preventDefault();
    });
    
    $('#title').on('input', function(event) {
    	var s = $.trim($(this).val());
    	s = s.toLowerCase().replace(/[^-0..9\w]/g, "-");
    	s = s.replace(/\./g, "-");
    	s = s.replace(/\-+/g, "-");
    	if (s.indexOf("-") === 0)
    		s = s.substring(1);
    	if (s.lastIndexOf('-') === s.length - 1)
    		s = s.substring(0, s.length - 1);
    	$('#name').val(s);
    });

    updateTagCheckboxes();
    
    $("#tags").on('input', function() {
    	updateTagCheckboxes();
    });
    
    for (var i = 0; i < defaultTags.length; i++) {
    	$("#tag-" + defaultTags[i]).change(function() {
    		updateTagInput();
    	});
    }

    autosize($(".nw-autosize"));

    $('.star').click(starClick);
}

function starClick(event) {
    var star = 1;
    var t = event.target;
    var p = $(t).data('package');

    if ($(t).hasClass("glyphicon-star-empty")) {
        star = 1;
    } else {
        star = 0;
    }

    $.ajax({
        url: "/api/star",
        data: { package: p, star: star },
        cache: false
    }).done(function() {
        $.ajax({
            url: "/star",
            data: { package: p },
            context: t,
            cache: false
        }).done(function(html) {
            this.parentElement.innerHTML = html;
            $('.star').off("click");
            $('.star').click(starClick);
        });
    });
}

function renameOnClick() {
    var id = $('#name').val();
    var msg = prompt("Please enter the new package name.", id);
    if (msg !== null) {
        window.location.href='/package/rename-confirmed?new-name=' + 
                msg + "&name=" + id;
    }
}


$(document).ready(initEvents);

