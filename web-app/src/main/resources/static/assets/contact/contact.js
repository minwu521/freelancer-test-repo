const params = new URLSearchParams(window.location.search);
const query = params.get("name");
if (query) {
    document.getElementById("search-input").value = query;
}