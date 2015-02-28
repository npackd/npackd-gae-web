$(function() {
    $('#category0').change(function() {
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