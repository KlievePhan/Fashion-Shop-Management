// Sample Data
let products = [
    { id: 1, name: 'Classic T-Shirt', category: 'T-Shirts', price: 29.99, stock: 150 },
    { id: 2, name: 'Denim Jeans', category: 'Jeans', price: 79.99, stock: 85 },
    { id: 3, name: 'Leather Jacket', category: 'Jackets', price: 199.99, stock: 42 },
    { id: 4, name: 'Summer Dress', category: 'Accessories', price: 59.99, stock: 68 },
    { id: 5, name: 'Polo Shirt', category: 'T-Shirts', price: 39.99, stock: 120 }
];

let orders = [
    { id: 'ORD-001', customer: 'John Doe', date: '2025-11-05', amount: 125.00, status: 'Purchased' },
    { id: 'ORD-002', customer: 'Jane Smith', date: '2025-11-05', amount: 89.50, status: 'pending' },
    { id: 'ORD-003', customer: 'Mike Johnson', date: '2025-11-04', amount: 245.00, status: 'shipped' },
    { id: 'ORD-004', customer: 'Sarah Williams', date: '2025-11-04', amount: 159.99, status: 'delivered' },
    { id: 'ORD-005', customer: 'Tom Brown', date: '2025-11-03', amount: 99.99, status: 'pending' }
];

let customers = [
    { id: 1, name: 'John Doe', email: 'john@example.com', orders: 8, spent: 856.00 },
    { id: 2, name: 'Jane Smith', email: 'jane@example.com', orders: 5, spent: 445.50 },
    { id: 3, name: 'Mike Johnson', email: 'mike@example.com', orders: 12, spent: 1245.00 },
    { id: 4, name: 'Sarah Williams', email: 'sarah@example.com', orders: 3, spent: 389.99 },
    { id: 5, name: 'Tom Brown', email: 'tom@example.com', orders: 6, spent: 599.99 }
];

let editingProductId = null;

// DOM Elements
const navItems = document.querySelectorAll('.nav-item');
const sections = document.querySelectorAll('.content-section');
const pageTitle = document.getElementById('pageTitle');
const menuToggle = document.getElementById('menuToggle');
const sidebar = document.querySelector('.sidebar');
const productModal = document.getElementById('productModal');
const addProductBtn = document.getElementById('addProductBtn');
const closeModalBtns = document.querySelectorAll('.close-modal');
const productForm = document.getElementById('productForm');
const searchInput = document.getElementById('searchInput');
const orderStatusFilter = document.getElementById('orderStatus');

// Navigation
navItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const targetSection = item.dataset.section;

        // Update active nav item
        navItems.forEach(nav => nav.classList.remove('active'));
        item.classList.add('active');

        // Show target section
        sections.forEach(section => section.classList.remove('active'));
        document.getElementById(targetSection).classList.add('active');

        // Update page title
        pageTitle.textContent = item.textContent.trim();

        // Close sidebar on mobile
        if (window.innerWidth <= 768) {
            sidebar.classList.remove('active');
        }
    });
});

// Mobile menu toggle
menuToggle.addEventListener('click', () => {
    sidebar.classList.toggle('active');
});

// Close sidebar when clicking outside
document.addEventListener('click', (e) => {
    if (window.innerWidth <= 768 &&
        !sidebar.contains(e.target) &&
        !menuToggle.contains(e.target)) {
        sidebar.classList.remove('active');
    }
});

// Modal functions
function openModal() {
    productModal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeModal() {
    productModal.classList.remove('active');
    document.body.style.overflow = 'auto';
    productForm.reset();
    editingProductId = null;
    document.getElementById('modalTitle').textContent = 'Add New Product';
}

addProductBtn.addEventListener('click', openModal);

closeModalBtns.forEach(btn => {
    btn.addEventListener('click', closeModal);
});

productModal.addEventListener('click', (e) => {
    if (e.target === productModal) {
        closeModal();
    }
});

// Product Form Submit
productForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const name = document.getElementById('productName').value;
    const category = document.getElementById('productCategory').value;
    const price = parseFloat(document.getElementById('productPrice').value);
    const stock = parseInt(document.getElementById('productStock').value);

    if (editingProductId) {
        // Update existing product
        const product = products.find(p => p.id === editingProductId);
        product.name = name;
        product.category = category;
        product.price = price;
        product.stock = stock;
    } else {
        // Add new product
        const newProduct = {
            id: products.length > 0 ? Math.max(...products.map(p => p.id)) + 1 : 1,
            name,
            category,
            price,
            stock
        };
        products.push(newProduct);
    }

    renderProducts();
    closeModal();
    showNotification(editingProductId ? 'Product updated successfully!' : 'Product added successfully!');
});

