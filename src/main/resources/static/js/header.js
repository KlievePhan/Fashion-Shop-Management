// header.js - Xử lý dropdown user và active link

let hoverTimeout;

// ===== USER DROPDOWN LOGIC =====
document.addEventListener('DOMContentLoaded', function () {
  const userDropdowns = document.querySelectorAll('.user-dropdown');

  userDropdowns.forEach(dropdown => {
    const menu = dropdown.querySelector('.dropdown-menu');

    const showMenu = () => {
      clearTimeout(hoverTimeout);
      // Đóng các dropdown khác
      userDropdowns.forEach(d => {
        if (d !== dropdown) d.classList.remove('active');
      });
      dropdown.classList.add('active');
    };

    const hideMenu = () => {
      hoverTimeout = setTimeout(() => {
        dropdown.classList.remove('active');
      }, 300); // Đợi 300ms trước khi đóng
    };

    // Click để toggle
    dropdown.addEventListener('click', (e) => {
      e.stopPropagation();
      if (dropdown.classList.contains('active')) {
        hideMenu();
      } else {
        showMenu();
      }
    });

    // Hover: mở ngay, đóng có delay
    dropdown.addEventListener('mouseenter', showMenu);
    dropdown.addEventListener('mouseleave', hideMenu);

    // Giữ menu mở khi hover vào menu
    if (menu) {
      menu.addEventListener('mouseenter', () => {
        clearTimeout(hoverTimeout);
        dropdown.classList.add('active');
      });
      menu.addEventListener('mouseleave', hideMenu);
    }
  });

  // Đóng tất cả dropdown khi click bên ngoài
  document.addEventListener('click', () => {
    userDropdowns.forEach(d => {
      d.classList.remove('active');
    });
  });
});

// ===== HIGHLIGHT ACTIVE LINK =====
function highlightActiveLink() {
  const navLinks = document.querySelectorAll(".link-home a");
  const currentPath = window.location.pathname;

  navLinks.forEach((link) => {
    link.classList.remove("active");

    const href = link.getAttribute("href");

    // So sánh đường dẫn
    if (href === currentPath ||
      (currentPath === "/" && href === "/") ||
      (currentPath.includes(href) && href !== "/")) {
      link.classList.add("active");
    }
  });
}

document.addEventListener("DOMContentLoaded", highlightActiveLink);

// ===== SYNC THEME WITH HEADER ICONS ===== 
function updateHeaderIcons() {
  const isDarkMode = document.body.classList.contains('dark-mode');
  const iconColor = isDarkMode ? '#e5e7eb' : 'black';

  // Update user icon
  const userIcons = document.querySelectorAll('.user-icon');
  userIcons.forEach(icon => {
    icon.setAttribute('stroke', iconColor);
  });

  // Update cart icon
  const cartIcons = document.querySelectorAll('.cart-icon');
  cartIcons.forEach(icon => {
    icon.setAttribute('stroke', iconColor);
  });
}

// Listen for theme changes
const observer = new MutationObserver((mutations) => {
  mutations.forEach((mutation) => {
    if (mutation.attributeName === 'class') {
      updateHeaderIcons();
    }
  });
});

// Start observing body class changes
if (document.body) {
  observer.observe(document.body, { attributes: true });
}

// Initial update on page load
document.addEventListener('DOMContentLoaded', updateHeaderIcons);