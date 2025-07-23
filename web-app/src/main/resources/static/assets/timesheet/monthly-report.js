document.addEventListener('DOMContentLoaded', function() {
    initializeMonthlyReport();
});

function initializeMonthlyReport() {
    // Initialize any interactive elements
    const approvedCheckbox = document.getElementById('approved');
    const approvalDate = document.getElementById('approvalDate');
    
    if (approvedCheckbox) {
        approvedCheckbox.addEventListener('change', function() {
            if (this.checked) {
                approvalDate.disabled = false;
            } else {
                approvalDate.disabled = true;
            }
        });
    }
    
    // Auto-submit form when dropdown values change
    const employeeSelect = document.getElementById('employeeName');
    const monthYearInput = document.getElementById('monthYear');
    
    if (employeeSelect) {
        employeeSelect.addEventListener('change', function() {
            this.form.submit();
        });
    }
    
    if (monthYearInput) {
        monthYearInput.addEventListener('change', function() {
            this.form.submit();
        });
    }
    
    // Highlight weekend columns (if needed)
    highlightWeekends();
}

function highlightWeekends() {
    // This function could highlight weekend columns in the table
    // For now, it's a placeholder for future enhancement
    const table = document.querySelector('.monthly-report-table');
    if (!table) return;
    
    // Weekend highlighting logic would go here
    // This would require knowing which days of the month fall on weekends
}

function printReport() {
    window.print();
}

function exportReport() {
    // Placeholder for export functionality
    alert('Export functionality to be implemented');
}

// Add print and export buttons if needed
function addReportActions() {
    const reportHeader = document.querySelector('.report-header');
    if (!reportHeader) return;
    
    const actionsDiv = document.createElement('div');
    actionsDiv.className = 'report-actions';
    actionsDiv.style.marginTop = '10px';
    
    const printBtn = document.createElement('button');
    printBtn.textContent = 'Print Report';
    printBtn.className = 'btn btn-secondary';
    printBtn.onclick = printReport;
    
    const exportBtn = document.createElement('button');
    exportBtn.textContent = 'Export to Excel';
    exportBtn.className = 'btn btn-secondary';
    exportBtn.style.marginLeft = '10px';
    exportBtn.onclick = exportReport;
    
    actionsDiv.appendChild(printBtn);
    actionsDiv.appendChild(exportBtn);
    reportHeader.appendChild(actionsDiv);
}