
// =========== CHOOSE SIZE , COLOR FUNCTION============
// Size button interactions
document.querySelectorAll('.size-btn').forEach(btn => {
  btn.addEventListener('click', function () {
    const card = this.closest('.product-card');
    card.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
    this.classList.add('active');
  });
});

// Color option interactions
document.querySelectorAll('.color-option').forEach(option => {
  option.addEventListener('click', function () {
    const card = this.closest('.product-card');
    card.querySelectorAll('.color-option').forEach(o => o.classList.remove('active'));
    this.classList.add('active');
  });
});

// Action button interactions
document.querySelectorAll('.action-btn').forEach(btn => {
  btn.addEventListener('click', function () {
    const title = this.getAttribute('title');
    if (title === 'Add to Cart') {
      alert('Added to cart!');
    } else if (title === 'Quick View') {
      alert('Quick view modal would open here');
    }
  });
});