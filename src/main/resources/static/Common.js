/* global m */

/**
 * Mithril component for a star.
 * 
 * @param {type} initialVnode
 * @returns Mithril component
 */
function Star(initialVnode) {
    // Component state variable, unique to each instance
    var starred = initialVnode.attrs.starred;
    var filled = initialVnode.attrs.filled;
    var package = initialVnode.attrs.package;
    
    //console.log("title:" + JSON.stringify(initialVnode));

    return {
        view: function(vnode) {
            var txt;
            if (starred === 1) {
                txt = "1 user starred this package";
            } else if (starred > 1) {
                txt = starred + " users starred this package";
            } else {
                txt = "";
            }
            
            var name;
            var style;
            if (filled) {
                name = "span.star.glyphicon.glyphicon-star";
                style = {
                            cursor: "pointer", 
                            color: "#337ab7"
                        };
            } else {
                name = "span.star.glyphicon.glyphicon-star-empty";
                style = {cursor: "pointer"};
            }
            
            var span = m(name, 
                    {
                        onclick: function() {
                            var star = 1;

                            if (!filled) {
                                star = 1;
                            } else {
                                star = 0;
                            }

                            m.request({
                                url: "/api/star",
                                params: { package: package, star: star },
                                withCredentials: true
                            })
                            .then(function(data) {
                                if (filled) {
                                    filled = false;
                                    starred--;
                                } else {
                                    filled = true;
                                    starred++;
                                }
                            });
                        },
                        style: style
                    });
            var small = m("small", {}, txt);
            return [span, small];
        }
    };
}

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
                        style: {
                            "font-size": "20px", 
                            "font-weight": "bold"
                        }
                    });
            var a = m("a", {
                    href: "javascript: return false;",
                    onclick: function() {
                        window.open(vnode.dom.value);
                        event.preventDefault();
                    }
            }, [span]);
            return [input, a];
        }
    };
}

/**
 * Convert HTML elements to Mithril components.
 */
function initComponents() {
    var elements = document.getElementsByClassName("nw-input-url");
    for (var i = 0; i < elements.length; i++) {
        var n = elements[i];
        var input = n.getElementsByTagName("input")[0];

        //console.log(n.nodeName);
        //console.log(JSON.stringify(n));
        m.render(n, m(InputURL, {
            name: input.getAttribute("name"),
            value: input.getAttribute("value"),
            title: input.getAttribute("title")
        }));
    }

    var elements = document.getElementsByClassName("nw-star");
    for (var i = 0; i < elements.length; i++) {
        var n = elements[i];
        var span = n.getElementsByTagName("span")[0];

        //console.log(n.nodeName);
        //console.log(JSON.stringify(n));
        m.mount(n, {view: function () {return m(Star, {
            starred: parseInt(span.getAttribute("data-starred")),
            filled: span.getAttribute("data-filled") === "true",
            package: span.getAttribute("data-package")
        });}});
    }
}

$(document).ready(initComponents);

