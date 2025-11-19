/**
 * cart-update.js - COMPLETE VERSION for New Architecture
 */

// ========== 1. NOTIFICATION TOAST ==========
function showNotification(message, type = 'success') {
    const existing = document.querySelector('.cart-notification');
    if (existing) {
        existing.remove();
    }

    const notification = document.createElement('div');
    notification.className = `cart-notification ${type}`;
    notification.innerHTML = `
    <i class="bi bi-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
    <span>${message}</span>
  `;
    document.body.appendChild(notification);

    setTimeout(() => notification.classList.add('show'), 10);

    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// ========== 2. ADD TO CART ==========
/**
 * Add item to cart with productId + selectedOptions
 * @param {number} productId - Product ID
 * @param {Object} selectedOptions - {"size":"41", "color":"Red"}
 * @param {number} qty - Quantity
 * @returns {Promise<boolean>}
 */
async function addToCartWithUpdate(productId, selectedOptions, qty = 1) {
    try {
        const response = await fetch('/fashionshop/cart/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                productId: productId,
                selectedOptions: selectedOptions,
                qty: qty
            })
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();

        if (data.success) {
            // Show notification
            if (typeof showHeadNotification === 'function') {
                showHeadNotification(
                    'Added to cart!',
                    'Item has been successfully added to your cart.',
                    { variant: 'success', duration: 2600 }
                );
            } else {
                showNotification('Product added to cart!', 'success');
            }

            // Update cart badge
            if (typeof window.refreshCartBadge === 'function') {
                window.refreshCartBadge();
            }
            updateCartBadge();

            return true;

        } else {
            throw new Error(data.message || 'Failed to add to cart');
        }

    } catch (error) {
        console.error('Error adding to cart:', error);

        const message = error.message || 'Failed to add item to cart.';

        if (typeof showHeadNotification === 'function') {
            showHeadNotification('Error', message, { variant: 'error', duration: 3200 });
        } else {
            showNotification(message, 'error');
        }

        return false;
    }
}

// ========== 3. UPDATE QUANTITY ==========
function updateQuantityOnServer(button, change) {
    const cartItem = button.closest('.cart-item');
    const input = cartItem.querySelector('.quantity-input');
    const removeBtn = cartItem.querySelector('.remove-btn');
    const itemId = removeBtn.getAttribute('onclick').match(/\d+/)[0];

    let newQty = parseInt(input.value) + change;
    if (newQty < 1) newQty = 1;

    const buttons = cartItem.querySelectorAll('.quantity-btn');
    buttons.forEach(btn => btn.disabled = true);

    fetch('/fashionshop/cart/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `itemId=${itemId}&qty=${newQty}`
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                input.value = newQty;

                const lineTotalDiv = cartItem.querySelector('.item-line-total');
                if (lineTotalDiv) {
                    const priceSpan = lineTotalDiv.querySelector('span');
                    if (priceSpan) {
                        priceSpan.textContent = '$' + data.itemTotal.toFixed(2);
                    }
                }

                updateCartSummaryDOM(data);

                if (typeof window.refreshCartBadge === 'function') {
                    window.refreshCartBadge();
                }
                updateCartBadge();

                if (typeof showHeadNotification === 'function') {
                    showHeadNotification('Cart updated!', '', { variant: 'success', duration: 1500 });
                } else {
                    showNotification('Cart updated!', 'success');
                }

            } else {
                throw new Error(data.message || 'Failed to update quantity');
            }
        })
        .catch(error => {
            console.error('Error:', error);

            const message = error.message || 'An error occurred';
            if (typeof showHeadNotification === 'function') {
                showHeadNotification('Error', message, { variant: 'error', duration: 3200 });
            } else {
                showNotification(message, 'error');
            }

            location.reload();
        })
        .finally(() => {
            buttons.forEach(btn => btn.disabled = false);
        });
}

// ========== 4. REMOVE FROM CART ==========
function removeFromCartOnServer(itemId) {
    if (!confirm('Are you sure you want to remove this item?')) {
        return;
    }

    const cartItem = document.querySelector(`[onclick*="removeFromCartOnServer(${itemId})"]`).closest('.cart-item');
    cartItem.style.opacity = '0.5';
    cartItem.style.pointerEvents = 'none';

    fetch(`/fashionshop/cart/delete/${itemId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                cartItem.style.transition = 'all 0.3s ease';
                cartItem.style.transform = 'translateX(-100%)';
                cartItem.style.opacity = '0';

                setTimeout(() => {
                    cartItem.remove();

                    if (data.isEmpty) {
                        location.reload();
                    } else {
                        updateCartSummaryDOM(data);
                    }
                }, 300);

                if (typeof window.refreshCartBadge === 'function') {
                    window.refreshCartBadge();
                }
                updateCartBadge();

                if (typeof showHeadNotification === 'function') {
                    showHeadNotification('Item removed', '', { variant: 'success', duration: 2000 });
                } else {
                    showNotification('Item removed from cart', 'success');
                }

            } else {
                throw new Error(data.message || 'Failed to remove item');
            }
        })
        .catch(error => {
            console.error('Error:', error);

            const message = error.message || 'An error occurred';
            if (typeof showHeadNotification === 'function') {
                showHeadNotification('Error', message, { variant: 'error', duration: 3200 });
            } else {
                showNotification(message, 'error');
            }

            cartItem.style.opacity = '1';
            cartItem.style.pointerEvents = 'auto';
        });
}

// ========== 5. HELPER FUNCTIONS ==========
function updateCartSummaryDOM(data) {
    const summaryRows = document.querySelectorAll('.summary-row');
    if (summaryRows.length === 0) return;

    summaryRows.forEach(row => {
        const label = row.querySelector('span:first-child').textContent.toLowerCase();
        const valueSpan = row.querySelector('span:last-child');

        if (label.includes('subtotal')) {
            valueSpan.textContent = '$' + (data.subtotal || 0).toFixed(2);
        } else if (label.includes('shipping')) {
            valueSpan.textContent = '$' + (data.shipping || 0).toFixed(2);
        } else if (label.includes('tax')) {
            valueSpan.textContent = '$' + (data.tax || 0).toFixed(2);
        } else if (label.includes('total')) {
            valueSpan.textContent = '$' + (data.total || 0).toFixed(2);
        }
    });
}

function updateCartBadge() {
    fetch('/fashionshop/cart/count')
        .then(response => response.json())
        .then(data => {
            const badge = document.querySelector('.cart-badge');
            if (badge) {
                const count = data.count || 0;
                badge.textContent = count;
                badge.style.display = count > 0 ? 'flex' : 'none';
            }
        })
        .catch(error => {
            console.error('Error updating cart badge:', error);
        });
}

// ========== BACKWARD COMPATIBILITY ==========
window.updateQuantity = updateQuantityOnServer;
window.removeFromCart = removeFromCartOnServer;

// ========== EXPORT TO GLOBAL ==========
window.addToCartWithUpdate = addToCartWithUpdate;
window.updateQuantityOnServer = updateQuantityOnServer;
window.removeFromCartOnServer = removeFromCartOnServer;
window.updateCartBadge = updateCartBadge;
window.showNotification = showNotification;

// ========== AUTO INIT ==========
document.addEventListener('DOMContentLoaded', () => {
    updateCartBadge();
    console.log('✅ cart-update.js loaded successfully.');
});