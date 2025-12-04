// Blog page functionality

document.addEventListener('DOMContentLoaded', function() {
    // Smooth scroll to blog section
    const exploreBtn = document.querySelector('.hero-btn');
    if (exploreBtn) {
        exploreBtn.addEventListener('click', function(e) {
            e.preventDefault();
            const blogSection = document.querySelector('#blog');
            if (blogSection) {
                blogSection.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }

    // Newsletter form submission
    const newsletterForm = document.querySelector('.newsletter-form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const emailInput = this.querySelector('input[type="email"]');
            const email = emailInput.value;

            if (email) {
                alert('Thank you for subscribing! We\'ll keep you updated with the latest fashion trends.');
                emailInput.value = '';
            }
        });
    }

    // Add hover effect to blog cards
    const blogCards = document.querySelectorAll('.blog-card');
    blogCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-10px)';
        });

        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });

    // Copy link functionality for share buttons
    const copyLinkBtns = document.querySelectorAll('.share-buttons a:last-child');
    copyLinkBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            const url = window.location.href;
            navigator.clipboard.writeText(url).then(() => {
                alert('Link copied to clipboard!');
            }).catch(err => {
                console.error('Failed to copy link:', err);
            });
        });
    });
});