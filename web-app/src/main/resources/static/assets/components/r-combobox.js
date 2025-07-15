import { LitElement, html, css } from 'lit';

/**
 * r-combobox - A searchable combobox component built with Lit.dev
 * 
 * Features:
 * - Searchable dropdown with filtering
 * - Keyboard navigation (Arrow keys, Enter, Escape)
 * - Click outside to close
 * - Clear button
 * - Hidden input for form submission
 * - Flexible item structure (title, subtitle, description, meta)
 * 
 * Usage:
 * <r-combobox 
 *   id="my-combo"
 *   placeholder="Search..."
 *   name="fieldName"
 *   value="initialValue">
 * </r-combobox>
 * 
 * Then set items via JavaScript:
 * document.getElementById('my-combo').items = [
 *   { value: '1', title: 'Option 1', description: 'Description' },
 *   { value: '2', title: 'Option 2', subtitle: '(subtitle)', description: 'Description' }
 * ];
 */
class RCombobox extends LitElement {
    static properties = {
        placeholder: { type: String },
        value: { type: String },
        name: { type: String },
        tabindex: { type: String },
        items: { type: Array },
        isOpen: { type: Boolean, state: true },
        searchQuery: { type: String, state: true },
        highlightedIndex: { type: Number, state: true },
        displayValue: { type: String, state: true }
    };

    static styles = css`
        :host {
            display: block;
            position: relative;
            font-family: inherit;
        }

        .combobox-wrapper {
            position: relative;
        }

        .combobox-input {
            width: 100%;
            padding: 0.5rem 0.75rem;
            font-size: 0.875rem;
            border: 1px solid var(--wa-color-gray-90);
            border-radius: 0.375rem;
            background-color: white;
            transition: all 0.15s ease-in-out;
            box-sizing: border-box;
        }

        .combobox-input:focus {
            outline: none;
            border-color: var(--wa-color-blue-70);
            box-shadow: 0 0 0 3px var(--wa-color-blue-95);
        }

        .combobox-input.has-error {
            border-color: var(--wa-color-red-50);
            background-color: var(--wa-color-gray-95);
        }

        .dropdown {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            margin-top: 0.25rem;
            background: white;
            border: 1px solid var(--wa-color-gray-90);
            border-radius: 0.375rem;
            box-shadow: 0 10px 15px -3px var(--wa-color-gray-95);
            max-height: 300px;
            overflow-y: auto;
            z-index: 1000;
            display: none;
        }

        .dropdown.open {
            display: block;
        }

        .dropdown-item {
            padding: 0.5rem 0.75rem;
            cursor: pointer;
            transition: background-color 0.15s ease-in-out;
            border-bottom: 1px solid var(--wa-color-gray-95);
        }

        .dropdown-item:last-child {
            border-bottom: none;
        }

        .dropdown-item:hover,
        .dropdown-item.highlighted {
            background-color: var(--wa-color-gray-95);
        }

        .dropdown-item.selected {
            background-color: var(--wa-color-blue-95);
        }

        .item-title {
            font-weight: 500;
            font-size: 0.875rem;
            color: var(--wa-color-gray-10);
            margin-bottom: 0.125rem;
        }

        .item-description {
            font-size: 0.75rem;
            color: var(--wa-color-gray-40);
            line-height: 1.4;
        }

        .item-meta {
            font-size: 0.7rem;
            color: var(--wa-color-gray-50);
            margin-top: 0.125rem;
        }

        .no-results {
            padding: 1rem;
            text-align: center;
            color: var(--wa-color-gray-50);
            font-style: italic;
            font-size: 0.875rem;
        }

        .clear-button {
            position: absolute;
            right: 0.5rem;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            color: var(--wa-color-gray-50);
            cursor: pointer;
            padding: 0.25rem;
            display: none;
        }

        .clear-button:hover {
            color: var(--wa-color-gray-30);
        }

        .combobox-input:not(:placeholder-shown) ~ .clear-button {
            display: block;
        }
    `;

    constructor() {
        super();
        this.placeholder = 'Search...';
        this.value = '';
        this.name = '';
        this.tabindex = '';
        this.items = [];
        this.isOpen = false;
        this.searchQuery = '';
        this.highlightedIndex = -1;
        this.displayValue = '';
    }

    connectedCallback() {
        super.connectedCallback();
        // Listen for clicks outside to close dropdown
        this._handleOutsideClick = this._handleOutsideClick.bind(this);
        document.addEventListener('click', this._handleOutsideClick);
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener('click', this._handleOutsideClick);
    }

    updated(changedProperties) {
        // This is the key change: when items or value are updated,
        // we ensure the display text is correctly set. This solves the race condition.
        if ((changedProperties.has('items') || changedProperties.has('value')) && this.value) {
            const item = this.items.find(i => i.value === this.value);
            if (item) {
                const newDisplayValue = item.displayText || `${item.title}${item.subtitle ? ' ' + item.subtitle : ''}`;
                // Only update if it's different to avoid re-rendering loops
                if (this.displayValue !== newDisplayValue) {
                    this.displayValue = newDisplayValue;
                    this.searchQuery = newDisplayValue; // Also update searchQuery to show in input
                }
            } else {
                this.displayValue = ''; // Clear if value is not in items
            }
        }
    }

    _handleOutsideClick(e) {
        if (!this.contains(e.target)) {
            this.isOpen = false;
        }
    }

