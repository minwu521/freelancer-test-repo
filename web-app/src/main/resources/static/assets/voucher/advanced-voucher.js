// Simplified Advanced Voucher JavaScript - Basic functionality
let rowCounter = 0;

// Initialize form on page load
document.addEventListener('DOMContentLoaded', function () {
    // Only add a posting line if there are no existing posting lines
    const existingPostings = document.querySelectorAll('.posting-line-row');
    if (existingPostings.length === 0) {
        addPostingLine();
    } else {
        // Set rowCounter to the number of existing postings for new additions
        rowCounter = existingPostings.length;
        // Update balance for existing postings
        updateBalance();
    }

});

// Add new posting line
function addPostingLine() {
    const tbody = document.getElementById('postingLines');
    if (!tbody) {
        console.error('Could not find postingLines tbody element');
        return;
    }

    // Get tenant ID from URL
    const tenantId = new URLSearchParams(window.location.search).get('tenantId');
    const url = tenantId ?
        `/htmx/voucher/add-posting-line?rowCounter=${rowCounter}&tenantId=${tenantId}` :
        `/htmx/voucher/add-posting-line?rowCounter=${rowCounter}`;


    // Use HTMX to fetch the new posting line
    htmx.ajax('GET', url, {
        target: '#postingLines',
        swap: 'beforeend',
        headers: {
            'HX-Request': 'true'
        }
    }).then(() => {
        rowCounter++;
        updateBalance();
    }).catch(error => {
        console.error('Error adding posting line:', error);
    });
}

// Remove posting line
function removePostingLine(button) {
    const row = button.closest('tr');
    const rows = document.querySelectorAll('.posting-line-row');

    // Don't remove the last row
    if (rows.length > 1) {
        row.remove();
        updateBalance();
    }
}

// Update balance by sending form data to server via HTMX
function updateBalance() {
    const form = document.getElementById('voucherForm');
    if (!form) return;

    // Use HTMX to submit the form data to the balance endpoint
    htmx.ajax('POST', '/htmx/voucher/update-balance', {
        source: form,
        target: '#balanceFooter',
        swap: 'innerHTML',
        headers: {
            'HX-Request': 'true'
        }
    }).then(() => {
        updateSaveButtonState();
    }).catch(error => {
        console.error('Error updating balance:', error);
    });
}


// Handle form validation before submission
// Show validation error message
function showValidationError(message) {
    const messagesDiv = document.getElementById('form-messages');
    if (messagesDiv) {
        messagesDiv.innerHTML = `<div style="margin-bottom: var(--wa-space-m);">
            <wa-callout variant="danger" open>
                <wa-icon slot="icon" name="circle-exclamation" variant="regular"></wa-icon>
                <span>${message}</span>
            </wa-callout></div>`;
    }
}

// Handle HTMX events and errors
document.addEventListener('htmx:responseError', (e) => {
    console.error('HTMX Error:', e.detail);
    showValidationError('An error occurred. Please try again.');
});

document.addEventListener('htmx:sendError', (e) => {
    console.error('HTMX Network Error:', e.detail);
    showValidationError('Network error. Please check your connection.');
});

// Auto-trigger balance update
document.addEventListener('change', (e) => {
    if (e.target.matches('input[type="number"], select, wa-input, wa-select')) {
        setTimeout(updateBalance, 100);
    }
});

// Configure HTMX requests with tenant ID and form data
document.addEventListener('htmx:configRequest', (e) => {
    const tenantId = new URLSearchParams(window.location.search).get('tenantId');
    if (tenantId && e.detail.path.includes('/htmx/')) {
        e.detail.parameters.tenantId = tenantId;
    }

    // Add r-combobox values to form data for all requests
    if (e.detail.elt && e.detail.elt.tagName === 'FORM') {
        const comboboxes = e.detail.elt.querySelectorAll('r-combobox');
        comboboxes.forEach(combobox => {
            if (combobox.name && combobox.value) {
                e.detail.parameters[combobox.name] = combobox.value;
            }
        });
    }
});
