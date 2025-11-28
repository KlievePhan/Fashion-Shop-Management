// header.js - FIXED VERSION - XÓA HẾT CODE CŨ VÀ DÙNG CÁI NÀY

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

// ========== 1. CART BADGE UPDATE (GIỮ NGUYÊN) ==========
function refreshCartBadge() {
  fetch('/fashionshop/cart/count', {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin'
  })
    .then(response => response.json())
    .then(data => {
      const badge = document.querySelector('.cart-badge'); // ⭐ Tìm theo CLASS
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

        console.log('✅ Cart badge updated:', count);
      }
    })
    .catch(error => {
      console.error('❌ Error refreshing cart badge:', error);
    });
}

// ========== 2. WISHLIST BADGE UPDATE (FIXED) ==========
function updateWishlistCount() {
  console.log('🔥 updateWishlistCount() called from header.js');

  fetch('/fashionshop/wishlist/count', {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'same-origin'
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      // ⭐ TÌM THEO ID (vì header.html dùng id="wishlist-count")
      const badge = document.getElementById('wishlist-count');

      if (!badge) {
        console.error('❌ #wishlist-count element NOT FOUND!');
        return;
      }

      const count = data.count || 0;

      console.log('📊 Wishlist count from API:', count);

      // Update text
      badge.textContent = count > 0 ? count : '';
      badge.dataset.count = count;

      // Animate
      if (count > 0) {
        badge.style.display = 'flex';
        badge.style.transform = 'scale(1.3)';
        setTimeout(() => {
          badge.style.transform = 'scale(1)';
        }, 200);
      } else {
        badge.style.display = 'none';
      }

      console.log('✅ Wishlist badge updated:', count);
    })
    .catch(error => {
      console.error('❌ Error updating wishlist badge:', error);
    });
}

// ========== 3. DOM READY - INIT BOTH BADGES ==========
document.addEventListener("DOMContentLoaded", function () {
  console.log('🚀 header.js loaded');

  highlightActiveLink();

  // Update both badges
  refreshCartBadge();
  updateWishlistCount();

  console.log('✅ Both badge functions initialized');
});

// ========== 4. EXPORT TO GLOBAL ==========
window.refreshCartBadge = refreshCartBadge;
window.updateWishlistCount = updateWishlistCount;

console.log('✅ header.js fully loaded');