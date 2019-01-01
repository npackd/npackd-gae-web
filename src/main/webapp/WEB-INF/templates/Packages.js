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
    
$(function() {
    $('#category0').change(function() {
        this.form.submit();
    });
    $('#repository').change(function() {
        this.form.submit();
    });
    $('#sort').change(function() {
        this.form.submit();
    });
    $('.star').click(starClick);
});

function removeCategory0Filter() {
    $('#category0').val("");
    $('#searchForm').submit();
}

function removeRepositoryFilter() {
    $('#repository').val("");
    $('#searchForm').submit();
}