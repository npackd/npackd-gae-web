function initInputURLs() {
    var inputURLs = document.getElementsByClassName("nw-input-url");
    for (var i = 0; i < inputURLs.length; i++) {
        (function() {
            var el = inputURLs[i];
            var link = el.nextSibling;

            link.addEventListener("click", function() {
                window.open(el.value);
                event.preventDefault();
            }); 
        })();
    }
}

$(document).ready(initInputURLs);

