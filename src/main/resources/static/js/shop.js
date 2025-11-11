document.addEventListener('DOMContentLoaded', () => {
  const productsGrid = document.querySelector('.products-grid');
  const sortSelect = document.getElementById('sortSelect');

  // SORT
  if (sortSelect && productsGrid) {
    sortSelect.addEventListener('change', function () {
      const sortValue = this.value;
      const cards = Array.from(productsGrid.querySelectorAll('.product-card'));
      const getPrice = el => parseFloat(el.dataset.price || '0') || 0;
      const getDate  = el => new Date(el.dataset.date || '1970-01-01').getTime();

      cards.sort((a, b) => {
        switch (sortValue) {
          case 'latest':     return getDate(b) - getDate(a);
          case 'price-low':  return getPrice(a) - getPrice(b);
          case 'price-high': return getPrice(b) - getPrice(a);
          default:           return 0;
        }
      });
      cards.forEach(c => productsGrid.appendChild(c));
    });
  }

  // MODAL
  const cartModal = document.getElementById('cartModal');
  const cartClose = cartModal ? cartModal.querySelector('.cart-close') : null;
  const body = document.body;

  const openModal  = () => { if(cartModal){ cartModal.classList.add('active'); body.style.overflow='hidden'; } };
  const closeModal = () => { if(cartModal){ cartModal.classList.remove('active'); body.style.overflow=''; } };

  cartClose && cartClose.addEventListener('click', closeModal);
  cartModal && cartModal.addEventListener('click', e => { if (e.target === cartModal) closeModal(); });

  // QUICK VIEW / ADD
  productsGrid?.querySelectorAll('.product-card').forEach(card => {
    const imgEl = card.querySelector('.product-image img');
    const titleEl = card.querySelector('.product-title');
    const catEl = card.querySelector('.product-category');
    const priceCurrentEl = card.querySelector('.price-current');
    const priceOriginalEl = card.querySelector('.price-original'); // có thể vắng

    card.querySelectorAll('.product-actions .action-btn').forEach(btn => {
      const action = btn.dataset.action;

      if (action === 'quick') {
        btn.addEventListener('click', (e) => {
          e.preventDefault(); e.stopPropagation();

          if (imgEl)   document.getElementById('modalProductImg').src = imgEl.src;
          if (titleEl) document.getElementById('modalProductTitle').textContent = titleEl.textContent;
          if (catEl)   document.getElementById('modalProductCategory').textContent = catEl.textContent;
          if (priceCurrentEl) document.getElementById('modalPrice').textContent = priceCurrentEl.textContent;
          document.getElementById('modalOriginalPrice').textContent = priceOriginalEl ? priceOriginalEl.textContent : '';

          const modalSizes = document.getElementById('modalSizes');
          modalSizes.innerHTML = '';
          card.querySelectorAll('.size-btn').forEach(btn => {
            const clone = btn.cloneNode(true);
            clone.classList.remove('active');
            clone.addEventListener('click', () => {
              modalSizes.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
              clone.classList.add('active');
            });
            modalSizes.appendChild(clone);
          });

          const modalColors = document.getElementById('modalColors');
          modalColors.innerHTML = '';
          card.querySelectorAll('.color-option').forEach(dot => {
            const clone = dot.cloneNode(true);
            clone.classList.remove('active');
            clone.addEventListener('click', () => {
              modalColors.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
              clone.classList.add('active');
            });
            modalColors.appendChild(clone);
          });

          const qtyInput = document.getElementById('productQty');
          const dec = document.getElementById('decreaseQty');
          const inc = document.getElementById('increaseQty');
          if (qtyInput) qtyInput.value = 1;
          if (inc) inc.onclick = () => qtyInput.value = parseInt(qtyInput.value || '1') + 1;
          if (dec) dec.onclick = () => {
            const cur = parseInt(qtyInput.value || '1');
            const min = parseInt(qtyInput.min || '1');
            if (cur > min) qtyInput.value = cur - 1;
          };

          openModal();
        });
      }

      if (action === 'add') {
        btn.addEventListener('click', (e) => {
          e.preventDefault(); e.stopPropagation();
          const productId = btn.dataset.id;
          console.log('Add to cart:', productId);
          // TODO: gọi API add-to-cart nếu có
        });
      }
    });
  });

  // chọn size/màu trên card
  document.querySelectorAll('.product-sizes').forEach(group => {
    group.addEventListener('click', (e) => {
      const btn = e.target.closest('.size-btn'); if (!btn) return;
      e.stopPropagation();
      group.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  document.querySelectorAll('.product-colors').forEach(group => {
    group.addEventListener('click', (e) => {
      const dot = e.target.closest('.color-option'); if (!dot) return;
      e.stopPropagation();
      group.querySelectorAll('.color-option').forEach(c => c.classList.remove('active'));
      dot.classList.add('active');
    });
  });
});