// Render Products
function renderProducts(filter = '') {
    const tbody = document.getElementById('productsTable');
    const filteredProducts = products.filter(p =>
        p.name.toLowerCase().includes(filter.toLowerCase()) ||
        p.category.toLowerCase().includes(filter.toLowerCase())
    );

    tbody.innerHTML = filteredProducts.map(product => `
        <tr>
            <td>${product.id}</td>
            <td>${product.name}</td>
            <td>${product.category}</td>
            <td>$${product.price.toFixed(2)}</td>
            <td>${product.stock}</td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm btn-secondary" onclick="editProduct(${product.id})">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// Edit Product
function editProduct(id) {
    const product = products.find(p => p.id === id);
    if (product) {
        editingProductId = id;
        document.getElementById('modalTitle').textContent = 'Edit Product';
        document.getElementById('productName').value = product.name;
        document.getElementById('productCategory').value = product.category;
        document.getElementById('productPrice').value = product.price;
        document.getElementById('productStock').value = product.stock;
        openModal();
    }
}

// Delete Product
function deleteProduct(id) {
    if (confirm('Are you sure you want to delete this product?')) {
        products = products.filter(p => p.id !== id);
        renderProducts();
        showNotification('Product deleted successfully!');
    }
}

// Render Orders
function renderOrders(statusFilter = 'all') {
    const tbody = document.getElementById('ordersTable');
    const filteredOrders = statusFilter === 'all'
        ? orders
        : orders.filter(o => o.status === statusFilter);

    tbody.innerHTML = filteredOrders.map(order => `
        <tr>
            <td>#${order.id}</td>
            <td>${order.customer}</td>
            <td>${order.date}</td>
            <td>$${order.amount.toFixed(2)}</td>
            <td><span class="badge ${getBadgeClass(order.status)}">${capitalizeFirst(order.status)}</span></td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm btn-secondary" onclick="viewOrder('${order.id}')">View</button>
                    <button class="btn btn-sm btn-primary" onclick="updateOrderStatus('${order.id}')">Update</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// View Order
function viewOrder(id) {
    const order = orders.find(o => o.id === id);
    if (order) {
        alert(`Order Details:\n\nID: #${order.id}\nCustomer: ${order.customer}\nDate: ${order.date}\nAmount: $${order.amount.toFixed(2)}\nStatus: ${capitalizeFirst(order.status)}`);
    }
}

// Update Order Status
function updateOrderStatus(id) {
    const order = orders.find(o => o.id === id);
    if (order) {
        const statuses = ['pending', 'shipped', 'delivered'];
        const currentIndex = statuses.indexOf(order.status);
        const nextIndex = (currentIndex + 1) % statuses.length;
        order.status = statuses[nextIndex];
        renderOrders(orderStatusFilter.value);
        showNotification('Order status updated!');
    }
}

// Render Customers
function renderCustomers() {
    const tbody = document.getElementById('customersTable');
    tbody.innerHTML = customers.map(customer => `
        <tr>
            <td>${customer.id}</td>
            <td>${customer.name}</td>
            <td>${customer.email}</td>
            <td>${customer.orders}</td>
            <td>$${customer.spent.toFixed(2)}</td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm btn-secondary" onclick="viewCustomer(${customer.id})">View</button>
                </div>
            </td>
        </tr>
    `).join('');
}

// View Customer
function viewCustomer(id) {
    const customer = customers.find(c => c.id === id);
    if (customer) {
        alert(`Customer Details:\n\nID: ${customer.id}\nName: ${customer.name}\nEmail: ${customer.email}\nTotal Orders: ${customer.orders}\nTotal Spent: $${customer.spent.toFixed(2)}`);
    }
}

// Utility Functions
function getBadgeClass(status) {
    const classes = {
        'purchased': 'success',
        'pending': 'warning',
        'shipped': 'info'
    };
    return classes[status] || 'info';
}

function capitalizeFirst(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function showNotification(message) {
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: linear-gradient(135deg, #dc2626, #991b1b);
        color: white;
        padding: 1rem 2rem;
        border-radius: 8px;
        box-shadow: 0 5px 15px rgba(220, 38, 38, 0.4);
        z-index: 3000;
        animation: slideIn 0.3s ease;
    `;

    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.animation = 'slideOut 0.3s ease';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// Search functionality
searchInput.addEventListener('input', (e) => {
    const query = e.target.value;
    const activeSection = document.querySelector('.content-section.active').id;

    if (activeSection === 'products') {
        renderProducts(query);
    }
});

// Order status filter
orderStatusFilter.addEventListener('change', (e) => {
    renderOrders(e.target.value);
});

// Add CSS for animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    @keyframes slideOut {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(400px);
            opacity: 0;
        }
    }
`;
document.head.appendChild(style);

// Initialize data on page load
document.addEventListener('DOMContentLoaded', () => {
    renderProducts();
    renderOrders();
    renderCustomers();
});

// Make functions globally accessible
window.editProduct = editProduct;
window.deleteProduct = deleteProduct;
window.viewOrder = viewOrder;
window.updateOrderStatus = updateOrderStatus;
window.viewCustomer = viewCustomer;

/* =====================
   HEADER UPDATE FUNCTIONS
===================== */

// Update header subtitle based on active section
function updateHeaderSubtitle(section) {
    const subtitles = {
        'dashboard': 'Welcome back, Admin',
        'products': 'Manage your product inventory',
        'orders': 'View and manage customer orders',
        'customers': 'Customer information and analytics',
        'analytics': 'Business insights and reports'
    };

    const headerSubtitle = document.getElementById('headerSubtitle');
    if (headerSubtitle) {
        headerSubtitle.textContent = subtitles[section] || 'Welcome back, Admin';
    }
}

// Update dashboard stats in header
function updateHeaderStats() {
    const ordersToday = Math.floor(Math.random() * 20) + 5; // Random 5-25
    const revenueToday = (Math.random() * 5000 + 1000).toFixed(0); // Random $1000-6000

    const ordersTodayEl = document.getElementById('ordersToday');
    const revenueTodayEl = document.getElementById('revenueToday');

    if (ordersTodayEl) ordersTodayEl.textContent = ordersToday;
    if (revenueTodayEl) revenueTodayEl.textContent = '$' + revenueToday;
}

// Notification click handler
document.addEventListener('DOMContentLoaded', () => {
    const notificationBtn = document.getElementById('notificationBtn');
    if (notificationBtn) {
        notificationBtn.addEventListener('click', () => {
            showNotificationPanel();
        });
    }

    // Update stats on load
    updateHeaderStats();
});

// Show notification panel
function showNotificationPanel() {
    const notifications = [
        '📦 New order #ORD-006 received',
        '✅ 5 products are low on stock',
        '💬 You have 2 new customer messages'
    ];

    let message = 'Notifications:\n\n';
    notifications.forEach((notif, index) => {
        message += `${index + 1}. ${notif}\n`;
    });

    alert(message);
}

// Update header when navigation changes (modify existing nav click handler)
const originalNavClickHandler = document.querySelectorAll('.nav-item')[0]?.onclick;

document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', function (e) {
        const section = this.dataset.section;
        updateHeaderSubtitle(section);

        // Remove old onclick and add new one if needed
        e.preventDefault();

        // Update active nav item
        document.querySelectorAll('.nav-item').forEach(nav => nav.classList.remove('active'));
        this.classList.add('active');

        // Show target section
        document.querySelectorAll('.content-section').forEach(section => section.classList.remove('active'));
        document.getElementById(section).classList.add('active');

        // Update page title
        document.getElementById('pageTitle').textContent = this.textContent.trim();

        // Update header subtitle
        updateHeaderSubtitle(section);

        // Close sidebar on mobile
        if (window.innerWidth <= 768) {
            document.querySelector('.sidebar').classList.remove('active');
        }
    });
});

// Close dropdown when clicking outside
document.addEventListener('click', (e) => {
    const profileDropdown = document.querySelector('.profile-dropdown');
    if (profileDropdown && !profileDropdown.contains(e.target)) {
        // Dropdown closes automatically with CSS hover
    }
});