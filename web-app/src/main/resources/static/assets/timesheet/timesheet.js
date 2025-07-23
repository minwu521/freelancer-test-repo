document.addEventListener('DOMContentLoaded', function() {
    initializeTimesheet();
    setupEventListeners();
});

function initializeTimesheet() {
    updateAllRowTotals();
    filterRowsByEmployee();
}

function setupEventListeners() {
    const employeeInput = document.getElementById('employee-input');
    if (employeeInput) {
        employeeInput.addEventListener('input', handleEmployeeInput);
        employeeInput.addEventListener('change', filterRowsByEmployee);
    }
    
    const hourInputs = document.querySelectorAll('.hour-input');
    hourInputs.forEach(input => {
        input.addEventListener('input', function() {
            updateRowTotal(this.closest('tr'));
        });
        
        input.addEventListener('blur', function() {
            saveIndividualEntry(this);
        });
    });
}

function handleEmployeeInput() {
    const employeeInput = document.getElementById('employee-input');
    const addEmployeeBtn = document.getElementById('add-employee-btn');
    const employeeList = document.getElementById('employee-list');
    
    if (!employeeInput || !addEmployeeBtn) return;
    
    const inputValue = employeeInput.value.trim();
    
    // Check if the input value exists in the datalist
    let exists = false;
    const options = employeeList.querySelectorAll('option');
    options.forEach(option => {
        if (option.value === inputValue) {
            exists = true;
        }
    });
    
    // Show "Add New Employee" button if the value doesn't exist and is not empty
    if (inputValue && !exists) {
        addEmployeeBtn.style.display = 'inline-block';
    } else {
        addEmployeeBtn.style.display = 'none';
    }
    
    filterRowsByEmployee();
}

