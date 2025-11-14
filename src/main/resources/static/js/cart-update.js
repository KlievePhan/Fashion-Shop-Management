/**
 * cart-update.js
 * "Bộ não" xử lý TẤT CẢ các tương tác giỏ hàng (Add, Update, Remove)
 * và kích hoạt cập nhật UI (Badge, Notification, DOM).
 */

// ========== 1. NOTIFICATION TOAST (Giữ nguyên) ==========
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

  setTimeout(() => {
    notification.classList.add('show');
  }, 10);

  setTimeout(() => {
    notification.classList.remove('show');
    setTimeout(() => {
      notification.remove();
    }, 300);
  }, 3000);
}

// ========== 2. ADD TO CART (Sửa lại để gọi đúng hàm) ==========
function addToCartWithUpdate(variantId, qty = 1) {
  return fetch('/fashionshop/cart/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `variantId=${variantId}&qty=${qty}`
  })
    .then(response => response.text())
    .then(result => {
      if (result.includes('success')) {
        // KÍCH HOẠT REAL-TIME:
        if (typeof window.refreshCartBadge === 'function') {
          window.refreshCartBadge(); // 1. Gọi hàm của header.js
        }
        showNotification('Product added to cart!', 'success'); // 2. Hiển thị thông báo
        return true;
      } else {
        throw new Error(result); // Ném lỗi để .catch xử lý
      }
    })
    .catch(error => {
      console.error('Error adding to cart:', error.message);
      let msg = 'Failed to add to cart';
      if (error.message.includes('not logged in')) {
        msg = 'Please login first';
        setTimeout(() => window.location.href = '/fashionshop/login', 1500);
      }
      showNotification(msg, 'error');
      return false;
    });
}

// ========== 3. UPDATE QUANTITY (Hàm MỚI) ==========
function updateQuantityOnServer(button, change) {
  const cartItem = button.closest('.cart-item');
  const input = cartItem.querySelector('.quantity-input');
  const removeBtn = cartItem.querySelector('.remove-btn');
  const itemId = removeBtn.getAttribute('onclick').match(/(\d+)/)[0];

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
        // 1. Cập nhật DOM của trang cart.html
        input.value = newQty;
        const lineTotalSpan = cartItem.querySelector('.item-line-total span');
        if (lineTotalSpan) {
          lineTotalSpan.textContent = '$' + data.itemTotal.toFixed(2);
        }
        updateCartSummaryDOM(data); // Cập nhật Tóm tắt đơn hàng

        // 2. KÍCH HOẠT REAL-TIME:
        if (typeof window.refreshCartBadge === 'function') {
          window.refreshCartBadge();
        }
        showNotification('Cart updated!', 'success');
      } else {
        throw new Error(data.message || 'Failed to update quantity');
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification(error.message, 'error');
      location.reload(); // Tải lại trang nếu có lỗi
    })
    .finally(() => {
      buttons.forEach(btn => btn.disabled = false);
    });
}

// ========== 4. REMOVE FROM CART (Hàm MỚI) ==========
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
        // 1. Cập nhật DOM (xóa item)
        cartItem.style.transition = 'all 0.3s ease';
        cartItem.style.transform = 'translateX(-100%)';
        cartItem.style.opacity = '0';
        setTimeout(() => {
          cartItem.remove();
          if (data.isEmpty) {
            location.reload(); // Tải lại để hiển thị giỏ hàng trống
          } else {
            updateCartSummaryDOM(data); // Cập nhật tóm tắt
          }
        }, 300);

        // 2. KÍCH HOẠT REAL-TIME:
        if (typeof window.refreshCartBadge === 'function') {
          window.refreshCartBadge();
        }
        showNotification('Item removed from cart', 'success');
      } else {
        throw new Error(data.message || 'Failed to remove item');
      }
    })
    .catch(error => {
      console.error('Error:', error);
      showNotification(error.message, 'error');
      cartItem.style.opacity = '1';
      cartItem.style.pointerEvents = 'auto';
    });
}

// ========== 5. HELPER (Cập nhật Tóm tắt đơn hàng) ==========
function updateCartSummaryDOM(data) {
  // Hàm này chỉ chạy trên trang cart.html
  const summaryRows = document.querySelectorAll('.summary-row');
  if (summaryRows.length === 0) return; // Không phải trang cart

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

// ========== 6. EXPORT GLOBAL (Xuất ra window) ==========
window.addToCartWithUpdate = addToCartWithUpdate;
window.updateQuantityOnServer = updateQuantityOnServer;
window.removeFromCartOnServer = removeFromCartOnServer;
window.showNotification = showNotification; // Export luôn hàm này

console.log('✅ cart-update.js (Refactored) loaded.');