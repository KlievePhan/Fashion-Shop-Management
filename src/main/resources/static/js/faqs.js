// Toggle FAQ accordion
function toggleFaq(button) {
  const faqItem = button.parentElement;
  const isActive = faqItem.classList.contains('active');
  
  // Close all FAQ items
  document.querySelectorAll('.faq-item').forEach(item => {
    item.classList.remove('active');
  });
  
  // If the clicked item wasn't active, open it
  if (!isActive) {
    faqItem.classList.add('active');
  }
}

// Newsletter form submission
document.addEventListener('DOMContentLoaded', function() {
  const newsletterForm = document.querySelector('.newsletter-form');
  
  if (newsletterForm) {
    const input = newsletterForm.querySelector('.newsletter-input');
    const button = newsletterForm.querySelector('.subscribe-btn');
    
    button.addEventListener('click', function(e) {
      e.preventDefault();
      
      const email = input.value.trim();
      
      if (!email) {
        alert('Please enter your email address');
        return;
      }
      
      // Basic email validation
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(email)) {
        alert('Please enter a valid email address');
        return;
      }
      
      // Success message
      alert('Thank you for subscribing! Check your email for your 10% discount code.');
      input.value = '';
    });
    
    // Allow Enter key to submit
    input.addEventListener('keypress', function(e) {
      if (e.key === 'Enter') {
        button.click();
      }
    });
  }
});