function filterRowsByEmployee() {
    const employeeInput = document.getElementById('employee-input');
    if (!employeeInput) return;
    
    const selectedEmployee = employeeInput.value.trim();
    const rows = document.querySelectorAll('#timesheet-body tr');
    
    rows.forEach(row => {
        const rowEmployee = row.getAttribute('data-employee');
        if (!selectedEmployee || rowEmployee === selectedEmployee) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

function updateRowTotal(row) {
    const hourInputs = row.querySelectorAll('.hour-input');
    let total = 0;
    
    hourInputs.forEach(input => {
        const value = parseFloat(input.value) || 0;
        total += value;
    });
    
    const totalCell = row.querySelector('.total-hours');
    if (totalCell) {
        totalCell.textContent = total.toFixed(2);
    }
}

function updateAllRowTotals() {
    const rows = document.querySelectorAll('#timesheet-body tr');
    rows.forEach(updateRowTotal);
}

function addNewEmployee() {
    const employeeInput = document.getElementById('employee-input');
    const employeeList = document.getElementById('employee-list');
    const addEmployeeBtn = document.getElementById('add-employee-btn');
    
    if (!employeeInput || !employeeList) return;
    
    const newEmployeeName = employeeInput.value.trim();
    
    if (!newEmployeeName) {
        alert('Please enter an employee name');
        return;
    }
    
    // Add the new employee to the datalist
    const newOption = document.createElement('option');
    newOption.value = newEmployeeName;
    employeeList.appendChild(newOption);
    
    // Hide the add button
    if (addEmployeeBtn) {
        addEmployeeBtn.style.display = 'none';
    }
    
    // Filter rows to show only this employee's entries
    filterRowsByEmployee();
    
    alert(`Employee "${newEmployeeName}" has been added. You can now create timesheet entries for them.`);
}

function addNewRow() {
    const employeeInput = document.getElementById('employee-input');
    const newProjectSelect = document.getElementById('new-project');
    const newActivitySelect = document.getElementById('new-activity');
    
    if (!employeeInput || !newProjectSelect || !newActivitySelect) {
        console.error('Required form elements not found');
        return;
    }
    
    const selectedEmployee = employeeInput.value.trim();
    const newProject = newProjectSelect.value;
    const newActivity = newActivitySelect.value;
    
    if (!selectedEmployee) {
        alert('Please enter an employee name first');
        return;
    }
    
    if (!newProject || !newActivity) {
        alert('Please select both project and activity');
        return;
    }
    
    // Handle new project/activity creation
    let projectValue = newProject;
    let activityValue = newActivity;
    
    if (newProject === '__new__') {
        projectValue = prompt('Enter new project name:');
        if (!projectValue) return;
        
        // Add the new project to the select element
        const projectSelect = document.getElementById('new-project');
        const newProjectOption = document.createElement('option');
        newProjectOption.value = projectValue;
        newProjectOption.textContent = projectValue;
        // Insert before the "Add New Project" option
        const addNewOption = projectSelect.querySelector('option[value="__new__"]');
        projectSelect.insertBefore(newProjectOption, addNewOption);
    }
    
    if (newActivity === '__new__') {
        activityValue = prompt('Enter new activity name:');
        if (!activityValue) return;
        
        // Add the new activity to the select element
        const activitySelect = document.getElementById('new-activity');
        const newActivityOption = document.createElement('option');
        newActivityOption.value = activityValue;
        newActivityOption.textContent = activityValue;
        // Insert before the "Add New Activity" option
        const addNewOption = activitySelect.querySelector('option[value="__new__"]');
        activitySelect.insertBefore(newActivityOption, addNewOption);
    }
    
    const tbody = document.getElementById('timesheet-body');
    const newRow = document.createElement('tr');
    newRow.setAttribute('data-employee', selectedEmployee);
    newRow.setAttribute('data-project', projectValue);
    newRow.setAttribute('data-activity', activityValue);
    
    newRow.innerHTML = `
        <td>${projectValue}</td>
        <td>${activityValue}</td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="1"></td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="2"></td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="3"></td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="4"></td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="5"></td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="6"></td>
        <td><input type="number" step="0.25" min="0" max="24" class="hour-input" data-day="7"></td>
        <td class="total-hours">0.00</td>
        <td><button class="delete-row" onclick="deleteRow(this)">Delete</button></td>
    `;
    
    tbody.appendChild(newRow);
    
    // Set up event listeners for new inputs
    const newInputs = newRow.querySelectorAll('.hour-input');
    newInputs.forEach(input => {
        input.addEventListener('input', function() {
            updateRowTotal(this.closest('tr'));
        });
        
        input.addEventListener('blur', function() {
            saveIndividualEntry(this);
        });
    });
    
    // Clear selections
    if (newProjectSelect) newProjectSelect.value = '';
    if (newActivitySelect) newActivitySelect.value = '';
    
    // Update visibility based on current employee filter
    filterRowsByEmployee();
}

function deleteRow(button) {
    if (confirm('Are you sure you want to delete this row?')) {
        const row = button.closest('tr');
        row.remove();
    }
}

function saveIndividualEntry(input) {
    const row = input.closest('tr');
    const employeeName = row.getAttribute('data-employee');
    const project = row.getAttribute('data-project');
    const activity = row.getAttribute('data-activity');
    const dayNumber = input.getAttribute('data-day');
    const hours = parseFloat(input.value) || 0;
    
    if (hours === 0) return; // Don't save empty entries
    
    // Calculate the actual date based on current week and day number
    const currentDate = new Date(); // This should come from the page context
    const entryDate = calculateDateFromDay(currentDate, dayNumber);
    
    const formData = new FormData();
    formData.append('employeeName', employeeName);
    formData.append('project', project);
    formData.append('activity', activity);
    formData.append('entryDate', entryDate);
    formData.append('hours', hours);
    
    fetch('/timesheet/entry', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(data => {
        console.log('Entry saved:', data);
    })
    .catch(error => {
        console.error('Error saving entry:', error);
        alert('Failed to save entry. Please try again.');
    });
}

function calculateDateFromDay(currentDate, dayNumber) {
    // Get the start of the current week (Monday)
    const day = currentDate.getDay();
    const diff = currentDate.getDate() - day + (day === 0 ? -6 : 1);
    const monday = new Date(currentDate.setDate(diff));
    
    // Add days to get the target date
    const targetDate = new Date(monday);
    targetDate.setDate(monday.getDate() + parseInt(dayNumber) - 1);
    
    return targetDate.toISOString().split('T')[0];
}

function saveTimesheet() {
    const rows = document.querySelectorAll('#timesheet-body tr:not([style*="display: none"])');
    let savePromises = [];
    
    rows.forEach(row => {
        const employeeName = row.getAttribute('data-employee');
        const project = row.getAttribute('data-project');
        const activity = row.getAttribute('data-activity');
        const hourInputs = row.querySelectorAll('.hour-input');
        
        hourInputs.forEach(input => {
            const hours = parseFloat(input.value) || 0;
            if (hours > 0) {
                const dayNumber = input.getAttribute('data-day');
                const entryDate = calculateDateFromDay(new Date(), dayNumber);
                
                const formData = new FormData();
                formData.append('employeeName', employeeName);
                formData.append('project', project);
                formData.append('activity', activity);
                formData.append('entryDate', entryDate);
                formData.append('hours', hours);
                
                const promise = fetch('/timesheet/entry', {
                    method: 'POST',
                    body: formData
                });
                
                savePromises.push(promise);
            }
        });
    });
    
    Promise.all(savePromises)
        .then(() => {
            alert('All entries saved successfully!');
        })
        .catch(error => {
            console.error('Error saving timesheet:', error);
            alert('Some entries failed to save. Please check and try again.');
        });
}