/**
 * Shop Page JavaScript with Real-time Cart Update
 */

document.addEventListener("DOMContentLoaded", () => {
  // ========== SORTING ==========
  const sortSelect = document.getElementById("sortSelect");
  const productsGrid = document.querySelector(".products-grid");
  const productCards = Array.from(document.querySelectorAll(".product-card"));

  if (sortSelect && productsGrid) {
    sortSelect.addEventListener("change", () => {
      const sortValue = sortSelect.value;
      let sortedCards = [...productCards];

      switch (sortValue) {
        case "price-low":
          sortedCards.sort((a, b) => {
            const priceA = parseFloat(a.dataset.price) || 0;
            const priceB = parseFloat(b.dataset.price) || 0;
            return priceA - priceB;
          });
          break;

        case "price-high":
          sortedCards.sort((a, b) => {
            const priceA = parseFloat(a.dataset.price) || 0;
            const priceB = parseFloat(b.dataset.price) || 0;
            return priceB - priceA;
          });
          break;

        case "latest":
          sortedCards.sort((a, b) => {
            const dateA = new Date(a.dataset.date || 0);
            const dateB = new Date(b.dataset.date || 0);
            return dateB - dateA;
          });
          break;

        case "popularity":
        case "rating":
          break;
      }

      productsGrid.innerHTML = "";
      sortedCards.forEach(card => productsGrid.appendChild(card));
    });
  }

  // ========== MODAL ==========
  const modal = document.getElementById("cartModal");
  const closeBtn = document.querySelector(".cart-close");

  const openModal = () => {
    if (modal) {
      modal.classList.add('active');
      document.body.style.overflow = 'hidden';
    }
  };

  const closeModal = () => {
    if (modal) {
      modal.classList.remove('active');
      document.body.style.overflow = '';
    }
  };

  if (closeBtn) {
    closeBtn.addEventListener("click", closeModal);
  }

  window.addEventListener("click", (e) => {
    if (e.target === modal) {
      closeModal();
    }
  });

  // ========== QUICK VIEW & ADD TO CART BUTTONS ==========
  document.querySelectorAll(".action-btn").forEach((btn) => {
    btn.addEventListener("click", async (e) => {
      e.preventDefault();
      e.stopPropagation();

      const action = btn.dataset.action;
      const productId = btn.dataset.id;

      if (action === "add") {
        await handleQuickAddToCart(productId, btn);
      } else if (action === "quick") {
        await openQuickViewModal(productId);
      }
    });
  });

  // ========== QUICK ADD TO CART (No Modal) ==========
  async function handleQuickAddToCart(productId, buttonElement) {
    try {
      const response = await fetch(`/fashionshop/api/products/${productId}`);
      if (!response.ok) throw new Error('Product not found');

      const product = await response.json();

      if (!product.variants || product.variants.length === 0) {
        showNotification('Product has no variants available', 'error');
        return;
      }

      const firstVariant = product.variants[0];
      const success = await addToCartWithUpdate(firstVariant.id, 1);

      if (success && buttonElement) {
        const icon = buttonElement.querySelector('i');
        if (icon) {
          icon.classList.remove('bi-bag-plus');
          icon.classList.add('bi-check-lg');

          setTimeout(() => {
            icon.classList.remove('bi-check-lg');
            icon.classList.add('bi-bag-plus');
          }, 1000);
        }
      }
    } catch (error) {
      console.error('Error:', error);
      showNotification('Failed to add product', 'error');
    }
  }

  // ========== QUICK VIEW MODAL ==========
  async function openQuickViewModal(productId) {
    try {
      const response = await fetch(`/fashionshop/api/products/${productId}`);
      if (!response.ok) throw new Error('Product not found');

      const product = await response.json();

      // Populate modal
      document.getElementById("modalProductImg").src = product.primaryImageUrl || '/images/placeholder.jpg';
      document.getElementById("modalProductTitle").textContent = product.title || 'Product';
      document.getElementById("modalProductCategory").textContent = product.category?.name || 'Category';
      document.getElementById("modalPrice").textContent = formatPrice(product.basePrice) + ' ₫';

      const sizesContainer = document.getElementById("modalSizes");
      const colorsContainer = document.getElementById("modalColors");
      sizesContainer.innerHTML = '';
      colorsContainer.innerHTML = '';

      let selectedVariantId = null;

      if (product.variants && product.variants.length > 0) {
        product.variants.forEach((variant, index) => {
          const attrs = parseAttributes(variant.attributeJson);

          if (attrs.size) {
            const sizeBtn = document.createElement('button');
            sizeBtn.className = 'size-btn' + (index === 0 ? ' active' : '');
            sizeBtn.textContent = attrs.size;
            sizeBtn.dataset.variantId = variant.id;
            sizeBtn.addEventListener('click', () => {
              document.querySelectorAll('#modalSizes .size-btn').forEach(b => b.classList.remove('active'));
              sizeBtn.classList.add('active');
              selectedVariantId = variant.id;
            });
            sizesContainer.appendChild(sizeBtn);
          }

          if (attrs.color) {
            const colorDiv = document.createElement('div');
            colorDiv.className = 'color-option' + (index === 0 ? ' active' : '');
            colorDiv.style.background = attrs.color;
            colorDiv.dataset.variantId = variant.id;
            colorDiv.addEventListener('click', () => {
              document.querySelectorAll('#modalColors .color-option').forEach(c => c.classList.remove('active'));
              colorDiv.classList.add('active');
              selectedVariantId = variant.id;
            });
            colorsContainer.appendChild(colorDiv);
          }

          if (index === 0) {
            selectedVariantId = variant.id;
          }
        });
      }

      document.getElementById("productQty").value = 1;

      document.getElementById("addToCartBtn").onclick = async () => {
        const qty = parseInt(document.getElementById("productQty").value);

        if (!selectedVariantId) {
          showNotification('Please select a variant', 'error');
          return;
        }

        const success = await addToCartWithUpdate(selectedVariantId, qty);
        if (success) {
          closeModal();
        }
      };

      openModal();

    } catch (error) {
      console.error('Error:', error);
      showNotification('Failed to load product details', 'error');
    }
  }

  // ========== QUANTITY CONTROLS ==========
  const decreaseBtn = document.getElementById("decreaseQty");
  const increaseBtn = document.getElementById("increaseQty");
  const qtyInput = document.getElementById("productQty");

  if (decreaseBtn && qtyInput) {
    decreaseBtn.addEventListener("click", () => {
      let val = parseInt(qtyInput.value) || 1;
      if (val > 1) qtyInput.value = val - 1;
    });
  }

  if (increaseBtn && qtyInput) {
    increaseBtn.addEventListener("click", () => {
      let val = parseInt(qtyInput.value) || 1;
      qtyInput.value = val + 1;
    });
  }

  // ========== SIZE & COLOR SELECTION IN CARDS ==========
  document.querySelectorAll('.product-sizes').forEach(group => {
    group.addEventListener('click', (e) => {
      const btn = e.target.closest('.size-btn');
      if (!btn) return;
      e.stopPropagation();

      group.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  document.querySelectorAll('.product-colors').forEach(group => {
    group.addEventListener('click', (e) => {
      const dot = e.target.closest('.color-option');
      if (!dot) return;
      e.stopPropagation();

      group.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
      dot.classList.add('active');
    });
  });

  // ========== HELPER FUNCTIONS ==========
  function parseAttributes(json) {
    try {
      return json ? JSON.parse(json) : {};
    } catch {
      return {};
    }
  }

  function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN').format(price || 0);
  }

  // ========== UPDATE BADGE ON PAGE LOAD ==========
  updateCartBadge();
});