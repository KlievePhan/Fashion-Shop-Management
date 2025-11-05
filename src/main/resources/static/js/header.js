// header.js - Xử lý dropdown user và active link

// Toggle dropdown user menu
document.addEventListener("click", function (e) {
  const userDropdown = document.querySelector(".user-dropdown");

  if (!userDropdown) return;

  // Nếu click vào user icon hoặc dropdown
  if (userDropdown.contains(e.target)) {
    userDropdown.classList.toggle("active");
  } else {
    // Click bên ngoài thì đóng dropdown
    userDropdown.classList.remove("active");
  }
});

// Highlight active link dựa trên URL hiện tại
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

// Gọi khi DOM load xong
document.addEventListener("DOMContentLoaded", highlightActiveLink);