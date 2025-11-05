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
