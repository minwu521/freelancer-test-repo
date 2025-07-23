document.addEventListener('DOMContentLoaded', function() {
    initializeTimesheet();
    setupEventListeners();
    
    // Hide comment rows when clicking outside
    document.addEventListener('click', function(event) {
        if (!event.target.closest('.hour-input') && !event.target.closest('.comment-input')) {
            document.querySelectorAll('.comment-row').forEach(row => {
                row.style.display = 'none';
            });
        }
    });
});

function initializeTimesheet() {
    updateAllRowTotals();
    filterRowsByEmployee();
    initializeCommentIndicators();
}

function initializeCommentIndicators() {
    // Add visual indicators for inputs that have comments
    const hourInputs = document.querySelectorAll('.hour-input[data-comment]');
    hourInputs.forEach(input => {
        const comment = input.getAttribute('data-comment');
        if (comment && comment.trim() !== '') {
            input.classList.add('has-comment');
        }
    });
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
        
        input.addEventListener('focus', function() {
            showCommentRow(this);
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
            // Also show/hide associated comment row
            if (row.classList.contains('main-row')) {
                const commentRow = row.nextElementSibling;
                if (commentRow && commentRow.classList.contains('comment-row')) {
                    commentRow.style.display = commentRow.style.display; // Keep existing visibility
                }
            }
        } else {
            row.style.display = 'none';
            // Also hide associated comment row
            if (row.classList.contains('main-row')) {
                const commentRow = row.nextElementSibling;
                if (commentRow && commentRow.classList.contains('comment-row')) {
                    commentRow.style.display = 'none';
                }
            }
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
    const rows = document.querySelectorAll('#timesheet-body tr.main-row');
    rows.forEach(updateRowTotal);
}

// Store comments locally to prevent losing them
let commentStorage = {};

function getCommentKey(employeeName, project, activity, dayNumber) {
    const currentDateElement = document.querySelector('[data-current-date]');
    const currentDate = currentDateElement ? 
        new Date(currentDateElement.getAttribute('data-current-date')) : 
        new Date();
    const entryDate = calculateDateFromDay(currentDate, dayNumber);
    return `${employeeName}-${project}-${activity}-${entryDate}`;
}

function showCommentRow(hourInput) {
    // Hide all comment rows first
    document.querySelectorAll('.comment-row').forEach(row => {
        row.style.display = 'none';
    });
    
    const mainRow = hourInput.closest('tr');
    const dayNumber = hourInput.getAttribute('data-day');
    
    // Check if comment row already exists
    let commentRow = mainRow.nextElementSibling;
    if (!commentRow || !commentRow.classList.contains('comment-row')) {
        // Create comment row
        commentRow = createCommentRow(mainRow);
        mainRow.insertAdjacentElement('afterend', commentRow);
    }
    
    // Update the comment row for the focused day
    updateCommentRowForDay(commentRow, dayNumber);
    
    // Show the comment row
    commentRow.style.display = '';
}

function createCommentRow(mainRow) {
    const commentRow = document.createElement('tr');
    commentRow.classList.add('comment-row');
    commentRow.setAttribute('data-employee', mainRow.getAttribute('data-employee'));
    commentRow.setAttribute('data-project', mainRow.getAttribute('data-project'));
    commentRow.setAttribute('data-activity', mainRow.getAttribute('data-activity'));
    
    // Initial empty structure - will be filled by updateCommentRowForDay
    commentRow.innerHTML = `
        <td colspan="2" class="comment-label">Comment:</td>
        <td colspan="7" class="comment-cell"></td>
        <td colspan="2"></td>
    `;
    
    return commentRow;
}

function updateCommentRowForDay(commentRow, dayNumber) {
    const dayNames = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    const dayName = dayNames[parseInt(dayNumber) - 1];
    
    const employeeName = commentRow.getAttribute('data-employee');
    const project = commentRow.getAttribute('data-project');
    const activity = commentRow.getAttribute('data-activity');
    const commentKey = getCommentKey(employeeName, project, activity, dayNumber);
    
    const commentCell = commentRow.querySelector('.comment-cell');
    if (commentCell) {
        commentCell.innerHTML = `
            <textarea class="comment-input" data-day="${dayNumber}" placeholder="Comment for ${dayName}"></textarea>
        `;
        
        const commentInput = commentCell.querySelector('.comment-input');
        if (commentInput) {
            // Try to get existing comment from the hour input's data attribute first
            const mainRow = commentRow.previousElementSibling;
            const hourInput = mainRow ? mainRow.querySelector(`.hour-input[data-day="${dayNumber}"]`) : null;
            const existingComment = hourInput ? hourInput.getAttribute('data-comment') : '';
            
            // Load existing comment from local storage first, then from template data
            if (commentStorage[commentKey]) {
                commentInput.value = commentStorage[commentKey];
            } else if (existingComment) {
                commentInput.value = existingComment;
                // Store in local storage for future use
                commentStorage[commentKey] = existingComment;
            }
            
            // Add event listeners
            commentInput.addEventListener('input', function() {
                // Store comment locally as user types
                commentStorage[commentKey] = this.value;
            });
            
            commentInput.addEventListener('blur', function() {
                // Update the hour input's data attribute and visual indicator
                const mainRow = commentRow.previousElementSibling;
                const hourInput = mainRow ? mainRow.querySelector(`.hour-input[data-day="${dayNumber}"]`) : null;
                if (hourInput) {
                    hourInput.setAttribute('data-comment', this.value);
                    if (this.value && this.value.trim() !== '') {
                        hourInput.classList.add('has-comment');
                    } else {
                        hourInput.classList.remove('has-comment');
                    }
                }
                
                // Save to server
                saveIndividualEntry(this);
            });
        }
    }
}

function loadExistingComment(commentRow, dayNumber) {
    const employeeName = commentRow.getAttribute('data-employee');
    const project = commentRow.getAttribute('data-project');
    const activity = commentRow.getAttribute('data-activity');
    
    // Calculate the date for this day
    const currentDateElement = document.querySelector('[data-current-date]');
    const currentDate = currentDateElement ? 
        new Date(currentDateElement.getAttribute('data-current-date')) : 
        new Date();
    const entryDate = calculateDateFromDay(currentDate, dayNumber);
    
    // Fetch existing comment from server
    const params = new URLSearchParams({
        employeeName: employeeName,
        project: project,
        activity: activity,
        entryDate: entryDate
    });
    
    fetch(`/timesheet/entry/comment?${params}`)
        .then(response => {
            if (response.ok) {
                return response.text();
            }
            return '';
        })
        .then(comment => {
            const commentInput = commentRow.querySelector(`.comment-input[data-day="${dayNumber}"]`);
            if (commentInput && comment) {
                commentInput.value = comment;
                // Store in local storage for future use
                const commentKey = getCommentKey(employeeName, project, activity, dayNumber);
                commentStorage[commentKey] = comment;
            }
        })
        .catch(error => {
            console.log('No existing comment found:', error);
        });
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
    newRow.classList.add('main-row');
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
        
        input.addEventListener('focus', function() {
            showCommentRow(this);
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
    
    let hours = 0;
    let comments = '';
    
    if (input.classList.contains('hour-input')) {
        hours = parseFloat(input.value) || 0;
        // Get comment from comment row if it exists
        const commentRow = row.nextElementSibling;
        if (commentRow && commentRow.classList.contains('comment-row')) {
            const commentInput = commentRow.querySelector(`.comment-input[data-day="${dayNumber}"]`);
            if (commentInput) {
                comments = commentInput.value || '';
            }
        }
    } else if (input.classList.contains('comment-input')) {
        // This is a comment input, get hours from main row
        const mainRow = row.previousElementSibling;
        if (mainRow && mainRow.classList.contains('main-row')) {
            const hourInput = mainRow.querySelector(`.hour-input[data-day="${dayNumber}"]`);
            if (hourInput) {
                hours = parseFloat(hourInput.value) || 0;
            }
        }
        comments = input.value || '';
        console.log('DEBUG: Saving comment -', { employeeName, project, activity, dayNumber, hours, comments });
    }
    
    if (hours === 0 && (!comments || comments.trim() === '')) return; // Don't save if both are empty
    
    // Calculate the actual date based on current week and day number
    // Try to get the current date from the page context, fallback to today
    const currentDateElement = document.querySelector('[data-current-date]');
    const currentDate = currentDateElement ? 
        new Date(currentDateElement.getAttribute('data-current-date')) : 
        new Date();
    const entryDate = calculateDateFromDay(currentDate, dayNumber);
    
    const formData = new FormData();
    formData.append('employeeName', employeeName);
    formData.append('project', project);
    formData.append('activity', activity);
    formData.append('entryDate', entryDate);
    formData.append('hours', hours);
    formData.append('comments', comments);
    
    console.log('DEBUG: Sending to server -', { employeeName, project, activity, entryDate, hours, comments });
    
    fetch('/timesheet/entry', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        console.log('DEBUG: Response status:', response.status);
        return response.text();
    })
    .then(data => {
        console.log('DEBUG: Server response:', data);
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