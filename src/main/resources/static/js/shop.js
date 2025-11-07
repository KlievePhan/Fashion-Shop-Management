const sortSelect = document.getElementById('sortSelect');
const productsGrid = document.querySelector('.products-grid');

sortSelect.addEventListener('change', function () {
  const sortValue = this.value;
  const products = Array.from(document.querySelectorAll('.product-card'));

  products.sort((a, b) => {
    switch (sortValue) {
      case 'latest':
        return new Date(b.dataset.date) - new Date(a.dataset.date);
      case 'price-low':
        return parseFloat(a.dataset.price) - parseFloat(b.dataset.price);
      case 'price-high':
        return parseFloat(b.dataset.price) - parseFloat(a.dataset.price);
      default:
        return 0;
    }
  });

  products.forEach(product => productsGrid.appendChild(product));
});

// Add click functionality to product cards
document.querySelectorAll('.product-card').forEach(card => {
  card.style.cursor = 'pointer';
  card.addEventListener('click', function () {
    const title = this.querySelector('.product-title').textContent;
    console.log('Clicked on:', title);
    // Navigate to product detail page
  });
});

// Xử lý chọn size
document.querySelectorAll('.size-btn').forEach(btn => {
  btn.addEventListener('click', function (event) {
    event.stopPropagation(); // Ngăn click lan ra card

    const parent = this.closest('.product-sizes');
    parent.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
    this.classList.add('active');
  });
});

// Xử lý chọn màu
document.querySelectorAll('.color-option').forEach(option => {
  option.addEventListener('click', function (event) {
    event.stopPropagation(); // Ngăn click lan ra card

    const parent = this.closest('.product-colors');
    parent.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
    this.classList.add('active');
  });
});


const cartModal = document.getElementById('cartModal');
const cartClose = document.querySelector('.cart-close');
const body = document.body;

// Fade + Scale Animation
function openModal() {
  cartModal.classList.add('active');
  body.style.overflow = 'hidden';
}

function closeModal() {
  cartModal.classList.remove('active');
  body.style.overflow = '';
}

// Click handlers
cartClose.addEventListener('click', closeModal);
cartModal.addEventListener('click', e => {
  if (e.target === cartModal) closeModal();
});

// Open modal when clicking product image
document.querySelectorAll('.product-image img').forEach(img => {
  img.addEventListener('click', e => {
    const card = e.target.closest('.product-card');
    if (!card) return;

    // Populate modal
    document.getElementById('modalProductImg').src = card.querySelector('img').src;
    document.getElementById('modalProductTitle').textContent = card.querySelector('.product-title').textContent;
    document.getElementById('modalProductCategory').textContent = card.querySelector('.product-category').textContent;
    document.getElementById('modalPrice').textContent = card.querySelector('.price-current').textContent;
    document.getElementById('modalOriginalPrice').textContent = card.querySelector('.price-original').textContent;

    // Clone sizes
    const modalSizes = document.getElementById('modalSizes');
    modalSizes.innerHTML = '';
    card.querySelectorAll('.size-btn').forEach(btn => {
      const newBtn = btn.cloneNode(true);
      newBtn.classList.remove('active');
      newBtn.addEventListener('click', () => {
        modalSizes.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
        newBtn.classList.add('active');
      });
      modalSizes.appendChild(newBtn);
    });

    // Clone colors
    const modalColors = document.getElementById('modalColors');
    modalColors.innerHTML = '';
    card.querySelectorAll('.color-option').forEach(option => {
      const newOption = option.cloneNode(true);
      newOption.classList.remove('active');
      newOption.addEventListener('click', () => {
        modalColors.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
        newOption.classList.add('active');
      });
      modalColors.appendChild(newOption);
    });

    // Reset qty
    const productQty = document.getElementById('productQty');
    productQty.value = 1;

    // Show modal
    openModal();
    // Show modal
    openModal();

    // Re-attach quantity listeners (đặt sau openModal)
    const decreaseBtn = document.getElementById('decreaseQty');
    const increaseBtn = document.getElementById('increaseQty');
    const qtyInput = document.getElementById('productQty');

    increaseBtn.onclick = () => {
      qtyInput.value = parseInt(qtyInput.value) + 1;
    };

    decreaseBtn.onclick = () => {
      if (parseInt(qtyInput.value) > parseInt(qtyInput.min)) {
        qtyInput.value = parseInt(qtyInput.value) - 1;
      }
    };

  });
});
