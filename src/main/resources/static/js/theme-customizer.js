// ===== THEME CUSTOMIZER SCRIPT =====

document.addEventListener("DOMContentLoaded", function () {
  // Create customizer HTML
  createCustomizer();

  // Load saved theme (silent)
  loadSavedTheme();

  // Event listeners
  initEventListeners();
});

// Create Customizer Panel HTML
function createCustomizer() {
  const customizerHTML = `
    <!-- Customizer Overlay -->
    <div class="customizer-overlay" id="customizerOverlay"></div>
    
    <!-- Customizer Panel -->
    <div class="theme-customizer" id="themeCustomizer">
      <!-- Header -->
      <div class="customizer-header">
        <h3>
          <i class="bi bi-palette"></i>
          Theme Customizer
        </h3>
        <button class="customizer-close" id="customizerClose">
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
    
      <!-- Content -->
      <div class="customizer-content">
        
        <!-- Theme Mode Section -->
        <div class="customizer-section">
          <h4>
            <i class="bi bi-moon-stars"></i>
            Theme Mode
          </h4>
          <p>Choose your preferred color scheme</p>
          
          <div class="theme-options">
            <!-- Light Mode -->
            <div class="theme-option active" data-theme="light">
              <div class="theme-preview light">
                <div class="theme-label">
                  <i class="bi bi-sun-fill"></i> Light
                </div>
              </div>
            </div>
            
            <!-- Dark Mode -->
            <div class="theme-option" data-theme="dark">
              <div class="theme-preview dark">
                <div class="theme-label">
                  <i class="bi bi-moon-fill"></i> Dark
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Currency Converter Section -->
        <div class="customizer-section">
          <h4>
            <i class="bi bi-currency-exchange"></i>
            Currency Converter
          </h4>
          <p>Convert USD amount to VND</p>
          <input type="number" id="usdAmount" placeholder="Enter USD" style="width: 100%; padding: 8px; margin-bottom: 10px; border-radius: 6px; border: 1px solid #ccc;">
          <button class="convert-btn" id="convertBtn">Convert to VND</button>
          <p id="vndResult" style="margin-top: 10px; font-weight: bold;"></p>
        </div>

        <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;">
        
        <!-- Additional Info -->
        <div class="customizer-section">
          <h4>
            <i class="bi bi-info-circle"></i>
            About
          </h4>
          <p style="font-size: 13px; color: #6b7280; line-height: 1.6;">
            Your theme preference is saved locally and will persist across sessions.
          </p>
        </div>
        
        <!-- Reset Button -->
        <button class="reset-btn" id="resetTheme">
          <i class="bi bi-arrow-clockwise"></i>
          Reset to Default
        </button>
        
      </div>
    </div>
  `;

  // Inject into body
  document.body.insertAdjacentHTML("beforeend", customizerHTML);
}

// Initialize Event Listeners
function initEventListeners() {
  const customizeBtn = document.querySelector(".customize-btn");
  const customizer = document.getElementById("themeCustomizer");
  const overlay = document.getElementById("customizerOverlay");
  const closeBtn = document.getElementById("customizerClose");
  const themeOptions = document.querySelectorAll(".theme-option");
  const resetBtn = document.getElementById("resetTheme");

  // Open customizer
  if (customizeBtn) {
    customizeBtn.addEventListener("click", function (e) {
      e.preventDefault();
      openCustomizer();
    });
  }

  // Close customizer
  closeBtn.addEventListener("click", closeCustomizer);
  overlay.addEventListener("click", closeCustomizer);

  // Theme selection (show toast ONLY on click)
  themeOptions.forEach((option) => {
    option.addEventListener("click", function () {
      const theme = this.getAttribute("data-theme");

      // apply with notification because this is a user action
      applyTheme(theme, { notify: true });

      // Update active state
      themeOptions.forEach((opt) => opt.classList.remove("active"));
      this.classList.add("active");
    });
  });

  // Reset theme (also show toast)
  resetBtn.addEventListener("click", function () {
    applyTheme("light", { notify: true });
    themeOptions.forEach((opt) => opt.classList.remove("active"));
    document.querySelector('[data-theme="light"]').classList.add("active");
  });

  // Keyboard shortcut: ESC to close
  document.addEventListener("keydown", function (e) {
    if (e.key === "Escape" && customizer.classList.contains("active")) {
      closeCustomizer();
    }
  });

  // Convert USD -> VND
  const convertBtn = document.getElementById('convertBtn');
  const usdInput = document.getElementById('usdAmount');
  const vndResult = document.getElementById('vndResult');

  if (convertBtn) {
    convertBtn.addEventListener('click', function () {
      const usd = parseFloat(usdInput.value);
      if (isNaN(usd)) {
        vndResult.textContent = "Please enter a valid USD amount!";
        return;
        }
      const rate = 24000; // tỉ giá ví dụ
      const vnd = usd * rate;
      vndResult.textContent = `${usd} USD = ${vnd.toLocaleString()} VND`;
    });
  }
}

// Open Customizer
function openCustomizer() {
  const customizer = document.getElementById("themeCustomizer");
  const overlay = document.getElementById("customizerOverlay");

  customizer.classList.add("active");
  overlay.classList.add("active");
  document.body.style.overflow = "hidden"; // Prevent body scroll
}

// Close Customizer
function closeCustomizer() {
  const customizer = document.getElementById("themeCustomizer");
  const overlay = document.getElementById("customizerOverlay");

  customizer.classList.remove("active");
  overlay.classList.remove("active");
  document.body.style.overflow = ""; // Restore body scroll
}

// Apply Theme — silent by default; notify only when requested
function applyTheme(theme, opts = { notify: false }) {
  if (theme === "dark") {
    document.body.classList.add("dark-mode");
  } else {
    document.body.classList.remove("dark-mode");
  }

  // Save to localStorage
  localStorage.setItem("selectedTheme", theme);

  // Only show toast if explicitly requested (user action)
  if (opts.notify) {
    showNotification(`${theme.charAt(0).toUpperCase() + theme.slice(1)} mode activated!`);
  }
}

// Load Saved Theme (no toast here)
function loadSavedTheme() {
  const savedTheme = localStorage.getItem("selectedTheme") || "light";

  // Apply silently on page load
  applyTheme(savedTheme, { notify: false });

  // Update active option
  const themeOptions = document.querySelectorAll(".theme-option");
  themeOptions.forEach((opt) => {
    if (opt.getAttribute("data-theme") === savedTheme) {
      opt.classList.add("active");
    } else {
      opt.classList.remove("active");
    }
  });
}

// Show Notification (Toast)
function showNotification(message) {
  // Remove existing notification
  const existingToast = document.querySelector(".theme-toast");
  if (existingToast) {
    existingToast.remove();
  }

  // Create toast
  const toast = document.createElement("div");
  toast.className = "theme-toast";
  toast.innerHTML = `
    <i class="bi bi-check-circle-fill"></i>
    <span>${message}</span>
  `;

  document.body.appendChild(toast);

  // Show toast
  setTimeout(() => toast.classList.add("show"), 100);

  // Hide and remove toast
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 300);
  }, 2500);
}
