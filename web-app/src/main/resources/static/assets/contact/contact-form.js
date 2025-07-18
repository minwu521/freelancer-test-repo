function selectCompanyFromSearch(element) {
    let isSelectingCompany = true;
    const name = element.getAttribute('data-name');
    const orgNumber = element.getAttribute('data-org-number');
    const countryCode = element.getAttribute('data-country-code');
    const nameInput = document.getElementById('companyName');
    const orgInput = document.getElementById('organizationNumber');
    const countrySelect = document.getElementById('countryCode');
    nameInput.value = name;
    if (orgNumber && orgNumber !== 'null') {
        orgInput.value = orgNumber;
    }
    countrySelect.value = countryCode;

    nameInput.dispatchEvent(new Event('change', { bubbles: true }));
    orgInput.dispatchEvent(new Event('change', { bubbles: true }));

    document.getElementById('name-dropdown').innerHTML = '';

    setTimeout(() => {
        orgInput.setCustomValidity('');
        orgInput.reportValidity();
        // Reset flag to allow form submission
        isSelectingCompany = false;
    }, 100);
}

document.addEventListener('click', function (e) {
    const nameInput = document.getElementById('companyName');
    const nameDropdown = document.getElementById('name-dropdown');

    const isSearchItem = e.target.closest('.search-item');

    if (isSearchItem) {
        setTimeout(() => {
            nameDropdown.innerHTML = '';
        }, 100);
    } else if (!nameInput.contains(e.target) && !nameDropdown.contains(e.target)) {
        nameDropdown.innerHTML = '';
    }
});
