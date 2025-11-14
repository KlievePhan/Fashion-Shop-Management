// header.js - Xử lý dropdown, active link và cart badge

// ========== USER DROPDOWN ==========
document.addEventListener("click", function (e) {
  const userDropdown = document.querySelector(".user-dropdown");
  if (!userDropdown) return;

  if (userDropdown.contains(e.target)) {
    userDropdown.classList.toggle("active");
  } else {
    userDropdown.classList.remove("active");
  }
});

// ========== ACTIVE LINK HIGHLIGHT ==========
function highlightActiveLink() {
  const navLinks = document.querySelectorAll(".link-home a");
  const currentPath = window.location.pathname;

  navLinks.forEach((link) => {
    link.classList.remove("active");
    const href = link.getAttribute("href");
    if (href === currentPath || (currentPath.includes(href) && href !== "/")) {
      link.classList.add("active");
    }
  });
}

// ========== CART BADGE UPDATE ==========
/**
 * Hàm CHÍNH để cập nhật cart badge.
 * Sẽ được gọi bởi cart-update.js sau mỗi hành động.
 */
function refreshCartBadge() {
  fetch('/fashionshop/cart/count', {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin'
  })
    .then(response => response.json())
    .then(data => {
      const badge = document.querySelector('.cart-link .badge');
      if (badge) {
        const count = data.count || 0;
        badge.textContent = count;

        // Animate
        badge.style.transform = 'scale(1.3)';
        setTimeout(() => {
          badge.style.transform = 'scale(1)';
        }, 200);

        // Show/hide
        badge.style.display = (count > 0) ? 'flex' : 'none';
      }
    })
    .catch(error => {
      console.error('Error refreshing cart badge:', error);
    });
}

// Export to window cho file cart-update.js sử dụng
window.refreshCartBadge = refreshCartBadge;

// ========== DOM READY ==========
document.addEventListener("DOMContentLoaded", function () {
  highlightActiveLink();
  refreshCartBadge(); // Tải số lượng giỏ hàng ban đầu khi vào trang
  console.log('✅ header.js loaded - refreshCartBadge() is available.');
});