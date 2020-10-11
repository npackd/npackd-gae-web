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
});

function removeCategory0Filter() {
    $('#category0').val("");
    $('#searchForm').submit();
}

function removeRepositoryFilter() {
    $('#repository').val("");
    $('#searchForm').submit();
}