    get filteredItems() {
        if (!this.searchQuery.trim()) {
            return this.items;
        }

        const query = this.searchQuery.toLowerCase();
        return this.items.filter(item => {
            const titleMatch = item.title?.toLowerCase().includes(query);
            const descriptionMatch = item.description?.toLowerCase().includes(query);
            const subtitleMatch = item.subtitle?.toLowerCase().includes(query);
            const metaMatch = item.meta?.toLowerCase().includes(query);
            return titleMatch || descriptionMatch || subtitleMatch || metaMatch;
        });
    }

    _handleInput(e) {
        this.searchQuery = e.target.value;
        this.isOpen = true;
        this.highlightedIndex = 0; // Highlight first item when searching

        // Reset value when user types (unless they're typing the exact display value)
        if (this.displayValue && e.target.value !== this.displayValue) {
            this.value = '';
            this.dispatchEvent(new CustomEvent('change', {
                detail: { value: '', item: null }
            }));
        }
    }

    _handleKeyDown(e) {
        const items = this.filteredItems;

        switch(e.key) {
            case 'ArrowDown':
                e.preventDefault();
                this.isOpen = true;
                this.highlightedIndex = Math.min(this.highlightedIndex + 1, items.length - 1);
                break;

            case 'ArrowUp':
                e.preventDefault();
                this.highlightedIndex = Math.max(this.highlightedIndex - 1, -1);
                break;

            case 'Enter':
                e.preventDefault();
                if (this.isOpen && items.length > 0) {
                    // If only one item, select it
                    if (items.length === 1) {
                        this._selectItem(items[0]);
                    } else if (this.highlightedIndex >= 0 && this.highlightedIndex < items.length) {
                        this._selectItem(items[this.highlightedIndex]);
                    }
                }
                break;

            case 'Escape':
                this.isOpen = false;
                this.highlightedIndex = -1;
                break;

            case 'Tab':
                this.isOpen = false;
                break;
        }
    }

    _handleFocus() {
        if (!this.value) {
            this.isOpen = true;
            this.highlightedIndex = 0;
        }
    }

    _handleClick() {
        // Toggle dropdown when clicking on input
        if (this.value && this.searchQuery === this.displayValue) {
            // Clear and show all options
            this.searchQuery = '';
            const input = this.shadowRoot.querySelector('.combobox-input');
            input.value = '';
            this.isOpen = true;
            this.highlightedIndex = 0;
        } else {
            this.isOpen = true;
        }
    }

    _selectItem(item) {
        this.value = item.value;
        this.displayValue = item.displayText || `${item.title}${item.subtitle ? ' ' + item.subtitle : ''}`;
        this.searchQuery = this.displayValue;
        this.isOpen = false;
        this.highlightedIndex = -1;

        // Update input value
        const input = this.shadowRoot.querySelector('.combobox-input');
        input.value = this.displayValue;

        // Dispatch change event
        this.dispatchEvent(new CustomEvent('change', {
            detail: { value: item.value, item }
        }));
    }

    _clearValue(e) {
        e.stopPropagation();
        this.value = '';
        this.displayValue = '';
        this.searchQuery = '';
        this.highlightedIndex = -1;

        const input = this.shadowRoot.querySelector('.combobox-input');
        input.value = '';
        input.focus();

        this.dispatchEvent(new CustomEvent('change', {
            detail: { value: '', item: null }
        }));
    }

    firstUpdated() {
        // Set initial input value
        const input = this.shadowRoot.querySelector('.combobox-input');
        if (this.displayValue) {
            input.value = this.displayValue;
            this.searchQuery = this.displayValue;
        }
    }

    render() {
        const items = this.filteredItems;

        return html`
            <div class="combobox-wrapper">
                <input
                    type="text"
                    class="combobox-input"
                    placeholder="${this.placeholder}"
                    tabindex="${this.tabindex || ''}"
                    @input="${this._handleInput}"
                    @keydown="${this._handleKeyDown}"
                    @focus="${this._handleFocus}"
                    @click="${this._handleClick}"
                    .value="${this.searchQuery}"
                />
                ${this.searchQuery ? html`
                    <button class="clear-button" @click="${this._clearValue}" type="button" tabindex="-1">
                        ✕
                    </button>
                ` : ''}

                <div class="dropdown ${this.isOpen ? 'open' : ''}">
                    ${items.length === 0 ? html`
                        <div class="no-results">No items found</div>
                    ` : items.map((item, index) => html`
                        <div
                            class="dropdown-item ${index === this.highlightedIndex ? 'highlighted' : ''} ${item.value === this.value ? 'selected' : ''}"
                            @click="${() => this._selectItem(item)}"
                            @mouseenter="${() => this.highlightedIndex = index}"
                        >
                            <div class="item-title">
                                ${item.title}${item.subtitle ? html` ${item.subtitle}` : ''}
                            </div>
                            ${item.description ? html`
                                <div class="item-description">${item.description}</div>
                            ` : ''}
                            ${item.meta ? html`
                                <div class="item-meta">${item.meta}</div>
                            ` : ''}
                        </div>
                    `)}
                </div>

                ${this.name ? html`
                    <input type="hidden" name="${this.name}" .value="${this.value}" />
                ` : ''}
            </div>
        `;
    }
}

customElements.define('r-combobox', RCombobox); 