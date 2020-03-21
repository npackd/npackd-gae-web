/* global m */

/**
 * Mithril component for entering URLs.
 * It is an "input" and a icon with link inside a "div".
 * 
 * @param {type} initialVnode
 * @returns Mithril component
 */
function InputURL(initialVnode) {
    // Component state variable, unique to each instance
    var name = initialVnode.attrs.name;
    var title = initialVnode.attrs.title;
    var value = initialVnode.attrs.value;
    
    //console.log("title:" + JSON.stringify(initialVnode));

    return {
        view: function(vnode) {
            var input = m("input.form-control", 
                    {
                        style: {display: "inline", width: "90%"},
                        type: "url",
                        name: name,
                        value: value,
                        size: 120,
                        title: title
                    });
            var span = m("span.glyphicon.glyphicon-link", 
                    {
                        onclick: function() {
                            window.open(vnode.dom.value);
                            event.preventDefault();
                        },
                        style: {
                            cursor: "pointer", 
                            "font-size": "20px", 
                            "font-weight": "bold"
                        }
                    });
            return [input, span];
        }
    };
}

/**
 * Convert all <div class="nw-input-url"> to "InputURL" components.
 */
function initInputURLs() {
    var inputURLs = document.getElementsByClassName("nw-input-url");
    for (var i = 0; i < inputURLs.length; i++) {
        var n = inputURLs[i];
        var input = n.getElementsByTagName("input")[0];

        //console.log(n.nodeName);
        //console.log(JSON.stringify(n));
        m.render(n, m(InputURL, {
            name: input.getAttribute("name"),
            value: input.getAttribute("value"),
            title: input.getAttribute("title")
        }));
    }
}

$(document).ready(initInputURLs);

