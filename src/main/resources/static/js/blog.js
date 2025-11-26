// Smooth scroll
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function (e) {
    e.preventDefault();
    const target = document.querySelector(this.getAttribute('href'));
    if (target) {
      target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
});

// Newsletter form
document.querySelector('.newsletter-form').addEventListener('submit', function (e) {
  e.preventDefault();
  alert('Cảm ơn bạn đã đăng ký! Chúng tôi sẽ gửi những xu hướng thời trang mới nhất đến email của bạn.');
  this.reset();
});

// Blog card click
document.querySelectorAll('.blog-card').forEach(card => {
  card.addEventListener('click', function () {
    alert('Đang mở bài viết... (Bạn có thể thêm link đến trang chi tiết bài viết ở đây)');
  });
});